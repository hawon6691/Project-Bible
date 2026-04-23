import { Module } from "@nestjs/common";
import { CategoriesController } from "./controller/categories.controller";
import { CategoriesService } from "./service/categories.service";

@Module({
  controllers: [CategoriesController],
  providers: [CategoriesService],
  exports: [CategoriesService],
})
export class CategoriesModule {}
