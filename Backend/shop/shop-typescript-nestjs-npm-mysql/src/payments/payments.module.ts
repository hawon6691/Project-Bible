import { Module } from "@nestjs/common";
import { PaymentsController } from "./controller/payments.controller";
import { PaymentsService } from "./service/payments.service";

@Module({
  controllers: [PaymentsController],
  providers: [PaymentsService],
})
export class PaymentsModule {}
