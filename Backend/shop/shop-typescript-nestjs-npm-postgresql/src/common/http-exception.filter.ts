import { ArgumentsHost, Catch, ExceptionFilter, HttpException, HttpStatus } from "@nestjs/common";
import { AppException } from "./app.exception";
import { fail } from "./api-response";

@Catch()
export class HttpExceptionFilter implements ExceptionFilter {
  catch(exception: unknown, host: ArgumentsHost): void {
    const response = host.switchToHttp().getResponse();
    if (exception instanceof AppException) {
      response.status(exception.getStatus()).json(fail(exception.code, exception.message, exception.details));
      return;
    }
    if (exception instanceof HttpException) {
      const status = exception.getStatus();
      response.status(status).json(fail(status === HttpStatus.BAD_REQUEST ? "VALIDATION_ERROR" : "HTTP_ERROR", exception.message));
      return;
    }
    const message = exception instanceof Error ? exception.message : "Unexpected error";
    response.status(HttpStatus.INTERNAL_SERVER_ERROR).json(fail("INTERNAL_SERVER_ERROR", message));
  }
}
