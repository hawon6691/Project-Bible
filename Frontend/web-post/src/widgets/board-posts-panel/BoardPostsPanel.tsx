import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { fetchPosts } from "../../entities/post/api";
import { CreatePostForm } from "../../features/post-create/CreatePostForm";
import { ForumPostTable } from "../forum-post-table/ForumPostTable";

export function BoardPostsPanel() {
  const boards = useQuery({ queryKey: ["boards"], queryFn: fetchBoards });
  const posts = useQuery({ queryKey: ["posts"], queryFn: () => fetchPosts({ page: 1, limit: 30, sort: "latest" }) });
  const postRows = posts.data?.data ?? [];
  const hotPosts = [...postRows]
    .sort((left, right) => right.likeCount + right.commentCount - (left.likeCount + left.commentCount))
    .slice(0, 6);

  return (
    <div className="community-board">
      <section className="gallery-headline">
        <div>
          <span className="gallery-label">post gallery</span>
          <h2>post 갤러리</h2>
          <p>글번호, 제목, 글쓴이, 조회, 추천을 빠르게 훑는 갤러리형 게시판입니다.</p>
        </div>
        <div className="gallery-counts">
          <span>전체글 <strong>{posts.data?.meta?.totalCount ?? 0}</strong></span>
          <span>게시판 <strong>{boards.data?.length ?? 0}</strong></span>
        </div>
      </section>
      <div className="community-toolbar">
        <div className="board-tabs" aria-label="게시글 정렬">
          <button type="button" className="active">전체</button>
          <button type="button">개념글</button>
          <button type="button">댓글많은글</button>
          <button type="button">조회많은글</button>
        </div>
        <div className="board-search">
          <input placeholder="검색어 입력" />
          <button type="button">검색</button>
        </div>
      </div>

      <div className="community-layout board-page-layout">
        <aside className="community-left">
          <section className="forum-box">
            <h2>갤러리</h2>
            <div className="category-list">
              {boards.isLoading && <p className="muted">게시판을 불러오는 중입니다.</p>}
              {boards.error && <p className="error">게시판을 불러오지 못했습니다.</p>}
              {(boards.data ?? []).map((board) => (
                <button type="button" key={board.id}>
                  <span>{board.name}</span>
                  <small>{board.description}</small>
                </button>
              ))}
            </div>
          </section>

          <section className="forum-box write-box">
            <h2>글쓰기</h2>
            <CreatePostForm />
          </section>
        </aside>

        <section className="community-main">
          <div className="forum-board-head">
            <div>
              <span className="eyebrow">All posts</span>
              <h2>전체 게시글</h2>
            </div>
            <div className="forum-stats">
              <span>{posts.data?.meta?.totalCount ?? 0} posts</span>
              <button type="button">글쓰기</button>
            </div>
          </div>
          <ForumPostTable posts={postRows} loading={posts.isLoading} error={Boolean(posts.error)} />
        </section>

        <aside className="community-right">
          <section className="forum-box">
            <h2>개념글</h2>
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
            <h2>게시판 정보</h2>
            <div className="mini-stat">
              <span>전체글</span>
              <strong>{posts.data?.meta?.totalCount ?? 0}</strong>
            </div>
            <div className="mini-stat">
              <span>게시판</span>
              <strong>{boards.data?.length ?? 0}</strong>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}
