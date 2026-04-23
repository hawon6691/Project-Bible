import { CanActivate, ExecutionContext, HttpStatus, Injectable } from "@nestjs/common";
import { AppException } from "../app.exception";
import { CurrentUserPayload } from "../current-user.decorator";

@Injectable()
export class AdminGuard implements CanActivate {
  canActivate(context: ExecutionContext): boolean {
    const request = context.switchToHttp().getRequest<{ user?: CurrentUserPayload }>();
    if (request.user?.role !== "ADMIN" || request.user.subjectType !== "admin") {
      throw new AppException("FORBIDDEN", "Admin access required", HttpStatus.FORBIDDEN);
    }
    return true;
  }
}
