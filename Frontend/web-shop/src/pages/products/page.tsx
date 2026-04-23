import { ProductCatalog } from "../../widgets/product-catalog/ProductCatalog";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function ProductsPage() {
  return (
    <PageShell title="Products" description="카테고리와 상품 목록을 확인하고 장바구니에 담습니다.">
      <ProductCatalog />
    </PageShell>
  );
}
