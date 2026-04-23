import { CanActivate, ExecutionContext, HttpStatus, Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { JwtService } from "@nestjs/jwt";
import { Reflector } from "@nestjs/core";
import { AppException } from "../app.exception";
import { CurrentUserPayload } from "../current-user.decorator";
import { IS_PUBLIC_KEY } from "../public.decorator";

@Injectable()
export class JwtAuthGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) {
      return true;
    }

    const request = context.switchToHttp().getRequest<{ headers: Record<string, string | undefined>; user?: CurrentUserPayload }>();
    const authHeader = request.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      throw new AppException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
    }

    try {
      request.user = await this.jwtService.verifyAsync<CurrentUserPayload>(authHeader.slice(7), {
        secret: this.configService.get<string>("JWT_SECRET", "change-me-access"),
      });
      return true;
    } catch {
      throw new AppException("UNAUTHORIZED", "Invalid access token", HttpStatus.UNAUTHORIZED);
    }
  }
}
