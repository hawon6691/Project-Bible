import { useQuery } from "@tanstack/react-query";
import { fetchCart } from "../../entities/cart/api";
import { CartItemActions, ClearCartButton } from "../../features/cart-update/CartItemActions";

export function CartPanel() {
  const cart = useQuery({ queryKey: ["cart"], queryFn: fetchCart, retry: false });

  return (
    <>
      {cart.error && <p className="error">Login as user first.</p>}
      <ClearCartButton />
      <div className="stack">
        {(cart.data ?? []).map((item) => (
          <div className="card row" key={item.id}>
            <strong>{item.productName}</strong>
            <span>qty {item.quantity}</span>
            <span>{Number(item.lineAmount).toLocaleString()} KRW</span>
            <CartItemActions cartItemId={item.id} quantity={item.quantity} />
          </div>
        ))}
      </div>
    </>
  );
}
