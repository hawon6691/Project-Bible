import { useQueryClient } from "@tanstack/react-query";
import { changePostStatus } from "../../entities/post/api";
import type { PostSummary } from "../../entities/post/model";

interface Props {
  posts: PostSummary[];
}

export function AdminPostModeration({ posts }: Props) {
  const queryClient = useQueryClient();
  const reload = () => queryClient.invalidateQueries({ queryKey: ["admin-posts"] });

  return (
    <div className="admin-table">
      <div className="admin-table-head">
        <span>Post</span>
        <span>Status</span>
        <span>Actions</span>
      </div>
      {posts.map((post) => (
        <article className="admin-table-row" key={post.id}>
          <div className="post-main">
            <span className="board-chip">{post.boardName ?? `board ${post.boardId}`}</span>
            <strong>#{post.id} {post.title}</strong>
          </div>
          <span className="pill">{post.status}</span>
          <div className="row">
            <button type="button" onClick={() => changePostStatus(post.id, "ACTIVE").then(reload)}>Active</button>
            <button className="secondary" type="button" onClick={() => changePostStatus(post.id, "HIDDEN").then(reload)}>
              Hidden
            </button>
          </div>
        </article>
      ))}
      {posts.length === 0 && <p className="empty-state">운영할 게시글이 없습니다.</p>}
    </div>
  );
}
