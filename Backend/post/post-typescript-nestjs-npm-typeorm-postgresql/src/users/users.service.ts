import { HttpStatus, Injectable } from "@nestjs/common";
import * as bcrypt from "bcryptjs";
import { DataSource } from "typeorm";
import { AppException } from "../common/app.exception";
import { CurrentUserPayload } from "../common/current-user.decorator";

@Injectable()
export class UsersService {
  constructor(private readonly dataSource: DataSource) {}

  async me(user: CurrentUserPayload): Promise<Record<string, unknown>> {
    this.assertUser(user);
    const rows = await this.dataSource.query(
      `select id, email, nickname, status, created_at as "createdAt"
       from users where id = $1 and deleted_at is null`,
      [user.sub],
    );
    const current = rows[0] as Record<string, unknown> | undefined;
    if (!current) {
      throw new AppException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND);
    }
    return current;
  }

  async update(user: CurrentUserPayload, body: Record<string, unknown>): Promise<Record<string, unknown>> {
    this.assertUser(user);
    const current = await this.me(user);
    const nickname = typeof body.nickname === "string" && body.nickname.trim().length > 0 ? body.nickname.trim() : String(current.nickname);
    const passwordHash =
      typeof body.password === "string" && body.password.trim().length > 0
        ? await bcrypt.hash(body.password.trim(), 10)
        : null;

    const rows = await this.dataSource.query(
      `update users
       set nickname = $2, password_hash = coalesce($3, password_hash), updated_at = now()
       where id = $1
       returning id, email, nickname, status, updated_at as "updatedAt"`,
      [user.sub, nickname, passwordHash],
    );
    return rows[0] as Record<string, unknown>;
  }

  async remove(user: CurrentUserPayload): Promise<Record<string, string>> {
    this.assertUser(user);
    await this.dataSource.query(
      `update users set status = 'DELETED', deleted_at = now(), updated_at = now() where id = $1`,
      [user.sub],
    );
    await this.dataSource.query(
      `update user_refresh_tokens set revoked = true, updated_at = now() where user_id = $1 and revoked = false`,
      [user.sub],
    );
    return { message: "User deleted successfully" };
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
