import { SignupUserForm } from "../../features/signup-user/SignupUserForm";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function SignupPage() {
  return (
    <PageShell title="Signup" description="쇼핑몰 사용자 계정을 생성합니다.">
      <SignupUserForm />
    </PageShell>
  );
}
