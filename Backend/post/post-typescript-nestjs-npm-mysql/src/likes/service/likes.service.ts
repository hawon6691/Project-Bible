import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { AppException } from "../../common/app.exception";
import { CurrentUserPayload } from "../../common/current-user.decorator";
import { mapDbError } from "../../common/sql";

@Injectable()
export class LikesService {
  constructor(private readonly dataSource: DataSource) {}

  async like(user: CurrentUserPayload, postId: number) {
    this.assertUser(user);
    const postRows = await this.dataSource.query(`select id from posts where id = ? and status = 'ACTIVE'`, [postId]);
    if (postRows.length === 0) {
      throw new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND);
    }
    try {
      await this.dataSource.query(`insert into post_likes (post_id, user_id) values (?, ?)`, [postId, user.sub]);
      await this.dataSource.query(`update posts set like_count = like_count + 1, updated_at = now() where id = ?`, [postId]);
    } catch (error: unknown) {
      mapDbError(error, "DUPLICATE_LIKE", "Post already liked");
    }
    return this.summary(postId, true);
  }

  async unlike(user: CurrentUserPayload, postId: number) {
    this.assertUser(user);
    const existing = await this.dataSource.query(`select id from post_likes where post_id = ? and user_id = ?`, [postId, user.sub]);
    if (existing.length > 0) {
      await this.dataSource.query(`delete from post_likes where post_id = ? and user_id = ?`, [postId, user.sub]);
      await this.dataSource.query(`update posts set like_count = greatest(like_count - 1, 0), updated_at = now() where id = ?`, [postId]);
    }
    return this.summary(postId, false);
  }

  private async summary(postId: number, liked: boolean) {
    const rows = await this.dataSource.query(`select like_count as likeCount from posts where id = ?`, [postId]);
    if (rows.length === 0) {
      throw new AppException("POST_NOT_FOUND", "Post not found", HttpStatus.NOT_FOUND);
    }
    return { postId, liked, likeCount: Number((rows[0] as { likeCount: number }).likeCount) };
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
