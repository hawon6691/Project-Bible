import { useQuery } from "@tanstack/react-query";
import { fetchAdminOrders } from "../../entities/order/api";
import { AdminOrderStatusActions } from "../../features/admin-order-status/AdminOrderStatusActions";

export function AdminOrderPanel() {
  const orders = useQuery({ queryKey: ["admin-orders"], queryFn: fetchAdminOrders, retry: false });
  return (
    <>
      <p className="muted">Total: {orders.data?.meta?.totalCount ?? 0}</p>
      <AdminOrderStatusActions orders={orders.data?.data ?? []} />
    </>
  );
}
