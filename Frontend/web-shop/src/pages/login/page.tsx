import { LoginPanel } from "../../widgets/app-shell/LoginPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function LoginPage() {
  return (
    <PageShell title="Login" description="사용자와 관리자를 분리해서 로그인합니다.">
      <LoginPanel />
    </PageShell>
  );
}
