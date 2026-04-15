import { Module } from "@nestjs/common";
import { CategoriesModule } from "../categories/categories.module";
import { OrdersModule } from "../orders/orders.module";
import { ProductsModule } from "../products/products.module";
import { ReviewsModule } from "../reviews/reviews.module";
import { AdminController } from "./admin.controller";
import { AdminService } from "./admin.service";

@Module({
  imports: [CategoriesModule, ProductsModule, OrdersModule, ReviewsModule],
  controllers: [AdminController],
  providers: [AdminService],
})
export class AdminModule {}
