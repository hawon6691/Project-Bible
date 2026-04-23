import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../../common/api-response";
import { CurrentUser } from "../../common/current-user.decorator";
import type { CurrentUserPayload } from "../../common/current-user.decorator";
import { CartService } from "../service/cart.service";

@ApiTags("cart")
@ApiBearerAuth()
@Controller("api/v1/cart-items")
export class CartController {
  constructor(private readonly cartService: CartService) {}

  @Get()
  @ApiOperation({ summary: "List cart items" })
  async list(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.cartService.list(user));
  }

  @Post()
  @ApiOperation({ summary: "Create cart item" })
  async create(@CurrentUser() user: CurrentUserPayload, @Body() body: Record<string, unknown>) {
    return ok(await this.cartService.create(user, body));
  }

  @Patch(":cartItemId")
  @ApiOperation({ summary: "Update cart item" })
  async update(@CurrentUser() user: CurrentUserPayload, @Param("cartItemId", ParseIntPipe) cartItemId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.cartService.update(user, cartItemId, body));
  }

  @Delete(":cartItemId")
  @ApiOperation({ summary: "Delete cart item" })
  async remove(@CurrentUser() user: CurrentUserPayload, @Param("cartItemId", ParseIntPipe) cartItemId: number) {
    return ok(await this.cartService.remove(user, cartItemId));
  }

  @Delete()
  @ApiOperation({ summary: "Clear cart" })
  async clear(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.cartService.clear(user));
  }
}
