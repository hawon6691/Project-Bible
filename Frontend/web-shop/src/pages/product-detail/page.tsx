import { ProductDetailPanel } from "../../widgets/product-detail-panel/ProductDetailPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function ProductDetailPage() {
  return (
    <PageShell title="Product Detail" description="상품 상세, 옵션/이미지, 리뷰를 확인합니다.">
      <ProductDetailPanel />
    </PageShell>
  );
}
