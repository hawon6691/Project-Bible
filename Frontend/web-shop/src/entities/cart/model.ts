export interface CartItem {
  id: number;
  productId: number;
  productOptionId?: number | null;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineAmount: number;
}
