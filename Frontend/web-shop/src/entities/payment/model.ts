export interface Payment {
  id: number;
  orderId: number;
  paymentMethod: string;
  paymentStatus: string;
  paidAmount: number;
}
