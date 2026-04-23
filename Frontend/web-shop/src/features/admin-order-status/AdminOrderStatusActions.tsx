import { useQueryClient } from "@tanstack/react-query";
import { changeOrderStatus } from "../../entities/order/api";
import type { OrderSummary } from "../../entities/order/model";

export function AdminOrderStatusActions({ orders }: { orders: OrderSummary[] }) {
  const queryClient = useQueryClient();
  const reload = () => queryClient.invalidateQueries({ queryKey: ["admin-orders"] });
  return (
    <div className="stack">
      {orders.map((order) => (
        <div className="card row" key={order.id}>
          <strong>#{order.id} {order.orderNumber}</strong>
          <span>{order.orderStatus}</span>
          <button type="button" onClick={() => changeOrderStatus(order.id, "PREPARING").then(reload)}>Preparing</button>
          <button className="secondary" type="button" onClick={() => changeOrderStatus(order.id, "SHIPPING").then(reload)}>Shipping</button>
        </div>
      ))}
    </div>
  );
}
