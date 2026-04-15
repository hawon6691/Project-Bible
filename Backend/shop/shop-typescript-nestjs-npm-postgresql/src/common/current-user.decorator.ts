import { createParamDecorator, ExecutionContext } from "@nestjs/common";

export interface CurrentUserPayload {
  sub: number;
  email: string;
  role: "USER" | "ADMIN";
  subjectType: "user" | "admin";
}

export const CurrentUser = createParamDecorator(
  (_data: unknown, context: ExecutionContext): CurrentUserPayload => {
    const request = context.switchToHttp().getRequest<{ user: CurrentUserPayload }>();
    return request.user;
  },
);
