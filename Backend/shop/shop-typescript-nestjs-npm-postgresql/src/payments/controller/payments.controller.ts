import { Body, Controller, Get, Param, ParseIntPipe, Post } from "@nestjs/common";
import { ApiBearerAuth, ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "../../common/api-response";
import { CurrentUser } from "../../common/current-user.decorator";
import type { CurrentUserPayload } from "../../common/current-user.decorator";
import { PaymentsService } from "../service/payments.service";

@ApiTags("payments")
@ApiBearerAuth()
@Controller("api/v1/payments")
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  @Post()
  @ApiOperation({ summary: "Create payment" })
  async create(@CurrentUser() user: CurrentUserPayload, @Body() body: Record<string, unknown>) {
    return ok(await this.paymentsService.create(user, body));
  }

  @Get(":paymentId")
  @ApiOperation({ summary: "Get payment detail" })
  async one(@CurrentUser() user: CurrentUserPayload, @Param("paymentId", ParseIntPipe) paymentId: number) {
    return ok(await this.paymentsService.one(user, paymentId));
  }

  @Post(":paymentId/refund")
  @ApiOperation({ summary: "Refund payment" })
  async refund(@CurrentUser() user: CurrentUserPayload, @Param("paymentId", ParseIntPipe) paymentId: number) {
    return ok(await this.paymentsService.refund(user, paymentId));
  }
}
