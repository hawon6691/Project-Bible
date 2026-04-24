import { BoardPostsPanel } from "../../widgets/board-posts-panel/BoardPostsPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function BoardsPage() {
  return (
    <PageShell title="Boards" description="게시판을 살펴보고 최신 게시글을 목록 중심으로 확인합니다.">
      <BoardPostsPanel />
    </PageShell>
  );
}
