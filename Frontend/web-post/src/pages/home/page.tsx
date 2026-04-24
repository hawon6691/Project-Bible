import { HomeSummary } from "../../widgets/home-summary/HomeSummary";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function HomePage() {
  return (
    <PageShell title="Post Board" description="실시간 글 목록, 게시판 분류, 인기글을 빠르게 훑어봅니다.">
      <HomeSummary />
    </PageShell>
  );
}
