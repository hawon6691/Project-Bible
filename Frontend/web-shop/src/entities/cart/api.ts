import { apiClient, unwrap } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { CartItem } from "./model";

export const fetchCart = () => unwrap<CartItem[]>(apiClient.get("/api/v1/cart-items", { headers: authHeader("user") }));
export const addCart = (payload: { productId: number; productOptionId?: number; quantity: number }) =>
  unwrap<CartItem>(apiClient.post("/api/v1/cart-items", payload, { headers: authHeader("user") }));
export const updateCart = (cartItemId: number, quantity: number) =>
  unwrap<CartItem>(apiClient.patch(`/api/v1/cart-items/${cartItemId}`, { quantity }, { headers: authHeader("user") }));
export const deleteCart = (cartItemId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/cart-items/${cartItemId}`, { headers: authHeader("user") }));
export const clearCart = () =>
  unwrap<{ message: string }>(apiClient.delete("/api/v1/cart-items", { headers: authHeader("user") }));
