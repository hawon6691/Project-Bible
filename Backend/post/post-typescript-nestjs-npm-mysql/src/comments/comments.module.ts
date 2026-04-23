import { Module } from "@nestjs/common";
import { CommentsController } from "./controller/comments.controller";
import { CommentsService } from "./service/comments.service";

@Module({
  controllers: [CommentsController],
  providers: [CommentsService],
  exports: [CommentsService],
})
export class CommentsModule {}
