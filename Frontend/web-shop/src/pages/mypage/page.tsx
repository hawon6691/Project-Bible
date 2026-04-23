import { MypagePanel } from "../../widgets/mypage-panel/MypagePanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function MypagePage() {
  return (
    <PageShell title="My Page" description="회원 정보와 배송지를 확인합니다.">
      <MypagePanel />
    </PageShell>
  );
}
