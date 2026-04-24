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
    <div className="detail-layout">
      <aside className="detail-tools">
        <label>
          Post ID
          <input type="number" value={postId} onChange={(event) => setPostId(Number(event.target.value))} />
        </label>
        <UpdatePostTitleForm postId={postId} />
        <CreateCommentForm postId={postId} />
      </aside>

      <section className="detail-main">
        {detail.data && (
          <article className="post-detail-card">
            <div className="post-detail-header">
              <span className="board-chip">{detail.data.boardName ?? `board ${detail.data.boardId}`}</span>
              <span className="pill">{detail.data.status}</span>
            </div>
            <h2>{detail.data.title}</h2>
            <p>{detail.data.content}</p>
            <div className="post-meta prominent">
              <span>{detail.data.viewCount} views</span>
              <span>{detail.data.likeCount} likes</span>
              <span>{detail.data.commentCount} comments</span>
            </div>
            <PostLikeActions postId={postId} />
          </article>
        )}
        {!detail.data && detail.isLoading && <p className="empty-state">게시글을 불러오는 중입니다.</p>}
        {detail.error && <p className="error">게시글을 불러오지 못했습니다.</p>}

        <div className="section-heading split">
          <div>
            <span className="eyebrow">Thread</span>
            <h2>댓글</h2>
          </div>
          <span className="pill">{comments.data?.meta?.totalCount ?? 0} comments</span>
        </div>
        <div className="comment-list">
          {(comments.data?.data ?? []).map((item) => (
            <article className="comment-item" key={item.id}>
              <p>{item.content}</p>
              <div className="row">
                <span className="muted">#{item.id} / {item.status}</span>
                <DeleteCommentButton commentId={item.id} postId={postId} />
              </div>
            </article>
          ))}
          {comments.data?.data.length === 0 && <p className="empty-state">아직 댓글이 없습니다.</p>}
        </div>
      </section>
    </div>
  );
}
