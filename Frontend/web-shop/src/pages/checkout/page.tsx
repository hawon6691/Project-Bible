import { CheckoutPanel } from "../../widgets/checkout-panel/CheckoutPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function CheckoutPage() {
  return (
    <PageShell title="Checkout" description="장바구니 항목과 배송지로 주문을 생성합니다.">
      <CheckoutPanel />
    </PageShell>
  );
}
