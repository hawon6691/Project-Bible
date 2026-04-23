import { Controller, Get } from "@nestjs/common";
import { ApiOperation, ApiTags } from "@nestjs/swagger";
import { ok } from "./api-response";
import { Public } from "./public.decorator";

@ApiTags("health")
@Controller("api/v1/health")
export class HealthController {
  @Public()
  @Get()
  @ApiOperation({ summary: "Health check" })
  health() {
    return ok({
      status: "UP",
      service: "post-typescript-nestjs-npm-postgresql",
      domain: "post",
    });
  }
}
