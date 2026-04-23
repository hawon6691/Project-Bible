import { Module } from "@nestjs/common";
import { AddressesController } from "./controller/addresses.controller";
import { AddressesService } from "./service/addresses.service";

@Module({
  controllers: [AddressesController],
  providers: [AddressesService],
  exports: [AddressesService],
})
export class AddressesModule {}
