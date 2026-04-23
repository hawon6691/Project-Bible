import { ValidationPipe } from "@nestjs/common";
import { NestFactory } from "@nestjs/core";
import { DocumentBuilder, SwaggerModule } from "@nestjs/swagger";
import { AppModule } from "./app.module";
import { HttpExceptionFilter } from "./common/http-exception.filter";

async function bootstrap(): Promise<void> {
  const app = await NestFactory.create(AppModule, { cors: true });
  app.useGlobalPipes(new ValidationPipe({ transform: true, whitelist: true }));
  app.useGlobalFilters(new HttpExceptionFilter());

  const config = new DocumentBuilder()
    .setTitle("shop-typescript-nestjs-npm-postgresql API")
    .setDescription("v1 API for shop service")
    .setVersion("v1")
    .addTag("auth")
    .addTag("users")
    .addTag("admin")
    .addTag("categories")
    .addTag("products")
    .addTag("cart")
    .addTag("addresses")
    .addTag("orders")
    .addTag("payments")
    .addTag("reviews")
    .build();

  SwaggerModule.setup("docs", app, SwaggerModule.createDocument(app, config));
  await app.listen(Number(process.env.PORT ?? process.env.APP_PORT ?? 8000));
}

bootstrap();
