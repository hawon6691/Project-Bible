import type { PostSummary } from "../../entities/post/model";
import { compactNumber, formatBoardDate } from "../../shared/lib/format";

interface Props {
  posts: PostSummary[];
  loading?: boolean;
  error?: boolean;
  emptyText?: string;
}

export function ForumPostTable({ posts, loading = false, error = false, emptyText = "표시할 게시글이 없습니다." }: Props) {
  return (
    <div className="forum-table">
      <div className="forum-table-head">
        <span>번호</span>
        <span>이미지</span>
        <span>제목</span>
        <span>글쓴이</span>
        <span>조회</span>
        <span>추천</span>
        <span>날짜</span>
      </div>
      <article className="forum-row notice-row">
        <span className="forum-no">공지</span>
        <span className="forum-thumb notice-thumb">공지</span>
        <div className="forum-title-cell">
          <span className="notice-chip">공지</span>
          <strong>post 갤러리 이용 안내</strong>
        </div>
        <span className="forum-author" data-label="글쓴이">운영자</span>
        <span className="forum-views" data-label="조회">-</span>
        <span className="forum-likes" data-label="추천">-</span>
        <span className="forum-date" data-label="날짜">상시</span>
      </article>
      {loading && <p className="empty-state">게시글을 불러오는 중입니다.</p>}
      {error && <p className="error empty-state">게시글을 불러오지 못했습니다.</p>}
      {posts.map((post) => (
        <article className="forum-row" key={post.id}>
          <span className="forum-no">{post.id}</span>
          <span className={`forum-thumb thumb-${post.id % 6}`}>{post.boardName?.slice(0, 2) ?? "PB"}</span>
          <div className="forum-title-cell">
            <span className="board-chip">{post.boardName ?? `board ${post.boardId}`}</span>
            <strong>{post.title}</strong>
            {post.commentCount > 0 && <span className="comment-badge">[{post.commentCount}]</span>}
          </div>
          <span className="forum-author" data-label="글쓴이">{post.author?.nickname ?? "익명"}</span>
          <span className="forum-views" data-label="조회">{compactNumber(post.viewCount)}</span>
          <span className="forum-likes" data-label="추천">{compactNumber(post.likeCount)}</span>
          <span className="forum-date" data-label="날짜">{formatBoardDate(post.createdAt)}</span>
        </article>
      ))}
      {posts.length === 0 && !loading && !error && <p className="empty-state">{emptyText}</p>}
    </div>
  );
}
