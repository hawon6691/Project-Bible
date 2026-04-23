import { MypagePanel } from "../../widgets/mypage-panel/MypagePanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function MypagePage() {
  return (
    <PageShell title="My Page" description="로그인한 사용자 정보를 확인하고 수정합니다.">
      <MypagePanel />
    </PageShell>
  );
}
