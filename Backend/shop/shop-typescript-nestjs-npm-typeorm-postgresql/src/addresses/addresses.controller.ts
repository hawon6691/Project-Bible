import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../common/api-response";
import { CurrentUser } from "../common/current-user.decorator";
import type { CurrentUserPayload } from "../common/current-user.decorator";
import { AddressesService } from "./addresses.service";

@ApiTags("addresses")
@ApiBearerAuth()
@Controller("api/v1/addresses")
export class AddressesController {
  constructor(private readonly addressesService: AddressesService) {}

  @Get()
  @ApiOperation({ summary: "List addresses" })
  async list(@CurrentUser() user: CurrentUserPayload) {
    return ok(await this.addressesService.list(user));
  }

  @Post()
  @ApiOperation({ summary: "Create address" })
  async create(@CurrentUser() user: CurrentUserPayload, @Body() body: Record<string, unknown>) {
    return ok(await this.addressesService.create(user, body));
  }

  @Patch(":addressId")
  @ApiOperation({ summary: "Update address" })
  async update(@CurrentUser() user: CurrentUserPayload, @Param("addressId", ParseIntPipe) addressId: number, @Body() body: Record<string, unknown>) {
    return ok(await this.addressesService.update(user, addressId, body));
  }

  @Delete(":addressId")
  @ApiOperation({ summary: "Delete address" })
  async remove(@CurrentUser() user: CurrentUserPayload, @Param("addressId", ParseIntPipe) addressId: number) {
    return ok(await this.addressesService.remove(user, addressId));
  }
}
