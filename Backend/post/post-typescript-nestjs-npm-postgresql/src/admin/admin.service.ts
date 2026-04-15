import { HttpStatus, Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import * as bcrypt from "bcryptjs";
import { createHash, randomUUID } from "node:crypto";
import { DataSource } from "typeorm";
import { AppException } from "../common/app.exception";
import { CurrentUserPayload } from "../common/current-user.decorator";
import { ensureString } from "../common/sql";

@Injectable()
export class AdminService {
  constructor(
    private readonly dataSource: DataSource,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
  ) {}

  async login(body: Record<string, unknown>): Promise<Record<string, unknown>> {
    const email = ensureString(body.email, "email").toLowerCase();
    const password = ensureString(body.password, "password");
    const rows = await this.dataSource.query(
      `select id, email, password_hash as "passwordHash", name, status, created_at as "createdAt"
       from admins where email = $1`,
      [email],
    );
    const admin = rows[0] as Record<string, unknown> | undefined;
    if (!admin || !(await bcrypt.compare(password, String(admin.passwordHash)))) {
      throw new AppException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
    const payload: CurrentUserPayload = {
      sub: Number(admin.id),
      email: String(admin.email),
      role: "ADMIN",
      subjectType: "admin",
    };
    const tokens = await this.issueAdminTokens(payload);
    return {
      ...tokens,
      admin: {
        id: admin.id,
        email: admin.email,
        name: admin.name,
        status: admin.status,
        createdAt: admin.createdAt,
      },
    };
  }

  async refresh(body: Record<string, unknown>) {
    const refreshToken = ensureString(body.refreshToken, "refreshToken");
    const tokenKey = this.hashToken(refreshToken);
    const rows = await this.dataSource.query(
      `select art.id, art.admin_id as "adminId", a.email
       from admin_refresh_tokens art
       join admins a on a.id = art.admin_id
       where art.token_key = $1 and art.revoked = false and art.expires_at > now()`,
      [tokenKey],
    );
    const tokenRow = rows[0] as { id: number; adminId: number; email: string } | undefined;
    if (!tokenRow) {
      throw new AppException("UNAUTHORIZED", "Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }
    await this.dataSource.query(`update admin_refresh_tokens set revoked = true, updated_at = now() where id = $1`, [tokenRow.id]);
    return this.issueAdminTokens({
      sub: tokenRow.adminId,
      email: tokenRow.email,
      role: "ADMIN",
      subjectType: "admin",
    });
  }

  async logout(user: CurrentUserPayload): Promise<Record<string, string>> {
    this.assertAdmin(user);
    await this.dataSource.query(
      `update admin_refresh_tokens set revoked = true, updated_at = now() where admin_id = $1 and revoked = false`,
      [user.sub],
    );
    return { message: "Admin logged out successfully" };
  }

  async me(user: CurrentUserPayload): Promise<Record<string, unknown>> {
    this.assertAdmin(user);
    const rows = await this.dataSource.query(
      `select id, email, name, status, created_at as "createdAt" from admins where id = $1`,
      [user.sub],
    );
    const admin = rows[0] as Record<string, unknown> | undefined;
    if (!admin) {
      throw new AppException("ADMIN_NOT_FOUND", "Admin not found", HttpStatus.NOT_FOUND);
    }
    return admin;
  }

  async dashboard(): Promise<Record<string, unknown>> {
    const queries = await Promise.all([
      this.scalar(`select count(*)::int from boards where status <> 'DELETED'`),
      this.scalar(`select count(*)::int from posts where status <> 'DELETED'`),
      this.scalar(`select count(*)::int from comments where status <> 'DELETED'`),
      this.scalar(`select count(*)::int from posts where status = 'HIDDEN'`),
      this.scalar(`select count(*)::int from comments where status = 'HIDDEN'`),
    ]);

    return {
      boardCount: queries[0],
      postCount: queries[1],
      commentCount: queries[2],
      hiddenPostCount: queries[3],
      hiddenCommentCount: queries[4],
    };
  }

  private async issueAdminTokens(payload: CurrentUserPayload) {
    const expiresIn = 1800;
    const accessToken = await this.jwtService.signAsync(payload, {
      secret: this.configService.get<string>("JWT_SECRET", "change-me-access"),
      expiresIn,
    });
    const refreshToken = randomUUID();
    await this.dataSource.query(
      `insert into admin_refresh_tokens (admin_id, token_key, expires_at, revoked)
       values ($1, $2, now() + interval '7 days', false)`,
      [payload.sub, this.hashToken(refreshToken)],
    );
    return { accessToken, refreshToken, expiresIn };
  }

  private async scalar(sql: string): Promise<number> {
    const rows = await this.dataSource.query(sql);
    const first = rows[0] as Record<string, unknown>;
    return Number(Object.values(first)[0] ?? 0);
  }

  private hashToken(value: string): string {
    return createHash("sha256").update(value).digest("hex");
  }

  private assertAdmin(user: CurrentUserPayload): void {
    if (user.subjectType !== "admin" || user.role !== "ADMIN") {
      throw new AppException("FORBIDDEN", "Admin access required", HttpStatus.FORBIDDEN);
    }
  }
}
