import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { cancelOrder, fetchOrderDetail, fetchOrders } from "../../entities/order/api";
import { fetchPayment } from "../../entities/payment/api";
import { CreatePaymentForm } from "../../features/payment-create/PaymentActions";
import { RefundPaymentButton } from "../../features/payment-refund/RefundPaymentButton";

export function OrdersPanel() {
  const queryClient = useQueryClient();
  const orders = useQuery({ queryKey: ["orders"], queryFn: fetchOrders, retry: false });
  const [orderId, setOrderId] = useState(1);
  const [paymentId, setPaymentId] = useState(1);
  const detail = useQuery({ queryKey: ["order-detail", orderId], queryFn: () => fetchOrderDetail(orderId), retry: false });
  const payment = useQuery({ queryKey: ["payment", paymentId], queryFn: () => fetchPayment(paymentId), retry: false });

  return (
    <>
      {orders.error && <p className="error">Login as user first.</p>}
      <h2>Orders</h2>
      <div className="stack">
        {(orders.data?.data ?? []).map((order) => (
          <button className="secondary" key={order.id} type="button" onClick={() => setOrderId(order.id)}>
            #{order.id} {order.orderStatus} / {Number(order.totalAmount).toLocaleString()} KRW
          </button>
        ))}
      </div>
      <div className="row">
        <input type="number" value={orderId} onChange={(event) => setOrderId(Number(event.target.value))} />
        <CreatePaymentForm orderId={orderId} onPaid={setPaymentId} />
        <button className="secondary" type="button" onClick={() => cancelOrder(orderId).then(() => queryClient.invalidateQueries({ queryKey: ["orders"] }))}>
          Cancel order
        </button>
      </div>
      {detail.data && <pre>{JSON.stringify(detail.data, null, 2)}</pre>}
      <h2>Payment</h2>
      <div className="row">
        <input type="number" value={paymentId} onChange={(event) => setPaymentId(Number(event.target.value))} />
        <RefundPaymentButton paymentId={paymentId} />
      </div>
      {payment.data && <pre>{JSON.stringify(payment.data, null, 2)}</pre>}
    </>
  );
}
