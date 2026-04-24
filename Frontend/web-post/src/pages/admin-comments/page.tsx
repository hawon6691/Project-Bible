import { AdminCommentPanel } from "../../widgets/admin-comment-panel/AdminCommentPanel";
import { AdminShell } from "../../widgets/app-shell/AdminShell";

export function AdminCommentsPage() {
  return (
    <AdminShell title="Comment Moderation" description="댓글 상태를 빠르게 검토하고 숨김 처리합니다.">
      <AdminCommentPanel />
    </AdminShell>
  );
}
