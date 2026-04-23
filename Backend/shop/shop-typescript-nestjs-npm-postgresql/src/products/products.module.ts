import { Module } from "@nestjs/common";
import { ProductsController } from "./controller/products.controller";
import { ProductsService } from "./service/products.service";

@Module({
  controllers: [ProductsController],
  providers: [ProductsService],
  exports: [ProductsService],
})
export class ProductsModule {}
