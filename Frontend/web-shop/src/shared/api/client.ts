import axios from "axios";
import { env } from "../config/env";
import type { ApiEnvelope, PagedResult, PageMeta } from "../types";

export const apiClient = axios.create({ baseURL: env.apiBaseUrl, timeout: 8000 });

export async function unwrap<T>(request: Promise<{ data: ApiEnvelope<T> }>) {
  const response = await request;
  if (!response.data.success || response.data.data === null) {
    throw new Error(response.data.error?.message ?? "Request failed");
  }
  return response.data.data;
}

export async function unwrapPaged<T>(request: Promise<{ data: ApiEnvelope<T[]> }>): Promise<PagedResult<T>> {
  const response = await request;
  if (!response.data.success || response.data.data === null) {
    throw new Error(response.data.error?.message ?? "Request failed");
  }
  return { data: response.data.data, meta: (response.data.meta as PageMeta | undefined) ?? null };
}
