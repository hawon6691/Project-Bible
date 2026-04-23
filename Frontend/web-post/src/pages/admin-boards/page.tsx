import { AdminBoardPanel } from "../../widgets/admin-board-panel/AdminBoardPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminBoardsPage() {
  return (
    <PageShell title="Admin Boards" description="관리자 게시판 CRUD입니다.">
      <AdminBoardPanel />
    </PageShell>
  );
}
