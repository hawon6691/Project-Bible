export interface ApiEnvelope<T> {
  success: boolean;
  data: T | null;
  meta?: PageMeta | null;
  error?: { code: string; message: string; details?: unknown } | null;
}

export interface PageMeta {
  page: number;
  limit: number;
  totalCount: number;
  totalPages: number;
}

export interface PagedResult<T> {
  data: T[];
  meta: PageMeta | null;
}

export interface HealthPayload {
  status: string;
  service: string;
  domain: string;
}
