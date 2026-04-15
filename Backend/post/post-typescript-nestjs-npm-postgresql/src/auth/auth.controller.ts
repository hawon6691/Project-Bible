import { Body, Controller, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../common/api-response";
import { CurrentUser } from "../common/current-user.decorator";
import type { CurrentUserPayload } from "../common/current-user.decorator";
import { Public } from "../common/public.decorator";
import { AuthService } from "./auth.service";

@ApiTags("auth")
@Controller("api/v1/auth")
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @Post("signup")
  @ApiOperation({ summary: "User signup" })
  async signup(@Body() body: Record<string, unknown>) {
    return ok(await this.authService.signup(body));
  }

  @Public()
  @Post("login")
  @ApiOperation({ summary: "User login" })
  async login(@Body() body: Record<string, unknown>) {
    return ok(await this.authService.login(body));
  }

  @Public()
  @Post("refresh")
  @ApiOperation({ summary: "Refresh user tokens" })
  async refresh(@Body() body: Record<string, unknown>) {
    return ok(await this.authService.refresh(body));
  }

  @Post("logout")
  @ApiBearerAuth()
  @ApiOperation({ summary: "User logout" })
  async logout(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.authService.logout(user));
  }
}
