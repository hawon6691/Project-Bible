export interface ApiEnvelope<T> { success: boolean; data: T | null; meta?: Record<string, unknown> | null; error?: { code: string; message: string; details?: unknown } | null; }
export function ok<T>(data: T, meta?: Record<string, unknown>): ApiEnvelope<T> { return { success: true, data, meta: meta ?? null, error: null }; }
export function fail(code: string, message: string, details?: unknown): ApiEnvelope<null> { return { success: false, data: null, meta: null, error: { code, message, details } }; }
export function listMeta(totalCount: number) { return { page: 1, limit: totalCount, totalCount, totalPages: 1 }; }
