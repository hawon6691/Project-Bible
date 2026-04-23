import { Module } from "@nestjs/common";
import { BoardsModule } from "../boards/boards.module";
import { CommentsModule } from "../comments/comments.module";
import { PostsModule } from "../posts/posts.module";
import { AdminController } from "./controller/admin.controller";
import { AdminService } from "./service/admin.service";

@Module({
  imports: [BoardsModule, PostsModule, CommentsModule],
  controllers: [AdminController],
  providers: [AdminService],
})
export class AdminModule {}
