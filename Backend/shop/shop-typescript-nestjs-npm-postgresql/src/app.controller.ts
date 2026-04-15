import { Controller, Get } from "@nestjs/common";
import { ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "./common/api-response";
import { Public } from "./common/public.decorator";

@ApiTags("health")
@Controller("api/v1/health")
export class AppController {
  @Public()
  @Get()
  @ApiOperation({ summary: "Health check" })
  health() {
    return ok({
      status: "UP",
      service: "shop-typescript-nestjs-npm-postgresql",
      domain: "shop",
    });
  }
}
