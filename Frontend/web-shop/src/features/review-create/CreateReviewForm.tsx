import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createReview } from "../../entities/review/api";

export function CreateReviewForm({ productId }: { productId: number }) {
  const queryClient = useQueryClient();
  const [orderItemId, setOrderItemId] = useState(1);
  const [content, setContent] = useState("Good product");
  const review = useMutation({
    mutationFn: () => createReview(orderItemId, { rating: 5, content }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["reviews", productId] }),
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    review.mutate();
  }

  return (
    <form className="stack" onSubmit={submit}>
      <h2>Create review</h2>
      <input type="number" value={orderItemId} onChange={(event) => setOrderItemId(Number(event.target.value))} />
      <textarea value={content} onChange={(event) => setContent(event.target.value)} />
      <button type="submit">Create review</button>
      {review.error && <p className="error">{review.error.message}</p>}
    </form>
  );
}
