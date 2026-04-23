import { FormEvent, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { createOrder } from "../../entities/order/api";
import type { CartItem } from "../../entities/cart/model";

interface Props {
  cartItems: CartItem[];
  addressId: number;
  onAddressChange: (addressId: number) => void;
}

export function CreateOrderForm({ cartItems, addressId, onAddressChange }: Props) {
  const queryClient = useQueryClient();
  const [result, setResult] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    const order = await createOrder({ cartItemIds: cartItems.map((item) => item.id), addressId });
    setResult(`Order #${order.order.id} created.`);
    queryClient.invalidateQueries({ queryKey: ["cart"] });
  }

  return (
    <form className="stack" onSubmit={submit}>
      <label>
        Address ID
        <input type="number" value={addressId} onChange={(event) => onAddressChange(Number(event.target.value))} />
      </label>
      <button type="submit">Create order</button>
      {result && <p className="success">{result}</p>}
    </form>
  );
}
