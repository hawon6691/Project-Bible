import { HttpStatus } from "@nestjs/common";
import { QueryFailedError } from "typeorm";
import { AppException } from "./app.exception";

export interface PageQuery {
  page: number;
  limit: number;
  offset: number;
}

export function readPage(pageValue?: string, limitValue?: string): PageQuery {
  const page = Math.max(1, Number(pageValue ?? 1) || 1);
  const limit = Math.max(1, Math.min(100, Number(limitValue ?? 20) || 20));
  return { page, limit, offset: (page - 1) * limit };
}

export function ensureString(value: unknown, field: string): string {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new AppException("VALIDATION_ERROR", `${field} is required`, HttpStatus.BAD_REQUEST);
  }
  return value.trim();
}

export function ensureNumber(value: unknown, field: string): number {
  const numberValue = Number(value);
  if (!Number.isFinite(numberValue)) {
    throw new AppException("VALIDATION_ERROR", `${field} must be a number`, HttpStatus.BAD_REQUEST);
  }
  return numberValue;
}

export function mapDbError(error: unknown, duplicateCode: string, message: string): never {
  if (error instanceof QueryFailedError) {
    const driverError = error.driverError as { code?: string } | undefined;
    if (driverError?.code === "23505") {
      throw new AppException(duplicateCode, message, HttpStatus.CONFLICT);
    }
  }
  throw error;
}
