import { PostDetailPanel } from "../../widgets/post-detail-panel/PostDetailPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function PostDetailPage() {
  return (
    <PageShell title="Post Detail" description="게시글 상세, 댓글, 좋아요를 확인합니다.">
      <PostDetailPanel />
    </PageShell>
  );
}
