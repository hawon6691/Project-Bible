import { useQueryClient } from "@tanstack/react-query";
import { updateReview } from "../../entities/review/api";

export function UpdateReviewButton({ reviewId, productId, content }: { reviewId: number; productId: number; content: string }) {
  const queryClient = useQueryClient();
  return (
    <button
      className="secondary"
      type="button"
      onClick={() => updateReview(reviewId, { rating: 4, content: `${content} updated` }).then(() => queryClient.invalidateQueries({ queryKey: ["reviews", productId] }))}
    >
      Quick update
    </button>
  );
}
