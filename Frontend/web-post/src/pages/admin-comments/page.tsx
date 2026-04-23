import { AdminCommentPanel } from "../../widgets/admin-comment-panel/AdminCommentPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminCommentsPage() {
  return (
    <PageShell title="Admin Comments" description="댓글 상태를 운영합니다.">
      <AdminCommentPanel />
    </PageShell>
  );
}
