import { HomeSummary } from "../../widgets/home-summary/HomeSummary";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function HomePage() {
  return (
    <PageShell title="Shop Home" description="쇼핑몰 API 상태와 주요 상품을 확인합니다.">
      <HomeSummary />
    </PageShell>
  );
}
