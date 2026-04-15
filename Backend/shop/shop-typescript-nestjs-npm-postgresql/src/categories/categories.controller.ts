import { Controller, Get, Param, ParseIntPipe, Query } from "@nestjs/common";
import { ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../common/api-response";
import { Public } from "../common/public.decorator";
import { CategoriesService } from "./categories.service";

@ApiTags("categories")
@Controller("api/v1/categories")
export class CategoriesController {
  constructor(private readonly categoriesService: CategoriesService) {}

  @Public()
  @Get()
  @ApiOperation({ summary: "List categories" })
  async list(@Query("status") status?: string) {
    return ok(await this.categoriesService.list(status));
  }

  @Public()
  @Get(":categoryId")
  @ApiOperation({ summary: "Get category detail" })
  async one(@Param("categoryId", ParseIntPipe) categoryId: number) {
    return ok(await this.categoriesService.one(categoryId));
  }
}
