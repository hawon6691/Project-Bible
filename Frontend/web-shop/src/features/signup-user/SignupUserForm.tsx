import { FormEvent, useState } from "react";
import { signupUser } from "../../entities/session/api";

export function SignupUserForm() {
  const [email, setEmail] = useState(`shop-${Date.now()}@example.com`);
  const [name, setName] = useState("New Customer");
  const [phone, setPhone] = useState("01012345678");
  const [result, setResult] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    const user = await signupUser({ email, name, phone, password: "Password1!" });
    setResult(`Created user #${user.id} ${user.email}`);
  }

  return (
    <form className="stack" onSubmit={submit}>
      <input value={email} onChange={(event) => setEmail(event.target.value)} />
      <input value={name} onChange={(event) => setName(event.target.value)} />
      <input value={phone} onChange={(event) => setPhone(event.target.value)} />
      <button type="submit">Create user</button>
      {result && <p className="success">{result}</p>}
    </form>
  );
}
