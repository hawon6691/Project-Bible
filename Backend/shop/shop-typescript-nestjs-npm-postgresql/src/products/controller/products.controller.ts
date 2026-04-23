import { Controller, Get, Param, ParseIntPipe, Query } from "@nestjs/common";
import { ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../../common/api-response";
import { Public } from "../../common/public.decorator";
import { ProductsService } from "../service/products.service";

@ApiTags("products")
@Controller("api/v1/products")
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @Public()
  @Get()
  @ApiOperation({ summary: "List products" })
  async list(@Query() query: Record<string, string | undefined>) {
    const result = await this.productsService.list(query);
    return ok(result.items, result.meta);
  }

  @Public()
  @Get(":productId")
  @ApiOperation({ summary: "Get product detail" })
  async one(@Param("productId", ParseIntPipe) productId: number) {
    return ok(await this.productsService.one(productId));
  }
}
