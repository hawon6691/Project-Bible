import { useQueryClient } from "@tanstack/react-query";
import { clearCart, deleteCart, updateCart } from "../../entities/cart/api";

export function useCartReload() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ["cart"] });
}

export function CartItemActions({ cartItemId, quantity }: { cartItemId: number; quantity: number }) {
  const reload = useCartReload();
  return (
    <>
      <button type="button" onClick={() => updateCart(cartItemId, quantity + 1).then(reload)}>+1</button>
      <button className="secondary" type="button" onClick={() => deleteCart(cartItemId).then(reload)}>Delete</button>
    </>
  );
}

export function ClearCartButton() {
  const reload = useCartReload();
  return (
    <button className="secondary" type="button" onClick={() => clearCart().then(reload)}>
      Clear cart
    </button>
  );
}
