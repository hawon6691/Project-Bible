import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../../common/api-response";
import { CurrentUser } from "../../common/current-user.decorator";
import type { CurrentUserPayload } from "../../common/current-user.decorator";
import { Public } from "../../common/public.decorator";
import { PostsService } from "../service/posts.service";

@ApiTags("posts")
@Controller("api/v1/posts")
export class PostsController {
  constructor(private readonly postsService: PostsService) {}

  @Public()
  @Get()
  @ApiOperation({ summary: "List posts" })
  async list(@Query() query: Record<string, string | undefined>) {
    const result = await this.postsService.list(query);
    return ok(result.items, result.meta);
  }

  @Public()
  @Get(":postId")
  @ApiOperation({ summary: "Get post detail" })
  async one(@Param("postId", ParseIntPipe) postId: number) {
    return ok(await this.postsService.one(postId));
  }

  @Post()
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create post" })
  async create(@CurrentUser() user: CurrentUserPayload, @Body() body: Record<string, unknown>) {
    return ok(await this.postsService.create(user, body));
  }

  @Patch(":postId")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update post" })
  async update(@CurrentUser() user: CurrentUserPayload, @Param("postId", ParseIntPipe) postId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.postsService.update(user, postId, body));
  }

  @Delete(":postId")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete post" })
  async remove(@CurrentUser() user: CurrentUserPayload, @Param("postId", ParseIntPipe) postId: number) {
    return ok(await this.postsService.remove(user, postId));
  }
}
