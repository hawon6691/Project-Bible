import { Module } from "@nestjs/common";
import { ReviewsController } from "./controller/reviews.controller";
import { ReviewsService } from "./service/reviews.service";

@Module({
  controllers: [ReviewsController],
  providers: [ReviewsService],
  exports: [ReviewsService],
})
export class ReviewsModule {}
