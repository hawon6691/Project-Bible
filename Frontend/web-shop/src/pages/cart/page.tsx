import { CartPanel } from "../../widgets/cart-panel/CartPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function CartPage() {
  return (
    <PageShell title="Cart" description="장바구니 항목을 확인하고 수량을 변경합니다.">
      <CartPanel />
    </PageShell>
  );
}
