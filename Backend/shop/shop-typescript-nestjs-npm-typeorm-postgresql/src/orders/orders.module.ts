import { Module } from "@nestjs/common";
import { AddressesModule } from "../addresses/addresses.module";
import { CartModule } from "../cart/cart.module";
import { OrdersController } from "./orders.controller";
import { OrdersService } from "./orders.service";

@Module({
  imports: [AddressesModule, CartModule],
  controllers: [OrdersController],
  providers: [OrdersService],
  exports: [OrdersService],
})
export class OrdersModule {}
