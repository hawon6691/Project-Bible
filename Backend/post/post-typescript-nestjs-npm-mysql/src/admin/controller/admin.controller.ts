import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query, UseGuards } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { BoardsService } from "../../boards/service/boards.service";
import { CommentsService } from "../../comments/service/comments.service";
import { ok } from "../../common/api-response";
import { CurrentUser } from "../../common/current-user.decorator";
import type { CurrentUserPayload } from "../../common/current-user.decorator";
import { AdminGuard } from "../../common/guards/admin.guard";
import { Public } from "../../common/public.decorator";
import { PostsService } from "../../posts/service/posts.service";
import { AdminService } from "../service/admin.service";

@ApiTags("admin")
@Controller("api/v1/admin")
export class AdminController {
  constructor(
    private readonly adminService: AdminService,
    private readonly boardsService: BoardsService,
    private readonly postsService: PostsService,
    private readonly commentsService: CommentsService,
  ) {}

  @Public()
  @Post("auth/login")
  @ApiOperation({ summary: "Admin login" })
  async login(@Body() body: Record<string, unknown>) {
    return ok(await this.adminService.login(body));
  }

  @Public()
  @Post("auth/refresh")
  @ApiOperation({ summary: "Refresh admin tokens" })
  async refresh(@Body() body: Record<string, unknown>) {
    return ok(await this.adminService.refresh(body));
  }

  @Post("auth/logout")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin logout" })
  async logout(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.adminService.logout(user));
  }

  @Get("me")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Current admin" })
  async me(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.adminService.me(user));
  }

  @Get("dashboard")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin dashboard" })
  async dashboard() {
    return ok(await this.adminService.dashboard());
  }

  @Post("boards")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create board" })
  async createBoard(@CurrentUser() user: CurrentUserPayload, @Body() body: Record<string, unknown>) {
    return ok(await this.boardsService.create(body, user.sub));
  }

  @Patch("boards/:boardId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update board" })
  async updateBoard(@Param("boardId", ParseIntPipe) boardId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.boardsService.update(boardId, body));
  }

  @Delete("boards/:boardId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete board" })
  async deleteBoard(@Param("boardId", ParseIntPipe) boardId: number) {
    return ok(await this.boardsService.remove(boardId));
  }

  @Get("posts")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin list posts" })
  async adminPosts(@Query() query: Record<string, string | undefined>) {
    const result = await this.postsService.adminList(query);
    return ok(result.items, result.meta);
  }

  @Get("posts/:postId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin post detail" })
  async adminPost(@Param("postId", ParseIntPipe) postId: number) {
    return ok(await this.postsService.one(postId, false));
  }

  @Patch("posts/:postId/status")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin update post status" })
  async updatePostStatus(@Param("postId", ParseIntPipe) postId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.postsService.adminSetStatus(postId, String(body.status ?? "")));
  }

  @Get("comments")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin list comments" })
  async adminComments(@Query() query: Record<string, string | undefined>) {
    const result = await this.commentsService.adminList(query);
    return ok(result.items, result.meta);
  }

  @Get("comments/:commentId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin comment detail" })
  async adminComment(@Param("commentId", ParseIntPipe) commentId: number) {
    return ok(await this.commentsService.one(commentId));
  }

  @Patch("comments/:commentId/status")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin update comment status" })
  async updateCommentStatus(@Param("commentId", ParseIntPipe) commentId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.commentsService.adminSetStatus(commentId, String(body.status ?? "")));
  }
}
