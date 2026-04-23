import { apiClient, unwrap, unwrapPaged } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { ProductDetail, ProductSummary } from "./model";

export const fetchProducts = (params?: Record<string, string | number>) =>
  unwrapPaged<ProductSummary>(apiClient.get("/api/v1/products", { params }));
export const fetchProductDetail = (productId: number) =>
  unwrap<ProductDetail>(apiClient.get(`/api/v1/products/${productId}`));
export const createProduct = (payload: {
  categoryId: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  status?: string;
}) => unwrap<ProductDetail>(apiClient.post("/api/v1/admin/products", payload, { headers: authHeader("admin") }));
export const updateProduct = (productId: number, payload: Partial<ProductSummary> & { description?: string }) =>
  unwrap<ProductDetail>(apiClient.patch(`/api/v1/admin/products/${productId}`, payload, { headers: authHeader("admin") }));
export const deleteProduct = (productId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/admin/products/${productId}`, { headers: authHeader("admin") }));
export const createOption = (productId: number, payload: { name: string; value: string; additionalPrice: number; stock: number }) =>
  unwrap(apiClient.post(`/api/v1/admin/products/${productId}/options`, payload, { headers: authHeader("admin") }));
export const createImage = (productId: number, payload: { imageUrl: string; isPrimary: boolean; displayOrder: number }) =>
  unwrap(apiClient.post(`/api/v1/admin/products/${productId}/images`, payload, { headers: authHeader("admin") }));
