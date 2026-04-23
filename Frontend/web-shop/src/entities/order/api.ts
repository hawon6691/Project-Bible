import { apiClient, unwrap, unwrapPaged } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { OrderDetail, OrderSummary } from "./model";

export const createOrder = (payload: { cartItemIds: number[]; addressId: number }) =>
  unwrap<OrderDetail>(apiClient.post("/api/v1/orders", payload, { headers: authHeader("user") }));
export const fetchOrders = () =>
  unwrapPaged<OrderSummary>(apiClient.get("/api/v1/orders", { headers: authHeader("user"), params: { page: 1, limit: 20 } }));
export const fetchOrderDetail = (orderId: number) =>
  unwrap<OrderDetail>(apiClient.get(`/api/v1/orders/${orderId}`, { headers: authHeader("user") }));
export const cancelOrder = (orderId: number) =>
  unwrap<OrderSummary>(apiClient.post(`/api/v1/orders/${orderId}/cancel`, null, { headers: authHeader("user") }));
export const fetchAdminOrders = () =>
  unwrapPaged<OrderSummary>(
    apiClient.get("/api/v1/admin/orders", { headers: authHeader("admin"), params: { page: 1, limit: 20 } }),
  );
export const changeOrderStatus = (orderId: number, orderStatus: string) =>
  unwrap<OrderSummary>(apiClient.patch(`/api/v1/admin/orders/${orderId}/status`, { orderStatus }, { headers: authHeader("admin") }));
