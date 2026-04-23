import { FormEvent } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createPayment } from "../../entities/payment/api";

export function CreatePaymentForm({ orderId, onPaid }: { orderId: number; onPaid: (paymentId: number) => void }) {
  const queryClient = useQueryClient();
  const createPay = useMutation({
    mutationFn: () => createPayment(orderId),
    onSuccess: (result) => {
      onPaid(result.id);
      queryClient.invalidateQueries({ queryKey: ["order-detail", orderId] });
    },
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    createPay.mutate();
  }

  return (
    <form className="row" onSubmit={submit}>
      <button type="submit">Pay order</button>
    </form>
  );
}
