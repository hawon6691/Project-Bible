import { Controller, Delete, Param, ParseIntPipe, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../common/api-response";
import { CurrentUser } from "../common/current-user.decorator";
import type { CurrentUserPayload } from "../common/current-user.decorator";
import { LikesService } from "./likes.service";

@ApiTags("likes")
@ApiBearerAuth()
@Controller("api/v1/posts/:postId/likes")
export class LikesController {
  constructor(private readonly likesService: LikesService) {}

  @Post()
  @ApiOperation({ summary: "Like post" })
  async like(@CurrentUser() user: CurrentUserPayload, @Param("postId", ParseIntPipe) postId: number) {
    return ok(await this.likesService.like(user, postId));
  }

  @Delete()
  @ApiOperation({ summary: "Unlike post" })
  async unlike(@CurrentUser() user: CurrentUserPayload, @Param("postId", ParseIntPipe) postId: number) {
    return ok(await this.likesService.unlike(user, postId));
  }
}
