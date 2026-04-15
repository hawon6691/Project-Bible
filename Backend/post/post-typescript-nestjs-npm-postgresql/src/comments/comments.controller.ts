import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../common/api-response";
import { CurrentUser } from "../common/current-user.decorator";
import type { CurrentUserPayload } from "../common/current-user.decorator";
import { Public } from "../common/public.decorator";
import { CommentsService } from "./comments.service";

@ApiTags("comments")
@Controller()
export class CommentsController {
  constructor(private readonly commentsService: CommentsService) {}

  @Public()
  @Get("api/v1/posts/:postId/comments")
  @ApiOperation({ summary: "List comments" })
  async list(@Param("postId", ParseIntPipe) postId: number, @Query() query: Record<string, string | undefined>) {
    const result = await this.commentsService.list(postId, query);
    return ok(result.items, result.meta);
  }

  @Post("api/v1/posts/:postId/comments")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create comment" })
  async create(@CurrentUser() user: CurrentUserPayload, @Param("postId", ParseIntPipe) postId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.commentsService.create(user, postId, body));
  }

  @Patch("api/v1/comments/:commentId")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update comment" })
  async update(@CurrentUser() user: CurrentUserPayload, @Param("commentId", ParseIntPipe) commentId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.commentsService.update(user, commentId, body));
  }

  @Delete("api/v1/comments/:commentId")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete comment" })
  async remove(@CurrentUser() user: CurrentUserPayload, @Param("commentId", ParseIntPipe) commentId: number) {
    return ok(await this.commentsService.remove(user, commentId));
  }
}
