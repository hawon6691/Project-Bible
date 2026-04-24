import { PostDetailPanel } from "../../widgets/post-detail-panel/PostDetailPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function PostDetailPage() {
  return (
    <PageShell title="Post Detail" description="게시글 본문, 반응, 댓글 흐름을 확인합니다.">
      <PostDetailPanel />
    </PageShell>
  );
}
