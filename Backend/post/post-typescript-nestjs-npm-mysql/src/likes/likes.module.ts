import { Module } from "@nestjs/common";
import { LikesController } from "./controller/likes.controller";
import { LikesService } from "./service/likes.service";

@Module({
  controllers: [LikesController],
  providers: [LikesService],
})
export class LikesModule {}
