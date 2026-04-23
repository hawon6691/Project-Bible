import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../../common/api-response";
import { CurrentUser } from "../../common/current-user.decorator";
import type { CurrentUserPayload } from "../../common/current-user.decorator";
import { Public } from "../../common/public.decorator";
import { ReviewsService } from "../service/reviews.service";

@ApiTags("reviews")
@Controller()
export class ReviewsController {
  constructor(private readonly reviewsService: ReviewsService) {}

  @Public()
  @Get("api/v1/products/:productId/reviews")
  @ApiOperation({ summary: "List product reviews" })
  async list(@Param("productId", ParseIntPipe) productId: number, @Query() query: Record<string, string | undefined>) {
    const result = await this.reviewsService.list(productId, query);
    return ok(result.items, result.meta);
  }

  @Post("api/v1/order-items/:orderItemId/reviews")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create review" })
  async create(@CurrentUser() user: CurrentUserPayload, @Param("orderItemId", ParseIntPipe) orderItemId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.reviewsService.create(user, orderItemId, body));
  }

  @Patch("api/v1/reviews/:reviewId")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update review" })
  async update(@CurrentUser() user: CurrentUserPayload, @Param("reviewId", ParseIntPipe) reviewId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.reviewsService.update(user, reviewId, body));
  }

  @Delete("api/v1/reviews/:reviewId")
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete review" })
  async remove(@CurrentUser() user: CurrentUserPayload, @Param("reviewId", ParseIntPipe) reviewId: number) {
    return ok(await this.reviewsService.remove(user, reviewId));
  }
}
