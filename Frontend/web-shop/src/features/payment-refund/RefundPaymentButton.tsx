import { useQueryClient } from "@tanstack/react-query";
import { refundPayment } from "../../entities/payment/api";

export function RefundPaymentButton({ paymentId }: { paymentId: number }) {
  const queryClient = useQueryClient();
  return (
    <button className="secondary" type="button" onClick={() => refundPayment(paymentId).then(() => queryClient.invalidateQueries({ queryKey: ["payment", paymentId] }))}>
      Refund
    </button>
  );
}
