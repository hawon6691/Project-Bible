import { HomeSummary } from "../../widgets/home-summary/HomeSummary";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function HomePage() {
  return (
    <PageShell title="Post Board" description="게시판별 최신 글과 서비스 상태를 한 화면에서 확인합니다.">
      <HomeSummary />
    </PageShell>
  );
}
