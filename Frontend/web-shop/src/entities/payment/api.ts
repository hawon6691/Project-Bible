import { apiClient, unwrap } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { Payment } from "./model";

export const createPayment = (orderId: number) =>
  unwrap<Payment>(apiClient.post("/api/v1/payments", { orderId, paymentMethod: "mock" }, { headers: authHeader("user") }));
export const fetchPayment = (paymentId: number) =>
  unwrap<Payment>(apiClient.get(`/api/v1/payments/${paymentId}`, { headers: authHeader("user") }));
export const refundPayment = (paymentId: number) =>
  unwrap<Payment>(apiClient.post(`/api/v1/payments/${paymentId}/refund`, null, { headers: authHeader("user") }));
