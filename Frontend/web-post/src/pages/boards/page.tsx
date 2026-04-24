import { BoardPostsPanel } from "../../widgets/board-posts-panel/BoardPostsPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function BoardsPage() {
  return (
    <PageShell title="Boards" description="번호, 제목, 글쓴이, 조회, 추천 중심의 게시판 목록입니다.">
      <BoardPostsPanel />
    </PageShell>
  );
}
