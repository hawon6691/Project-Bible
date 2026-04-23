import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { fetchAddresses } from "../../entities/address/api";
import { fetchMe, updateMe } from "../../entities/session/api";
import { AddressRowActions } from "../../features/address-manage/AddressActions";

export function MypagePanel() {
  const queryClient = useQueryClient();
  const me = useQuery({ queryKey: ["me"], queryFn: fetchMe, retry: false });
  const addresses = useQuery({ queryKey: ["addresses", "mypage"], queryFn: fetchAddresses, retry: false });
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("01012345678");
  const update = useMutation({ mutationFn: () => updateMe({ name, phone }), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["me"] }) });

  function submit(event: FormEvent) {
    event.preventDefault();
    update.mutate();
  }

  return (
    <>
      {me.error && <p className="error">Login as user first.</p>}
      {me.data && <pre>{JSON.stringify(me.data, null, 2)}</pre>}
      <form className="row" onSubmit={submit}>
        <input placeholder="Name" value={name} onChange={(event) => setName(event.target.value)} />
        <input placeholder="Phone" value={phone} onChange={(event) => setPhone(event.target.value)} />
        <button type="submit">Update me</button>
      </form>
      <h2>Addresses</h2>
      <div className="stack">
        {(addresses.data ?? []).map((address) => (
          <div className="card row" key={address.id}>
            <span>#{address.id} {address.address1} {address.address2}</span>
            <AddressRowActions addressId={address.id} />
          </div>
        ))}
      </div>
    </>
  );
}
