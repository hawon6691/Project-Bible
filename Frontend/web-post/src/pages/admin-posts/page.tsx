import { AdminPostPanel } from "../../widgets/admin-post-panel/AdminPostPanel";
import { AdminShell } from "../../widgets/app-shell/AdminShell";

export function AdminPostsPage() {
  return (
    <AdminShell title="Post Moderation" description="게시글 공개 상태와 운영 지표를 확인합니다.">
      <AdminPostPanel />
    </AdminShell>
  );
}
