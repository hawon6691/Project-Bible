import { Controller, Get, Param, ParseIntPipe, Query } from "@nestjs/common";
import { ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../common/api-response";
import { Public } from "../common/public.decorator";
import { BoardsService } from "./boards.service";

@ApiTags("boards")
@Controller("api/v1/boards")
export class BoardsController {
  constructor(private readonly boardsService: BoardsService) {}

  @Public()
  @Get()
  @ApiOperation({ summary: "List boards" })
  async list(@Query("status") status?: string) {
    return ok(await this.boardsService.list(status));
  }

  @Public()
  @Get(":boardId")
  @ApiOperation({ summary: "Get board detail" })
  async one(@Param("boardId", ParseIntPipe) boardId: number) {
    return ok(await this.boardsService.one(boardId));
  }
}
