export interface Review {
  id: number;
  productId: number;
  orderItemId: number;
  rating: number;
  content: string;
  status: string;
}
