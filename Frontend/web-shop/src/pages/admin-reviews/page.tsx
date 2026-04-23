import { AdminReviewPanel } from "../../widgets/admin-review-panel/AdminReviewPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminReviewsPage() {
  return (
    <PageShell title="Admin Reviews" description="리뷰를 조회하고 삭제합니다.">
      <AdminReviewPanel />
    </PageShell>
  );
}
