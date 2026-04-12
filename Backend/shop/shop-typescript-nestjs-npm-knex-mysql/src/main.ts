import { ValidationPipe } from "@nestjs/common";
import { NestFactory } from "@nestjs/core";
import { SwaggerModule, DocumentBuilder } from "@nestjs/swagger";
import { AppModule } from "./app.module";
import { HttpExceptionFilter } from "./common/http-exception.filter";
async function bootstrap() { const app = await NestFactory.create(AppModule, { cors: true }); app.useGlobalPipes(new ValidationPipe({ transform: true, whitelist: true })); app.useGlobalFilters(new HttpExceptionFilter()); const config = new DocumentBuilder().setTitle("shop-typescript-nestjs-npm-knex-mysql API").setDescription("Baseline API for shop").setVersion("v1").build(); const document = SwaggerModule.createDocument(app, config); SwaggerModule.setup("docs", app, document); await app.listen(Number(process.env.PORT ?? process.env.APP_PORT ?? 8000)); } bootstrap();
