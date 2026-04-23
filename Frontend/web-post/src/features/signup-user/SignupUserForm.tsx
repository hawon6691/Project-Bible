import { FormEvent, useState } from "react";
import { signupUser } from "../../entities/session/api";

export function SignupUserForm() {
  const [email, setEmail] = useState(`post-${Date.now()}@example.com`);
  const [nickname, setNickname] = useState("new-writer");
  const [result, setResult] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    const user = await signupUser({ email, nickname, password: "Password1!" });
    setResult(`Created user #${user.id} ${user.email}`);
  }

  return (
    <form className="stack" onSubmit={submit}>
      <label>
        Email
        <input value={email} onChange={(event) => setEmail(event.target.value)} />
      </label>
      <label>
        Nickname
        <input value={nickname} onChange={(event) => setNickname(event.target.value)} />
      </label>
      <button type="submit">Create user</button>
      {result && <p className="success">{result}</p>}
    </form>
  );
}
