import { HttpStatus, Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import * as bcrypt from "bcryptjs";
import { createHash, randomUUID } from "node:crypto";
import { DataSource } from "typeorm";
import { AppException } from "../../common/app.exception";
import { CurrentUserPayload } from "../../common/current-user.decorator";
import { ensureString } from "../../common/sql";

@Injectable()
export class AdminService {
  constructor(
    private readonly dataSource: DataSource,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
  ) {}

  async login(body: Record<string, unknown>) {
    const email = ensureString(body.email, "email").toLowerCase();
    const password = ensureString(body.password, "password");
    const rows = await this.dataSource.query(
      `select id, email, password_hash as passwordHash, name, status, created_at as createdAt
       from admins where email = ?`,
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
      admin: { id: admin.id, email: admin.email, name: admin.name, status: admin.status, createdAt: admin.createdAt },
    };
  }

  async refresh(body: Record<string, unknown>) {
    const refreshToken = ensureString(body.refreshToken, "refreshToken");
    const tokenKey = this.hashToken(refreshToken);
    const rows = await this.dataSource.query(
      `select art.id, art.admin_id as adminId, a.email
       from admin_refresh_tokens art
       join admins a on a.id = art.admin_id
       where art.token_key = ? and art.revoked = false and art.expires_at > now()`,
      [tokenKey],
    );
    const tokenRow = rows[0] as { id: number; adminId: number; email: string } | undefined;
    if (!tokenRow) {
      throw new AppException("UNAUTHORIZED", "Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }
    await this.dataSource.query(`update admin_refresh_tokens set revoked = true, updated_at = now() where id = ?`, [tokenRow.id]);
    return this.issueAdminTokens({
      sub: tokenRow.adminId,
      email: tokenRow.email,
      role: "ADMIN",
      subjectType: "admin",
    });
  }

  async logout(user: CurrentUserPayload) {
    this.assertAdmin(user);
    await this.dataSource.query(`update admin_refresh_tokens set revoked = true, updated_at = now() where admin_id = ? and revoked = false`, [user.sub]);
    return { message: "Admin logged out successfully" };
  }

  async me(user: CurrentUserPayload) {
    this.assertAdmin(user);
    const rows = await this.dataSource.query(`select id, email, name, status, created_at as createdAt from admins where id = ?`, [user.sub]);
    const admin = rows[0] as Record<string, unknown> | undefined;
    if (!admin) {
      throw new AppException("ADMIN_NOT_FOUND", "Admin not found", HttpStatus.NOT_FOUND);
    }
    return admin;
  }

  async dashboard() {
    const [pending, paid, shipping, cancelled, activeProducts, hiddenProducts, deletedProducts, activeUsers, todayUsers, activeReviews, hiddenReviews, deletedReviews] = await Promise.all([
      this.scalar(`select count(*) as count from orders where order_status = 'PENDING'`),
      this.scalar(`select count(*) as count from orders where order_status = 'PAID'`),
      this.scalar(`select count(*) as count from orders where order_status = 'SHIPPING'`),
      this.scalar(`select count(*) as count from orders where order_status = 'CANCELLED'`),
      this.scalar(`select count(*) as count from products where status = 'ACTIVE'`),
      this.scalar(`select count(*) as count from products where status = 'HIDDEN'`),
      this.scalar(`select count(*) as count from products where status = 'DELETED'`),
      this.scalar(`select count(*) as count from users where status = 'ACTIVE' and deleted_at is null`),
      this.scalar(`select count(*) as count from users where date(created_at) = current_date() and deleted_at is null`),
      this.scalar(`select count(*) as count from reviews where status = 'ACTIVE' and deleted_at is null`),
      this.scalar(`select count(*) as count from reviews where status = 'HIDDEN' and deleted_at is null`),
      this.scalar(`select count(*) as count from reviews where status = 'DELETED'`),
    ]);

    return {
      orderSummary: { pending, paid, shipping, cancelled },
      productSummary: { active: activeProducts, hidden: hiddenProducts, deleted: deletedProducts },
      userSummary: { active: activeUsers, newUsersToday: todayUsers },
      reviewSummary: { active: activeReviews, hidden: hiddenReviews, deleted: deletedReviews },
    };
  }

  private async issueAdminTokens(payload: CurrentUserPayload) {
    const expiresIn = 1800;
    const accessToken = await this.jwtService.signAsync(payload, {
      secret: this.configService.get<string>("JWT_SECRET", "change-me-access"),
      expiresIn,
    });
    const refreshToken = randomUUID();
    await this.dataSource.query(`insert into admin_refresh_tokens (admin_id, token_key, expires_at, revoked) values (?, ?, date_add(now(), interval 7 day), false)`, [payload.sub, this.hashToken(refreshToken)]);
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
