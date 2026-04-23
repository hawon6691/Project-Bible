import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchMe, updateMe } from "../../entities/session/api";

export function MypagePanel() {
  const queryClient = useQueryClient();
  const me = useQuery({ queryKey: ["me"], queryFn: fetchMe, retry: false });
  const [nickname, setNickname] = useState("");
  const update = useMutation({
    mutationFn: () => updateMe({ nickname }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["me"] }),
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    update.mutate();
  }

  return (
    <>
      {me.error && <p className="error">Login as user first.</p>}
      {me.data && (
        <div className="card">
          <p>ID: {me.data.id}</p>
          <p>Email: {me.data.email}</p>
          <p>Nickname: {me.data.nickname}</p>
          <p>Status: {me.data.status}</p>
        </div>
      )}
      <form className="stack" onSubmit={submit}>
        <label>
          New nickname
          <input value={nickname} onChange={(event) => setNickname(event.target.value)} />
        </label>
        <button type="submit">Update</button>
      </form>
    </>
  );
}
