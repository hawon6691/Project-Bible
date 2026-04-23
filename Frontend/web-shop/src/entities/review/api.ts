import { apiClient, unwrap, unwrapPaged } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { Review } from "./model";

export const fetchProductReviews = (productId: number) =>
  unwrapPaged<Review>(apiClient.get(`/api/v1/products/${productId}/reviews`, { params: { page: 1, limit: 20 } }));
export const createReview = (orderItemId: number, payload: { rating: number; content: string }) =>
  unwrap<Review>(apiClient.post(`/api/v1/order-items/${orderItemId}/reviews`, payload, { headers: authHeader("user") }));
export const updateReview = (reviewId: number, payload: { rating: number; content: string }) =>
  unwrap<Review>(apiClient.patch(`/api/v1/reviews/${reviewId}`, payload, { headers: authHeader("user") }));
export const deleteReview = (reviewId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/reviews/${reviewId}`, { headers: authHeader("user") }));
export const fetchAdminReviews = () =>
  unwrapPaged<Review>(apiClient.get("/api/v1/admin/reviews", { headers: authHeader("admin"), params: { page: 1, limit: 20 } }));
export const deleteAdminReview = (reviewId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/admin/reviews/${reviewId}`, { headers: authHeader("admin") }));
