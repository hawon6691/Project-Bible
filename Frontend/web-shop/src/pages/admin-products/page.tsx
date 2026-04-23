import { AdminProductPanel } from "../../widgets/admin-product-panel/AdminProductPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminProductsPage() {
  return (
    <PageShell title="Admin Products" description="상품, 옵션, 이미지를 관리합니다.">
      <AdminProductPanel />
    </PageShell>
  );
}
