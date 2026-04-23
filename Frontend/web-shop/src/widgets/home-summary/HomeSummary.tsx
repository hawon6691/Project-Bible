import { useQuery } from "@tanstack/react-query";
import { fetchProducts } from "../../entities/product/api";
import { fetchHealth } from "../../entities/session/api";
import { env } from "../../shared/config/env";

export function HomeSummary() {
  const health = useQuery({ queryKey: ["health"], queryFn: fetchHealth });
  const products = useQuery({ queryKey: ["products", "home"], queryFn: () => fetchProducts({ page: 1, limit: 5 }) });

  return (
    <>
      <div className="grid">
        <div className="card">
          <h2>API</h2>
          <p className="muted">{env.apiBaseUrl}</p>
          <p>{health.isLoading ? "Checking..." : `${health.data?.status} / ${health.data?.service}`}</p>
        </div>
        <div className="card">
          <h2>Products</h2>
          <p>{products.data?.meta?.totalCount ?? 0} total products</p>
        </div>
      </div>
      <h2>Featured</h2>
      <div className="stack">
        {(products.data?.data ?? []).map((product) => (
          <div className="card" key={product.id}>
            <strong>{product.name}</strong>
            <p className="muted">{Number(product.price).toLocaleString()} KRW / stock {product.stock}</p>
          </div>
        ))}
      </div>
    </>
  );
}
