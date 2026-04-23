import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { pageMeta } from "../../common/api-response";
import { AppException } from "../../common/app.exception";
import { CurrentUserPayload } from "../../common/current-user.decorator";
import { ensureNumber, ensureString, readPage } from "../../common/sql";

@Injectable()
export class PostsService {
  constructor(private readonly dataSource: DataSource) {}

  async list(query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const values: unknown[] = [];
    const filters: string[] = [`p.status <> 'DELETED'`, `p.status = 'ACTIVE'`];

    if (query.boardId) {
      values.push(Number(query.boardId));
      filters.push(`p.board_id = $${values.length}`);
    }
    if (query.search) {
      values.push(`%${query.search.trim()}%`);
      filters.push(`(p.title ilike $${values.length} or p.content ilike $${values.length})`);
    }
    if (query.status) {
      filters.pop();
      values.push(query.status.toUpperCase());
      filters.push(`p.status = $${values.length}`);
    }

    const where = `where ${filters.join(" and ")}`;
    const countRows = await this.dataSource.query(`select count(*)::int as count from posts p ${where}`, values);
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    values.push(limit, offset);
    const rows = await this.dataSource.query(
      `select p.id,
              p.board_id as "boardId",
              b.name as "boardName",
              p.title,
              p.view_count as "viewCount",
              p.like_count as "likeCount",
              p.comment_count as "commentCount",
              p.status,
              p.created_at as "createdAt",
              json_build_object('id', u.id, 'nickname', u.nickname) as author
       from posts p
       join boards b on b.id = p.board_id
       join users u on u.id = p.user_id
       ${where}
       order by ${this.resolveSort(query.sort)}
       limit $${values.length - 1} offset $${values.length}`,
      values,
    );
    return { items: rows as Record<string, unknown>[], meta: pageMeta(page, limit, totalCount) };
  }

  async one(postId: number, incrementView = true): Promise<Record<string, unknown>> {
    if (incrementView) {
      await this.dataSource.query(`update posts set view_count = view_count + 1, updated_at = now() where id = $1 and status <> 'DELETED'`, [postId]);
    }
    const rows = await this.dataSource.query(
      `select p.id,
              p.board_id as "boardId",
              b.name as "boardName",
              p.user_id as "userId",
              p.title,
              p.content,
              p.view_count as "viewCount",
              p.like_count as "likeCount",
              p.comment_count as "commentCount",
              p.status,
              p.created_at as "createdAt",
              p.updated_at as "updatedAt",
              json_build_object('id', u.id, 'nickname', u.nickname) as author
       from posts p
       join boards b on b.id = p.board_id
       join users u on u.id = p.user_id
       where p.id = $1 and p.status <> 'DELETED'`,
      [postId],
    );
    const post = rows[0] as Record<string, unknown> | undefined;
    if (!post) {
      throw new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND);
    }
    return post;
  }

  async create(user: CurrentUserPayload, body: Record<string, unknown>): Promise<Record<string, unknown>> {
    this.assertUser(user);
    const boardId = ensureNumber(body.boardId, "boardId");
    const title = ensureString(body.title, "title");
    const content = ensureString(body.content, "content");
    const board = await this.dataSource.query(`select id from boards where id = $1 and status = 'ACTIVE'`, [boardId]);
    if (board.length === 0) {
      throw new AppException("BOARD_NOT_FOUND", "Board not found", HttpStatus.NOT_FOUND);
    }
    const rows = await this.dataSource.query(
      `insert into posts (board_id, user_id, title, content, view_count, like_count, comment_count, status)
       values ($1, $2, $3, $4, 0, 0, 0, 'ACTIVE')
       returning id`,
      [boardId, user.sub, title, content],
    );
    return this.one(Number((rows[0] as { id: number }).id), false);
  }

  async update(user: CurrentUserPayload, postId: number, body: Record<string, unknown>): Promise<Record<string, unknown>> {
    this.assertUser(user);
    const current = await this.one(postId, false);
    if (Number(current.userId) !== user.sub) {
      throw new AppException("FORBIDDEN", "Only the author can update this post", HttpStatus.FORBIDDEN);
    }
    const title = typeof body.title === "string" && body.title.trim() ? body.title.trim() : String(current.title);
    const content = typeof body.content === "string" && body.content.trim() ? body.content.trim() : String(current.content);
    await this.dataSource.query(`update posts set title = $2, content = $3, updated_at = now() where id = $1`, [postId, title, content]);
    return this.one(postId, false);
  }

  async remove(user: CurrentUserPayload, postId: number): Promise<Record<string, string>> {
    this.assertUser(user);
    const current = await this.one(postId, false);
    if (Number(current.userId) !== user.sub) {
      throw new AppException("FORBIDDEN", "Only the author can delete this post", HttpStatus.FORBIDDEN);
    }
    await this.dataSource.query(`update posts set status = 'DELETED', deleted_at = now(), updated_at = now() where id = $1`, [postId]);
    return { message: "Post deleted successfully" };
  }

  async adminList(query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const values: unknown[] = [];
    const filters: string[] = [`1=1`];
    if (query.boardId) {
      values.push(Number(query.boardId));
      filters.push(`p.board_id = $${values.length}`);
    }
    if (query.search) {
      values.push(`%${query.search.trim()}%`);
      filters.push(`(p.title ilike $${values.length} or p.content ilike $${values.length} or u.nickname ilike $${values.length})`);
    }
    if (query.status) {
      values.push(query.status.toUpperCase());
      filters.push(`p.status = $${values.length}`);
    }
    const where = `where ${filters.join(" and ")}`;
    const countRows = await this.dataSource.query(`select count(*)::int as count from posts p join users u on u.id = p.user_id ${where}`, values);
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    values.push(limit, offset);
    const rows = await this.dataSource.query(
      `select p.id,
              p.board_id as "boardId",
              b.name as "boardName",
              p.title,
              p.status,
              p.created_at as "createdAt",
              json_build_object('id', u.id, 'nickname', u.nickname) as author
       from posts p
       join boards b on b.id = p.board_id
       join users u on u.id = p.user_id
       ${where}
       order by p.created_at desc
       limit $${values.length - 1} offset $${values.length}`,
      values,
    );
    return { items: rows as Record<string, unknown>[], meta: pageMeta(page, limit, totalCount) };
  }

  async adminSetStatus(postId: number, status: string): Promise<Record<string, unknown>> {
    const upperStatus = status.toUpperCase();
    if (!["ACTIVE", "HIDDEN", "DELETED"].includes(upperStatus)) {
      throw new AppException("INVALID_STATUS", "Invalid status", HttpStatus.BAD_REQUEST);
    }
    await this.one(postId, false);
    const rows = await this.dataSource.query(
      `update posts
       set status = $2, deleted_at = case when $3 = 'DELETED' then now() else deleted_at end, updated_at = now()
       where id = $1
       returning id, status, updated_at as "updatedAt"`,
      [postId, upperStatus, upperStatus],
    );
    return rows[0] as Record<string, unknown>;
  }

  private resolveSort(sort?: string): string {
    switch ((sort ?? "latest").toLowerCase()) {
      case "view_count":
        return `p.view_count desc, p.id desc`;
      case "like_count":
        return `p.like_count desc, p.id desc`;
      default:
        return `p.created_at desc, p.id desc`;
    }
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
