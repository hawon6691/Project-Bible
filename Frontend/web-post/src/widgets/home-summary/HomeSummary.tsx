import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { fetchPosts } from "../../entities/post/api";
import { fetchHealth } from "../../entities/session/api";
import { env } from "../../shared/config/env";

export function HomeSummary() {
  const health = useQuery({ queryKey: ["health"], queryFn: fetchHealth });
  const boards = useQuery({ queryKey: ["boards", "home"], queryFn: fetchBoards });
  const posts = useQuery({ queryKey: ["posts", "home"], queryFn: () => fetchPosts({ page: 1, limit: 5 }) });
  const healthLabel = health.isLoading ? "Checking" : health.data ? `${health.data.status} / ${health.data.service}` : "Unavailable";
  const recentPosts = posts.data?.data ?? [];

  return (
    <div className="home-board">
      <div className="status-strip">
        <div>
          <span className="eyebrow">API Endpoint</span>
          <strong>{env.apiBaseUrl}</strong>
        </div>
        <span className={health.data?.status === "UP" ? "pill success-pill" : "pill"}>{healthLabel}</span>
      </div>

      <div className="metric-grid">
        <div className="metric-card">
          <span>Boards</span>
          <strong>{boards.data?.length ?? 0}</strong>
          <small>active board groups</small>
        </div>
        <div className="metric-card">
          <span>Posts</span>
          <strong>{posts.data?.meta?.totalCount ?? 0}</strong>
          <small>total discussion threads</small>
        </div>
        <div className="metric-card accent">
          <span>Latest</span>
          <strong>{recentPosts[0]?.title ?? "No posts"}</strong>
          <small>{recentPosts[0]?.boardName ?? `board ${recentPosts[0]?.boardId ?? "-"}`}</small>
        </div>
      </div>

      <div className="board-layout">
        <section className="board-rail">
          <div className="section-heading">
            <span className="eyebrow">Boards</span>
            <h2>게시판</h2>
          </div>
          <div className="board-list">
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
        </section>

        <section className="post-feed">
          <div className="section-heading split">
            <div>
              <span className="eyebrow">Recent posts</span>
              <h2>최신 글</h2>
            </div>
            <span className="muted">{posts.data?.meta?.totalCount ?? 0} total</span>
          </div>
          <div className="post-list">
            {recentPosts.map((post) => (
              <article className="post-row" key={post.id}>
                <div className="post-main">
                  <span className="board-chip">{post.boardName ?? `board ${post.boardId}`}</span>
                  <strong>{post.title}</strong>
                </div>
                <div className="post-meta">
                  <span>{post.viewCount} views</span>
                  <span>{post.likeCount} likes</span>
                  <span>{post.commentCount} comments</span>
                </div>
              </article>
            ))}
            {recentPosts.length === 0 && <p className="empty-state">아직 표시할 게시글이 없습니다.</p>}
          </div>
        </section>
      </div>
    </div>
  );
}
