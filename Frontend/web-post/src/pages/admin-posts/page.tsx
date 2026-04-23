import { AdminPostPanel } from "../../widgets/admin-post-panel/AdminPostPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminPostsPage() {
  return (
    <PageShell title="Admin Posts" description="게시글 상태를 운영합니다.">
      <AdminPostPanel />
    </PageShell>
  );
}
