import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchComments } from "../../entities/comment/api";
import { fetchPostDetail } from "../../entities/post/api";
import { CreateCommentForm } from "../../features/comment-create/CreateCommentForm";
import { DeleteCommentButton } from "../../features/comment-edit/DeleteCommentButton";
import { PostLikeActions } from "../../features/post-like/PostLikeActions";
import { UpdatePostTitleForm } from "../../features/post-edit/UpdatePostTitleForm";

export function PostDetailPanel() {
  const [postId, setPostId] = useState(1);
  const detail = useQuery({ queryKey: ["post-detail", postId], queryFn: () => fetchPostDetail(postId) });
  const comments = useQuery({ queryKey: ["comments", postId], queryFn: () => fetchComments(postId) });

  return (
    <div className="stack">
      <label>
        Post ID
        <input type="number" value={postId} onChange={(event) => setPostId(Number(event.target.value))} />
      </label>
      {detail.data && (
        <div className="card">
          <h2>{detail.data.title}</h2>
          <p>{detail.data.content}</p>
          <p className="muted">
            views {detail.data.viewCount} / likes {detail.data.likeCount} / comments {detail.data.commentCount}
          </p>
          <PostLikeActions postId={postId} />
        </div>
      )}
      <UpdatePostTitleForm postId={postId} />
      <CreateCommentForm postId={postId} />
      <h2>Comments</h2>
      {(comments.data?.data ?? []).map((item) => (
        <div className="card row" key={item.id}>
          <span>{item.content}</span>
          <span className="muted">{item.status}</span>
          <DeleteCommentButton commentId={item.id} postId={postId} />
        </div>
      ))}
    </div>
  );
}
