import { AdminOrderPanel } from "../../widgets/admin-order-panel/AdminOrderPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminOrdersPage() {
  return (
    <PageShell title="Admin Orders" description="주문 상태를 운영합니다.">
      <AdminOrderPanel />
    </PageShell>
  );
}
