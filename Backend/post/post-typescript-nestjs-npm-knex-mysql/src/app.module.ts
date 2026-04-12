import { Module } from "@nestjs/common";
import { ConfigModule } from "@nestjs/config";
import { AppController } from "./app.controller";
import { AuthModule } from "./auth/auth.module";
import { UsersModule } from "./users/users.module";
import { AdminModule } from "./admin/admin.module";
import { BoardsModule } from "./boards/boards.module";
import { PostsModule } from "./posts/posts.module";
@Module({ imports: [ConfigModule.forRoot({ isGlobal: true, envFilePath: ".env" }), AuthModule, UsersModule, AdminModule, BoardsModule, PostsModule], controllers: [AppController] }) export class AppModule {}
