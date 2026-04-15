import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query, UseGuards } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { CategoriesService } from "../categories/categories.service";
import { ok } from "../common/api-response";
import { CurrentUser } from "../common/current-user.decorator";
import type { CurrentUserPayload } from "../common/current-user.decorator";
import { AdminGuard } from "../common/guards/admin.guard";
import { Public } from "../common/public.decorator";
import { OrdersService } from "../orders/orders.service";
import { ProductsService } from "../products/products.service";
import { ReviewsService } from "../reviews/reviews.service";
import { AdminService } from "./admin.service";

@ApiTags("admin")
@Controller("api/v1/admin")
export class AdminController {
  constructor(
    private readonly adminService: AdminService,
    private readonly categoriesService: CategoriesService,
    private readonly productsService: ProductsService,
    private readonly ordersService: OrdersService,
    private readonly reviewsService: ReviewsService,
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

  @Post("categories")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create category" })
  async createCategory(@Body() body: Record<string, unknown>) {
    return ok(await this.categoriesService.create(body));
  }

  @Patch("categories/:categoryId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update category" })
  async updateCategory(@Param("categoryId", ParseIntPipe) categoryId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.categoriesService.update(categoryId, body));
  }

  @Delete("categories/:categoryId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete category" })
  async deleteCategory(@Param("categoryId", ParseIntPipe) categoryId: number) {
    return ok(await this.categoriesService.remove(categoryId));
  }

  @Post("products")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create product" })
  async createProduct(@Body() body: Record<string, unknown>) {
    return ok(await this.productsService.create(body));
  }

  @Patch("products/:productId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update product" })
  async updateProduct(@Param("productId", ParseIntPipe) productId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.productsService.update(productId, body));
  }

  @Delete("products/:productId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete product" })
  async deleteProduct(@Param("productId", ParseIntPipe) productId: number) {
    return ok(await this.productsService.remove(productId));
  }

  @Post("products/:productId/options")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create product option" })
  async createOption(@Param("productId", ParseIntPipe) productId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.productsService.createOption(productId, body));
  }

  @Patch("product-options/:optionId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update product option" })
  async updateOption(@Param("optionId", ParseIntPipe) optionId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.productsService.updateOption(optionId, body));
  }

  @Delete("product-options/:optionId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete product option" })
  async deleteOption(@Param("optionId", ParseIntPipe) optionId: number) {
    return ok(await this.productsService.removeOption(optionId));
  }

  @Post("products/:productId/images")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Create product image" })
  async createImage(@Param("productId", ParseIntPipe) productId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.productsService.createImage(productId, body));
  }

  @Patch("product-images/:imageId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Update product image" })
  async updateImage(@Param("imageId", ParseIntPipe) imageId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.productsService.updateImage(imageId, body));
  }

  @Delete("product-images/:imageId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Delete product image" })
  async deleteImage(@Param("imageId", ParseIntPipe) imageId: number) {
    return ok(await this.productsService.removeImage(imageId));
  }

  @Get("orders")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin order list" })
  async adminOrders(@Query() query: Record<string, string | undefined>) {
    const result = await this.ordersService.adminList(query);
    return ok(result.items, result.meta);
  }

  @Get("orders/:orderId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin order detail" })
  async adminOrder(@Param("orderId", ParseIntPipe) orderId: number) {
    return ok(await this.ordersService.one(orderId));
  }

  @Patch("orders/:orderId/status")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin update order status" })
  async updateOrderStatus(@Param("orderId", ParseIntPipe) orderId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.ordersService.adminSetStatus(orderId, String(body.orderStatus ?? "")));
  }

  @Get("reviews")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin review list" })
  async adminReviews(@Query() query: Record<string, string | undefined>) {
    const result = await this.reviewsService.adminList(query);
    return ok(result.items, result.meta);
  }

  @Get("reviews/:reviewId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin review detail" })
  async adminReview(@Param("reviewId", ParseIntPipe) reviewId: number) {
    return ok(await this.reviewsService.one(reviewId));
  }

  @Delete("reviews/:reviewId")
  @UseGuards(AdminGuard)
  @ApiBearerAuth()
  @ApiOperation({ summary: "Admin delete review" })
  async deleteReview(@Param("reviewId", ParseIntPipe) reviewId: number) {
    return ok(await this.reviewsService.adminRemove(reviewId));
  }
}
