import { Body, Controller, Get, Param, ParseIntPipe, Post, Query } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../common/api-response";
import { CurrentUser } from "../common/current-user.decorator";
import type { CurrentUserPayload } from "../common/current-user.decorator";
import { OrdersService } from "./orders.service";

@ApiTags("orders")
@ApiBearerAuth()
@Controller("api/v1/orders")
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  @Post()
  @ApiOperation({ summary: "Create order" })
  async create(@CurrentUser() user: CurrentUserPayload, @Body() body: Record<string, unknown>) {
    return ok(await this.ordersService.create(user, body));
  }

  @Get()
  @ApiOperation({ summary: "List my orders" })
  async list(@CurrentUser() user: CurrentUserPayload, @Query() query: Record<string, string | undefined>) {
    const result = await this.ordersService.list(user, query);
    return ok(result.items, result.meta);
  }

  @Get(":orderId")
  @ApiOperation({ summary: "Get my order detail" })
  async one(@CurrentUser() user: CurrentUserPayload, @Param("orderId", ParseIntPipe) orderId: number) {
    return ok(await this.ordersService.one(orderId, user));
  }

  @Post(":orderId/cancel")
  @ApiOperation({ summary: "Cancel order" })
  async cancel(@CurrentUser() user: CurrentUserPayload, @Param("orderId", ParseIntPipe) orderId: number) {
    return ok(await this.ordersService.cancel(user, orderId));
  }
}
