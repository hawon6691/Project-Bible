import { useQueryClient } from "@tanstack/react-query";
import { deleteComment } from "../../entities/comment/api";

interface Props {
  commentId: number;
  postId: number;
}

export function DeleteCommentButton({ commentId, postId }: Props) {
  const queryClient = useQueryClient();
  return (
    <button
      className="secondary"
      type="button"
      onClick={() => deleteComment(commentId).then(() => queryClient.invalidateQueries({ queryKey: ["comments", postId] }))}
    >
      Delete
    </button>
  );
}
