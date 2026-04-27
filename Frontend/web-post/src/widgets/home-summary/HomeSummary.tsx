import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { fetchPosts } from "../../entities/post/api";
import { ForumPostTable } from "../forum-post-table/ForumPostTable";

const fallbackTopics = [
  "Spring Boot 게시판 인증 흐름 정리",
  "React Query 캐시 무효화 패턴",
  "관리자 댓글 모더레이션 화면 설계",
  "PostgreSQL 시드 데이터 확인",
];

const footerSections = [
  ["개념글", ["게임", "연예/방송", "스포츠", "여행/음식/생물", "취미/생활"]],
  ["인기갤러리", ["post", "free", "qna", "notice-like", "admin"]],
  ["주요 메뉴", ["글쓰기", "게시글", "마이페이지", "관리자"]],
  ["갤러리 순회", ["Spring Boot", "React", "Database", "CLI", "문서"]],
  ["디시미디어", ["프로젝트 로그", "릴리즈 노트", "테스트 결과", "운영 메모"]],
] satisfies Array<[string, string[]]>;

export function HomeSummary() {
  const boards = useQuery({ queryKey: ["boards", "home"], queryFn: fetchBoards });
  const posts = useQuery({ queryKey: ["posts", "home"], queryFn: () => fetchPosts({ page: 1, limit: 15 }) });
  const recentPosts = posts.data?.data ?? [];
  const hotPosts = [...recentPosts].sort((left, right) => {
    const leftScore = left.likeCount * 3 + left.commentCount * 2 + left.viewCount;
    const rightScore = right.likeCount * 3 + right.commentCount * 2 + right.viewCount;
    return rightScore - leftScore;
  }).slice(0, 5);
  const featured = [...hotPosts.map((post) => post.title), ...fallbackTopics].slice(0, 4);

  return (
    <div className="community-board">
      <div className="community-toolbar">
        <div className="board-tabs" aria-label="게시판 분류">
          <button type="button" className="active">실시간 베스트</button>
          <button type="button">실베라이트</button>
          <button type="button">개념글</button>
          <button type="button">랭킹</button>
        </div>
        <div className="board-search">
          <input placeholder="제목, 내용, 글쓴이 검색" />
          <button type="button">검색</button>
        </div>
      </div>

      <section className="live-best-strip" aria-label="실시간 베스트">
        {featured.map((title, index) => (
          <article className="best-card" key={`${title}-${index}`}>
            <div className={`best-thumb thumb-${index % 6}`}>{index + 1}</div>
            <strong>{title}</strong>
          </article>
        ))}
      </section>

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
            <h2>최근 방문 갤러리</h2>
            <div className="quick-links">
              {(boards.data ?? []).slice(0, 4).map((board) => (
                <button type="button" key={board.id}>{board.name}</button>
              ))}
              {(boards.data ?? []).length === 0 && <span className="muted">방문 기록이 없습니다.</span>}
            </div>
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
            <h2>실북갤</h2>
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
            <h2>HOT 흥한글</h2>
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

      <section className="gallery-link-board">
        <h2>갤러리</h2>
        <div className="gallery-link-grid">
          {footerSections.map(([title, items]) => (
            <div className="gallery-link-column" key={title}>
              <strong>{title}</strong>
              {items.map((item) => (
                <span key={item}>{item}</span>
              ))}
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
