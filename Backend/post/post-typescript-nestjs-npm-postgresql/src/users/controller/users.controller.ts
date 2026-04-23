import { Body, Controller, Delete, Get, Patch } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../../common/api-response";
import { CurrentUser } from "../../common/current-user.decorator";
import type { CurrentUserPayload } from "../../common/current-user.decorator";
import { UsersService } from "../service/users.service";

@ApiTags("users")
@ApiBearerAuth()
@Controller("api/v1/users")
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get("me")
  @ApiOperation({ summary: "Current user" })
  async me(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.usersService.me(user));
  }

  @Patch("me")
  @ApiOperation({ summary: "Update current user" })
  async update(@CurrentUser() user: CurrentUserPayload, @Body() body: Record<string, unknown>) {
    return ok(await this.usersService.update(user, body));
  }

  @Delete("me")
  @ApiOperation({ summary: "Delete current user" })
  async remove(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.usersService.remove(user));
  }
}
