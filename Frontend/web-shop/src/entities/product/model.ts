export interface ProductSummary {
  id: number;
  categoryId: number;
  name: string;
  price: number;
  stock: number;
  status: string;
}

export interface ProductOption {
  id: number;
  name: string;
  value: string;
  additionalPrice: number;
  stock: number;
}

export interface ProductImage {
  id: number;
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface ProductDetail extends ProductSummary {
  description: string;
  options: ProductOption[];
  images: ProductImage[];
}
