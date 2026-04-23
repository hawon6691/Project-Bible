import { useQuery } from "@tanstack/react-query";
import { fetchProducts } from "../../entities/product/api";
import { AdminProductManager } from "../../features/admin-product-manage/AdminProductManager";

export function AdminProductPanel() {
  const products = useQuery({ queryKey: ["products", "admin"], queryFn: () => fetchProducts({ page: 1, limit: 20 }) });
  return <AdminProductManager products={products.data?.data ?? []} />;
}
