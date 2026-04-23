import { HomeSummary } from "../../widgets/home-summary/HomeSummary";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function HomePage() {
  return (
    <PageShell title="Post Home" description="게시판 서비스의 주요 상태와 최신 글을 확인합니다.">
      <HomeSummary />
    </PageShell>
  );
}
