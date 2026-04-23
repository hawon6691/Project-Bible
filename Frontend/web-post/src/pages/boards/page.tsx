import { BoardPostsPanel } from "../../widgets/board-posts-panel/BoardPostsPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function BoardsPage() {
  return (
    <PageShell title="Boards" description="게시판 목록, 게시글 목록, 게시글 작성을 확인합니다.">
      <BoardPostsPanel />
    </PageShell>
  );
}
