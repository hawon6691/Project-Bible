import { AdminDashboardPanel } from "../../widgets/admin-dashboard-panel/AdminDashboardPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminDashboardPage() {
  return (
    <PageShell title="Admin Dashboard" description="관리자 계정과 게시판 운영 요약입니다.">
      <AdminDashboardPanel />
    </PageShell>
  );
}
