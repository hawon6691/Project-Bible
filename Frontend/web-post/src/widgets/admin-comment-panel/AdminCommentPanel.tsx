import { useQuery } from "@tanstack/react-query";
import { fetchAdminComments } from "../../entities/comment/api";
import { AdminCommentModeration } from "../../features/admin-comment-moderate/AdminCommentModeration";

export function AdminCommentPanel() {
  const comments = useQuery({ queryKey: ["admin-comments"], queryFn: fetchAdminComments });
  return (
    <>
      <p className="muted">Total: {comments.data?.meta?.totalCount ?? 0}</p>
      <AdminCommentModeration comments={comments.data?.data ?? []} />
    </>
  );
}
