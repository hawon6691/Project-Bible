import { Module } from "@nestjs/common";
import { ConfigModule } from "@nestjs/config";
import { AppController } from "./app.controller";
import { AuthModule } from "./auth/auth.module";
import { UsersModule } from "./users/users.module";
import { AdminModule } from "./admin/admin.module";
import { CategoriesModule } from "./categories/categories.module";
import { ProductsModule } from "./products/products.module";
@Module({ imports: [ConfigModule.forRoot({ isGlobal: true, envFilePath: ".env" }), AuthModule, UsersModule, AdminModule, CategoriesModule, ProductsModule], controllers: [AppController] }) export class AppModule {}
