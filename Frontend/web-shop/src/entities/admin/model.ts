export interface AdminDashboard {
  orderSummary: Record<string, number>;
  productSummary: Record<string, number>;
  userSummary: Record<string, number>;
  reviewSummary: Record<string, number>;
}
