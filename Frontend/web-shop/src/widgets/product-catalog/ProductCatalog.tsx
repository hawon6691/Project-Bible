import { useQuery } from "@tanstack/react-query";
import { fetchCategories } from "../../entities/category/api";
import { fetchProducts } from "../../entities/product/api";
import { AddCartButton } from "../../features/cart-add/AddCartButton";

export function ProductCatalog() {
  const categories = useQuery({ queryKey: ["categories"], queryFn: fetchCategories });
  const products = useQuery({ queryKey: ["products"], queryFn: () => fetchProducts({ page: 1, limit: 20, sort: "latest" }) });

  return (
    <>
      <h2>Categories</h2>
      <div className="row">
        {(categories.data ?? []).map((category) => (
          <span className="card" key={category.id}>{category.name} / {category.status}</span>
        ))}
      </div>
      <h2>Products</h2>
      <div className="grid">
        {(products.data?.data ?? []).map((product) => (
          <div className="card stack" key={product.id}>
            <strong>#{product.id} {product.name}</strong>
            <span>{Number(product.price).toLocaleString()} KRW</span>
            <span className="muted">stock {product.stock} / {product.status}</span>
            <AddCartButton productId={product.id} />
          </div>
        ))}
      </div>
    </>
  );
}
