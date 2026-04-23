import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { pageMeta } from "../../common/api-response";
import { AppException } from "../../common/app.exception";
import { CurrentUserPayload } from "../../common/current-user.decorator";
import { ensureString, readPage } from "../../common/sql";

@Injectable()
export class CommentsService {
  constructor(private readonly dataSource: DataSource) {}

  async list(postId: number, query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const countRows = await this.dataSource.query(
      `select count(*) as count from comments c where c.post_id = ? and c.status <> 'DELETED'`,
      [postId],
    );
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    const rows = await this.dataSource.query(
      `select c.id, c.post_id as postId, c.user_id as userId, c.content, c.status,
              c.created_at as createdAt, c.updated_at as updatedAt,
              u.id as authorId, u.nickname as authorNickname
       from comments c
       join users u on u.id = c.user_id
       where c.post_id = ? and c.status <> 'DELETED'
       order by c.created_at asc
       limit ? offset ?`,
      [postId, limit, offset],
    );
    return { items: (rows as Record<string, unknown>[]).map((row) => this.mapCommentRow(row)), meta: pageMeta(page, limit, totalCount) };
  }

  async one(commentId: number): Promise<Record<string, unknown>> {
    const rows = await this.dataSource.query(
      `select c.id, c.post_id as postId, c.user_id as userId, c.content, c.status,
              c.created_at as createdAt, c.updated_at as updatedAt,
              u.id as authorId, u.nickname as authorNickname
       from comments c
       join users u on u.id = c.user_id
       where c.id = ? and c.status <> 'DELETED'`,
      [commentId],
    );
    const comment = rows[0] as Record<string, unknown> | undefined;
    if (!comment) {
      throw new AppException("COMMENT_NOT_FOUND", "Comment not found", HttpStatus.NOT_FOUND);
    }
    return this.mapCommentRow(comment);
  }

  async create(user: CurrentUserPayload, postId: number, body: Record<string, unknown>) {
    this.assertUser(user);
    const content = ensureString(body.content, "content");
    const postRows = await this.dataSource.query(`select id from posts where id = ? and status = 'ACTIVE'`, [postId]);
    if (postRows.length === 0) {
      throw new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND);
    }
    const inserted = (await this.dataSource.query(
      `insert into comments (post_id, user_id, content, status) values (?, ?, ?, 'ACTIVE')`,
      [postId, user.sub, content],
    )) as { insertId: number };
    await this.dataSource.query(`update posts set comment_count = comment_count + 1, updated_at = now() where id = ?`, [postId]);
    return this.one(inserted.insertId);
  }

  async update(user: CurrentUserPayload, commentId: number, body: Record<string, unknown>) {
    this.assertUser(user);
    const current = await this.one(commentId);
    if (Number(current.userId) !== user.sub) {
      throw new AppException("FORBIDDEN", "Only the author can update this comment", HttpStatus.FORBIDDEN);
    }
    const content = ensureString(body.content, "content");
    await this.dataSource.query(`update comments set content = ?, updated_at = now() where id = ?`, [content, commentId]);
    return this.one(commentId);
  }

  async remove(user: CurrentUserPayload, commentId: number) {
    this.assertUser(user);
    const current = await this.one(commentId);
    if (Number(current.userId) !== user.sub) {
      throw new AppException("FORBIDDEN", "Only the author can delete this comment", HttpStatus.FORBIDDEN);
    }
    await this.dataSource.query(`update comments set status = 'DELETED', deleted_at = now(), updated_at = now() where id = ?`, [commentId]);
    await this.dataSource.query(`update posts set comment_count = greatest(comment_count - 1, 0), updated_at = now() where id = ?`, [Number(current.postId)]);
    return { message: "Comment deleted successfully" };
  }

  async adminList(query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const values: unknown[] = [];
    const filters: string[] = [`1=1`];
    if (query.postId) {
      values.push(Number(query.postId));
      filters.push(`c.post_id = ?`);
    }
    if (query.search) {
      values.push(`%${query.search.trim()}%`);
      filters.push(`(c.content like ? or u.nickname like ?)`);
      values.push(values[values.length - 1]);
    }
    if (query.status) {
      values.push(query.status.toUpperCase());
      filters.push(`c.status = ?`);
    }
    const where = `where ${filters.join(" and ")}`;
    const countRows = await this.dataSource.query(`select count(*) as count from comments c join users u on u.id = c.user_id ${where}`, values);
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    values.push(limit, offset);
    const rows = await this.dataSource.query(
      `select c.id, c.post_id as postId, c.content, c.status, c.created_at as createdAt,
              u.id as authorId, u.nickname as authorNickname
       from comments c
       join users u on u.id = c.user_id
       ${where}
       order by c.created_at desc
       limit ? offset ?`,
      values,
    );
    return { items: (rows as Record<string, unknown>[]).map((row) => this.mapCommentRow(row)), meta: pageMeta(page, limit, totalCount) };
  }

  async adminSetStatus(commentId: number, status: string) {
    const upperStatus = status.toUpperCase();
    if (!["ACTIVE", "HIDDEN", "DELETED"].includes(upperStatus)) {
      throw new AppException("INVALID_STATUS", "Invalid status", HttpStatus.BAD_REQUEST);
    }
    const current = await this.one(commentId);
    await this.dataSource.query(
      `update comments
       set status = ?, deleted_at = case when ? = 'DELETED' then now() else deleted_at end, updated_at = now()
       where id = ?`,
      [upperStatus, upperStatus, commentId],
    );
    if (upperStatus === "DELETED" && String(current.status) !== "DELETED") {
      await this.dataSource.query(`update posts set comment_count = greatest(comment_count - 1, 0), updated_at = now() where id = ?`, [Number(current.postId)]);
    }
    const updated = await this.one(commentId);
    return { id: updated.id, status: updated.status, updatedAt: updated.updatedAt };
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }

  private mapCommentRow(row: Record<string, unknown>): Record<string, unknown> {
    return {
      id: row.id,
      postId: row.postId,
      userId: row.userId,
      content: row.content,
      status: row.status,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
      author: {
        id: row.authorId,
        nickname: row.authorNickname,
      },
    };
  }
}
