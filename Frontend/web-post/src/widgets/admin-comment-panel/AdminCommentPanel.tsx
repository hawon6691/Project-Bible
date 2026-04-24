import { useQuery } from "@tanstack/react-query";
import { fetchAdminComments } from "../../entities/comment/api";
import { AdminCommentModeration } from "../../features/admin-comment-moderate/AdminCommentModeration";

export function AdminCommentPanel() {
  const comments = useQuery({ queryKey: ["admin-comments"], queryFn: fetchAdminComments });
  return (
    <section className="admin-stack">
      <div className="section-heading split">
        <div>
          <span className="eyebrow">Queue</span>
          <h2>댓글 운영</h2>
        </div>
        <span className="pill">{comments.data?.meta?.totalCount ?? 0} comments</span>
      </div>
      <AdminCommentModeration comments={comments.data?.data ?? []} />
    </section>
  );
}
