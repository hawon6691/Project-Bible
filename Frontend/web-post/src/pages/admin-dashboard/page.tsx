import { AdminDashboardPanel } from "../../widgets/admin-dashboard-panel/AdminDashboardPanel";
import { AdminShell } from "../../widgets/app-shell/AdminShell";

export function AdminDashboardPage() {
  return (
    <AdminShell title="Admin Dashboard" description="관리자 계정과 게시판 운영 요약입니다.">
      <AdminDashboardPanel />
    </AdminShell>
  );
}
