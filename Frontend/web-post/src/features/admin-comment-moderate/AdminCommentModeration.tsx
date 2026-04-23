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
    <div className="stack">
      {comments.map((comment) => (
        <div className="card row" key={comment.id}>
          <span>#{comment.id} {comment.content}</span>
          <span>{comment.status}</span>
          <button type="button" onClick={() => changeCommentStatus(comment.id, "ACTIVE").then(reload)}>Active</button>
          <button className="secondary" type="button" onClick={() => changeCommentStatus(comment.id, "HIDDEN").then(reload)}>
            Hidden
          </button>
        </div>
      ))}
    </div>
  );
}
