import { useQueryClient } from "@tanstack/react-query";
import { deleteAdminReview } from "../../entities/review/api";
import type { Review } from "../../entities/review/model";

export function AdminReviewDeleteActions({ reviews }: { reviews: Review[] }) {
  const queryClient = useQueryClient();
  return (
    <div className="stack">
      {reviews.map((review) => (
        <div className="card row" key={review.id}>
          <span>#{review.id} rating {review.rating} / {review.content}</span>
          <span>{review.status}</span>
          <button className="secondary" type="button" onClick={() => deleteAdminReview(review.id).then(() => queryClient.invalidateQueries({ queryKey: ["admin-reviews"] }))}>
            Delete
          </button>
        </div>
      ))}
    </div>
  );
}
