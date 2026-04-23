import { FormEvent, useState } from "react";
import { loginAdmin, logoutAdmin, refreshAdmin } from "../../entities/session/api";

export function AdminLoginPanel() {
  const [email, setEmail] = useState("admin-shop-1@example.com");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      await loginAdmin({ email, password: "AdminPassword1!" });
      setMessage("Admin login saved.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Admin login failed");
    }
  }

  return (
    <form className="card stack" onSubmit={submit}>
      <h2>Admin</h2>
      <input value={email} onChange={(event) => setEmail(event.target.value)} />
      <button type="submit">Login admin</button>
      <button className="secondary" type="button" onClick={() => refreshAdmin().then(() => setMessage("Admin token refreshed."))}>
        Refresh admin
      </button>
      <button className="secondary" type="button" onClick={() => logoutAdmin().then(() => setMessage("Admin logged out."))}>
        Logout admin
      </button>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
    </form>
  );
}
