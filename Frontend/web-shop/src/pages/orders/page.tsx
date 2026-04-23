import { OrdersPanel } from "../../widgets/orders-panel/OrdersPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function OrdersPage() {
  return (
    <PageShell title="Orders" description="주문 상세, 결제, 환불, 취소를 확인합니다.">
      <OrdersPanel />
    </PageShell>
  );
}
