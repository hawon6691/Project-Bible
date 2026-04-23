import { AdminLoginPanel } from "../../features/auth-admin/AdminLoginPanel";
import { UserLoginPanel } from "../../features/auth-user/UserLoginPanel";

export function LoginPanel() {
  return (
    <div className="grid">
      <UserLoginPanel />
      <AdminLoginPanel />
    </div>
  );
}
