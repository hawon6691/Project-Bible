import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { fetchPosts } from "../../entities/post/api";
import { CreatePostForm } from "../../features/post-create/CreatePostForm";

export function BoardPostsPanel() {
  const boards = useQuery({ queryKey: ["boards"], queryFn: fetchBoards });
  const posts = useQuery({ queryKey: ["posts"], queryFn: () => fetchPosts({ page: 1, limit: 20, sort: "latest" }) });

  return (
    <>
      <div className="grid">
        <section>
          <h2>Boards</h2>
          <div className="stack">
            {(boards.data ?? []).map((board) => (
              <div className="card" key={board.id}>
                <strong>{board.name}</strong>
                <p>{board.description}</p>
                <p className="muted">#{board.id} / {board.status}</p>
              </div>
            ))}
          </div>
        </section>
        <section>
          <h2>Create post</h2>
          <CreatePostForm />
        </section>
      </div>
      <h2>Posts</h2>
      <div className="stack">
        {(posts.data?.data ?? []).map((post) => (
          <div className="card" key={post.id}>
            <strong>#{post.id} {post.title}</strong>
            <p className="muted">
              board {post.boardId} / views {post.viewCount} / likes {post.likeCount} / comments {post.commentCount} / {post.status}
            </p>
          </div>
        ))}
      </div>
    </>
  );
}
