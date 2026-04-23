import { useMutation, useQueryClient } from "@tanstack/react-query";
import { addCart } from "../../entities/cart/api";

interface Props {
  productId: number;
}

export function AddCartButton({ productId }: Props) {
  const queryClient = useQueryClient();
  const cart = useMutation({
    mutationFn: () => addCart({ productId, quantity: 1 }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["cart"] }),
  });

  return (
    <>
      <button type="button" onClick={() => cart.mutate()}>Add cart</button>
      {cart.error && <p className="error">{cart.error.message}</p>}
    </>
  );
}
