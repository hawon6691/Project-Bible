import { AdminBoardPanel } from "../../widgets/admin-board-panel/AdminBoardPanel";
import { AdminShell } from "../../widgets/app-shell/AdminShell";

export function AdminBoardsPage() {
  return (
    <AdminShell title="Board Management" description="게시판 노출 상태와 생성 순서를 관리합니다.">
      <AdminBoardPanel />
    </AdminShell>
  );
}
