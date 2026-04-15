export interface ApiErrorPayload {
  code: string;
  message: string;
  details?: unknown;
}

export interface ApiEnvelope<T> {
  success: boolean;
  data: T | null;
  meta?: Record<string, unknown> | null;
  error?: ApiErrorPayload | null;
}

export function ok<T>(data: T, meta?: Record<string, unknown>): ApiEnvelope<T> {
  return { success: true, data, meta: meta ?? null, error: null };
}

export function fail(code: string, message: string, details?: unknown): ApiEnvelope<null> {
  return { success: false, data: null, meta: null, error: { code, message, details } };
}

export function pageMeta(page: number, limit: number, totalCount: number): Record<string, number> {
  return { page, limit, totalCount, totalPages: Math.max(1, Math.ceil(totalCount / limit)) };
}
