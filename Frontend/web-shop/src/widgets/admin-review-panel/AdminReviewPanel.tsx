import { useQuery } from "@tanstack/react-query";
import { fetchAdminReviews } from "../../entities/review/api";
import { AdminReviewDeleteActions } from "../../features/admin-review-delete/AdminReviewDeleteActions";

export function AdminReviewPanel() {
  const reviews = useQuery({ queryKey: ["admin-reviews"], queryFn: fetchAdminReviews, retry: false });
  return (
    <>
      <p className="muted">Total: {reviews.data?.meta?.totalCount ?? 0}</p>
      <AdminReviewDeleteActions reviews={reviews.data?.data ?? []} />
    </>
  );
}
