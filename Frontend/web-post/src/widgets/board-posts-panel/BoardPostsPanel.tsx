import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { fetchPosts } from "../../entities/post/api";
import { CreatePostForm } from "../../features/post-create/CreatePostForm";

export function BoardPostsPanel() {
  const boards = useQuery({ queryKey: ["boards"], queryFn: fetchBoards });
  const posts = useQuery({ queryKey: ["posts"], queryFn: () => fetchPosts({ page: 1, limit: 20, sort: "latest" }) });
  const postRows = posts.data?.data ?? [];

  return (
    <div className="board-layout wide">
      <aside className="board-rail">
        <div className="section-heading">
          <span className="eyebrow">Channel</span>
          <h2>게시판 목록</h2>
        </div>
        <div className="board-list">
          {boards.isLoading && <p className="muted">게시판을 불러오는 중입니다.</p>}
          {boards.error && <p className="error">게시판을 불러오지 못했습니다.</p>}
          {(boards.data ?? []).map((board) => (
            <article className="board-item" key={board.id}>
              <div>
                <strong>{board.name}</strong>
                <p>{board.description}</p>
              </div>
              <span className="pill">{board.status}</span>
            </article>
          ))}
        </div>

        <section className="compose-panel">
          <div className="section-heading">
            <span className="eyebrow">Compose</span>
            <h2>글 작성</h2>
          </div>
          <CreatePostForm />
        </section>
      </aside>

      <section className="post-feed">
        <div className="section-heading split">
          <div>
            <span className="eyebrow">Latest</span>
            <h2>전체 게시글</h2>
          </div>
          <span className="pill">{posts.data?.meta?.totalCount ?? 0} posts</span>
        </div>
        <div className="board-table">
          <div className="board-table-head">
            <span>게시글</span>
            <span>반응</span>
            <span>상태</span>
          </div>
          {posts.isLoading && <p className="empty-state">게시글을 불러오는 중입니다.</p>}
          {posts.error && <p className="error">게시글을 불러오지 못했습니다.</p>}
          {postRows.map((post) => (
            <article className="board-table-row" key={post.id}>
              <div className="post-main">
                <span className="board-chip">{post.boardName ?? `board ${post.boardId}`}</span>
                <strong>
                  #{post.id} {post.title}
                </strong>
              </div>
              <div className="post-meta">
                <span>{post.viewCount} views</span>
                <span>{post.likeCount} likes</span>
                <span>{post.commentCount} comments</span>
              </div>
              <span className="pill">{post.status}</span>
            </article>
          ))}
          {postRows.length === 0 && !posts.isLoading && <p className="empty-state">아직 표시할 게시글이 없습니다.</p>}
        </div>
      </section>
    </div>
  );
}
