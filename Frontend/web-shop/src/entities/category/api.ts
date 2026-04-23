import { apiClient, unwrap } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { CategorySummary } from "./model";

export const fetchCategories = () => unwrap<CategorySummary[]>(apiClient.get("/api/v1/categories"));
export const fetchCategory = (categoryId: number) => unwrap<CategorySummary>(apiClient.get(`/api/v1/categories/${categoryId}`));
export const createCategory = (payload: { name: string; displayOrder: number }) =>
  unwrap<CategorySummary>(apiClient.post("/api/v1/admin/categories", payload, { headers: authHeader("admin") }));
export const updateCategory = (categoryId: number, payload: Partial<CategorySummary>) =>
  unwrap<CategorySummary>(apiClient.patch(`/api/v1/admin/categories/${categoryId}`, payload, { headers: authHeader("admin") }));
export const deleteCategory = (categoryId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/admin/categories/${categoryId}`, { headers: authHeader("admin") }));
