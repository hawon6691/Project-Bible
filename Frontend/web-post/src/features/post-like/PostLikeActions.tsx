import { useMutation, useQueryClient } from "@tanstack/react-query";
import { likePost, unlikePost } from "../../entities/like/api";

interface Props {
  postId: number;
}

export function PostLikeActions({ postId }: Props) {
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["post-detail", postId] });
  const like = useMutation({ mutationFn: () => likePost(postId), onSuccess: invalidate });
  const unlike = useMutation({ mutationFn: () => unlikePost(postId), onSuccess: invalidate });

  return (
    <div className="row action-row">
      <button type="button" onClick={() => like.mutate()}>Like</button>
      <button className="secondary" type="button" onClick={() => unlike.mutate()}>Unlike</button>
    </div>
  );
}
