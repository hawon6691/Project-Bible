import { Controller, Get } from "@nestjs/common";
import { ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "./common/api-response";
@ApiTags("health") @Controller("api/v1/health") export class AppController { @ApiOperation({ summary: "Health check" }) @Get() health() { return ok({ status: "UP", service: "shop-typescript-nestjs-npm-knex-mysql", domain: "shop" }); } }
