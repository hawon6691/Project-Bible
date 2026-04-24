import { useQueryClient } from "@tanstack/react-query";
import { changeCommentStatus } from "../../entities/comment/api";
import type { CommentSummary } from "../../entities/comment/model";

interface Props {
  comments: CommentSummary[];
}

export function AdminCommentModeration({ comments }: Props) {
  const queryClient = useQueryClient();
  const reload = () => queryClient.invalidateQueries({ queryKey: ["admin-comments"] });

  return (
    <div className="admin-table">
      <div className="admin-table-head">
        <span>Comment</span>
        <span>Status</span>
        <span>Actions</span>
      </div>
      {comments.map((comment) => (
        <article className="admin-table-row" key={comment.id}>
          <div>
            <strong>#{comment.id}</strong>
            <p>{comment.content}</p>
          </div>
          <span className="pill">{comment.status}</span>
          <div className="row">
            <button type="button" onClick={() => changeCommentStatus(comment.id, "ACTIVE").then(reload)}>Active</button>
            <button className="secondary" type="button" onClick={() => changeCommentStatus(comment.id, "HIDDEN").then(reload)}>
              Hidden
            </button>
          </div>
        </article>
      ))}
      {comments.length === 0 && <p className="empty-state">운영할 댓글이 없습니다.</p>}
    </div>
  );
}
