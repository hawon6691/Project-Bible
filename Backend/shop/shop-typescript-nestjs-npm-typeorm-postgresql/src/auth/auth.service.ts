import { HttpStatus, Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import * as bcrypt from "bcryptjs";
import { createHash, randomUUID } from "node:crypto";
import { DataSource } from "typeorm";
import { AppException } from "../common/app.exception";
import { CurrentUserPayload } from "../common/current-user.decorator";
import { ensureString, mapDbError } from "../common/sql";

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

@Injectable()
export class AuthService {
  constructor(
    private readonly dataSource: DataSource,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
  ) {}

  async signup(body: Record<string, unknown>): Promise<Record<string, unknown>> {
    const email = ensureString(body.email, "email").toLowerCase();
    const password = ensureString(body.password, "password");
    const name = ensureString(body.name, "name");
    const phone = ensureString(body.phone, "phone");
    const passwordHash = await bcrypt.hash(password, 10);

    try {
      const rows = await this.dataSource.query(
        `insert into users (email, password_hash, name, phone, status)
         values ($1, $2, $3, $4, 'ACTIVE')
         returning id, email, name, phone, status, created_at as "createdAt"`,
        [email, passwordHash, name, phone],
      );
      return rows[0] as Record<string, unknown>;
    } catch (error: unknown) {
      mapDbError(error, "DUPLICATE_EMAIL", "Email already exists");
    }
  }

  async login(body: Record<string, unknown>): Promise<Record<string, unknown>> {
    const email = ensureString(body.email, "email").toLowerCase();
    const password = ensureString(body.password, "password");
    const rows = await this.dataSource.query(
      `select id, email, password_hash as "passwordHash", name, phone, status, created_at as "createdAt"
       from users where email = $1 and deleted_at is null`,
      [email],
    );
    const user = rows[0] as Record<string, unknown> | undefined;
    if (!user || !(await bcrypt.compare(password, String(user.passwordHash)))) {
      throw new AppException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
    const payload: CurrentUserPayload = {
      sub: Number(user.id),
      email: String(user.email),
      role: "USER",
      subjectType: "user",
    };
    const tokens = await this.issueUserTokens(payload);
    return {
      ...tokens,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        phone: user.phone,
        status: user.status,
        createdAt: user.createdAt,
      },
    };
  }

  async refresh(body: Record<string, unknown>): Promise<AuthTokens> {
    const refreshToken = ensureString(body.refreshToken, "refreshToken");
    const tokenKey = this.hashToken(refreshToken);
    const rows = await this.dataSource.query(
      `select urt.id, urt.user_id as "userId", u.email
       from user_refresh_tokens urt
       join users u on u.id = urt.user_id
       where urt.token_key = $1 and urt.revoked = false and urt.expires_at > now() and u.deleted_at is null`,
      [tokenKey],
    );
    const tokenRow = rows[0] as { id: number; userId: number; email: string } | undefined;
    if (!tokenRow) {
      throw new AppException("UNAUTHORIZED", "Invalid refresh token", HttpStatus.UNAUTHORIZED);
    }
    await this.dataSource.query(`update user_refresh_tokens set revoked = true, updated_at = now() where id = $1`, [tokenRow.id]);
    return this.issueUserTokens({
      sub: tokenRow.userId,
      email: tokenRow.email,
      role: "USER",
      subjectType: "user",
    });
  }

  async logout(user: CurrentUserPayload): Promise<Record<string, string>> {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
    await this.dataSource.query(`update user_refresh_tokens set revoked = true, updated_at = now() where user_id = $1 and revoked = false`, [user.sub]);
    return { message: "Logged out successfully" };
  }

  private async issueUserTokens(payload: CurrentUserPayload): Promise<AuthTokens> {
    const expiresIn = 1800;
    const accessToken = await this.jwtService.signAsync(payload, {
      secret: this.configService.get<string>("JWT_SECRET", "change-me-access"),
      expiresIn,
    });
    const refreshToken = randomUUID();
    await this.dataSource.query(
      `insert into user_refresh_tokens (user_id, token_key, expires_at, revoked)
       values ($1, $2, now() + interval '7 days', false)`,
      [payload.sub, this.hashToken(refreshToken)],
    );
    return { accessToken, refreshToken, expiresIn };
  }

  private hashToken(value: string): string {
    return createHash("sha256").update(value).digest("hex");
  }
}
