import type { Address } from "../address/model";

export interface OrderSummary {
  id: number;
  orderNumber: string;
  orderStatus: string;
  paymentStatus: string;
  totalAmount: number;
}

export interface OrderDetail {
  order: OrderSummary;
  orderAddress: Address;
  orderItems: Array<{ id: number; productId: number; productNameSnapshot: string; quantity: number; lineAmount: number }>;
  payment: null | { id: number; paymentStatus: string; paidAmount: number };
}
