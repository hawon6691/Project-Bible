import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { fetchPosts } from "../../entities/post/api";
import { fetchHealth } from "../../entities/session/api";
import { env } from "../../shared/config/env";

export function HomeSummary() {
  const health = useQuery({ queryKey: ["health"], queryFn: fetchHealth });
  const boards = useQuery({ queryKey: ["boards", "home"], queryFn: fetchBoards });
  const posts = useQuery({ queryKey: ["posts", "home"], queryFn: () => fetchPosts({ page: 1, limit: 5 }) });

  return (
    <>
      <div className="grid">
        <div className="card">
          <h2>API</h2>
          <p className="muted">{env.apiBaseUrl}</p>
          <p>{health.isLoading ? "Checking..." : `${health.data?.status} / ${health.data?.service}`}</p>
        </div>
        <div className="card">
          <h2>Boards</h2>
          <p>{boards.data?.length ?? 0} boards loaded</p>
        </div>
        <div className="card">
          <h2>Posts</h2>
          <p>{posts.data?.meta?.totalCount ?? 0} total posts</p>
        </div>
      </div>
      <h2>Recent posts</h2>
      <div className="stack">
        {(posts.data?.data ?? []).map((post) => (
          <div className="card" key={post.id}>
            <strong>{post.title}</strong>
            <p className="muted">
              views {post.viewCount} / likes {post.likeCount} / comments {post.commentCount}
            </p>
          </div>
        ))}
      </div>
    </>
  );
}
