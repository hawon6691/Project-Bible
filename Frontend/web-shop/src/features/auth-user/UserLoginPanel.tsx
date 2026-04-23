import { FormEvent, useState } from "react";
import { loginUser, logoutUser, refreshUser } from "../../entities/session/api";

export function UserLoginPanel() {
  const [email, setEmail] = useState("shop-user-1@example.com");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      await loginUser({ email, password: "Password1!" });
      setMessage("User login saved.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "User login failed");
    }
  }

  return (
    <form className="card stack" onSubmit={submit}>
      <h2>User</h2>
      <input value={email} onChange={(event) => setEmail(event.target.value)} />
      <button type="submit">Login user</button>
      <button className="secondary" type="button" onClick={() => refreshUser().then(() => setMessage("User token refreshed."))}>
        Refresh user
      </button>
      <button className="secondary" type="button" onClick={() => logoutUser().then(() => setMessage("User logged out."))}>
        Logout user
      </button>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
    </form>
  );
}
