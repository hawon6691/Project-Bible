import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { fetchPosts } from "../../entities/post/api";
import { fetchHealth } from "../../entities/session/api";
import { env } from "../../shared/config/env";
import { ForumPostTable } from "../forum-post-table/ForumPostTable";

export function HomeSummary() {
  const health = useQuery({ queryKey: ["health"], queryFn: fetchHealth });
  const boards = useQuery({ queryKey: ["boards", "home"], queryFn: fetchBoards });
  const posts = useQuery({ queryKey: ["posts", "home"], queryFn: () => fetchPosts({ page: 1, limit: 15 }) });
  const healthLabel = health.isLoading ? "Checking" : health.data?.status ?? "Unavailable";
  const recentPosts = posts.data?.data ?? [];
  const hotPosts = [...recentPosts].sort((left, right) => {
    const leftScore = left.likeCount * 3 + left.commentCount * 2 + left.viewCount;
    const rightScore = right.likeCount * 3 + right.commentCount * 2 + right.viewCount;
    return rightScore - leftScore;
  }).slice(0, 5);

  return (
    <div className="community-board">
      <section className="gallery-headline">
        <div>
          <span className="gallery-label">post gallery</span>
          <h2>post 갤러리</h2>
          <p>실시간 글 목록과 인기글을 한 화면에서 확인합니다.</p>
        </div>
        <div className="gallery-counts">
          <span>전체글 <strong>{posts.data?.meta?.totalCount ?? 0}</strong></span>
          <span>댓글 <strong>{recentPosts.reduce((sum, post) => sum + post.commentCount, 0)}</strong></span>
        </div>
      </section>
      <div className="community-toolbar">
        <div className="board-tabs" aria-label="게시판 분류">
          <button type="button" className="active">전체</button>
          <button type="button">인기</button>
          <button type="button">공지</button>
          <button type="button">질문</button>
        </div>
        <div className="board-search">
          <input placeholder="제목, 내용, 글쓴이 검색" />
          <button type="button">검색</button>
        </div>
      </div>

      <div className="community-layout">
        <aside className="community-left">
          <section className="forum-box">
            <h2>게시판</h2>
            <div className="category-list">
              {(boards.data ?? []).map((board) => (
                <button type="button" key={board.id}>
                  <span>{board.name}</span>
                  <small>{board.status}</small>
                </button>
              ))}
            </div>
          </section>

          <section className="forum-box compact">
            <h2>서비스</h2>
            <p className="api-line">{env.apiBaseUrl}</p>
            <span className={health.data?.status === "UP" ? "pill success-pill" : "pill"}>{healthLabel}</span>
            {health.data?.service && <small className="api-line">{health.data.service}</small>}
          </section>
        </aside>

        <section className="community-main">
          <div className="forum-board-head">
            <div>
              <span className="eyebrow">Post Board</span>
              <h2>전체글</h2>
            </div>
            <div className="forum-stats">
              <span>{boards.data?.length ?? 0} boards</span>
              <span>{posts.data?.meta?.totalCount ?? 0} posts</span>
            </div>
          </div>
          <ForumPostTable posts={recentPosts} loading={posts.isLoading} error={Boolean(posts.error)} />
        </section>

        <aside className="community-right">
          <section className="forum-box">
            <h2>실시간 인기글</h2>
            <ol className="ranking-list">
              {hotPosts.map((post) => (
                <li key={post.id}>
                  <span>{post.title}</span>
                  <small>{post.likeCount} 추천</small>
                </li>
              ))}
            </ol>
          </section>

          <section className="forum-box compact">
            <h2>오늘의 요약</h2>
            <div className="mini-stat">
              <span>게시글</span>
              <strong>{posts.data?.meta?.totalCount ?? 0}</strong>
            </div>
            <div className="mini-stat">
              <span>댓글</span>
              <strong>{recentPosts.reduce((sum, post) => sum + post.commentCount, 0)}</strong>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}
