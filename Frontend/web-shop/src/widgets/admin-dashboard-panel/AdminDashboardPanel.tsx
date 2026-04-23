import { useQuery } from "@tanstack/react-query";
import { fetchAdminDashboard } from "../../entities/admin/api";
import { fetchAdminMe } from "../../entities/session/api";

export function AdminDashboardPanel() {
  const me = useQuery({ queryKey: ["admin-me"], queryFn: fetchAdminMe, retry: false });
  const dashboard = useQuery({ queryKey: ["admin-dashboard"], queryFn: fetchAdminDashboard, retry: false });

  return (
    <>
      {me.error && <p className="error">Login as admin first.</p>}
      {me.data && <p>Admin: {me.data.email}</p>}
      {dashboard.data && <pre>{JSON.stringify(dashboard.data, null, 2)}</pre>}
    </>
  );
}
