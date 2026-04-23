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
    <div className="stack">
      {posts.map((post) => (
        <div className="card row" key={post.id}>
          <strong>#{post.id} {post.title}</strong>
          <span>{post.status}</span>
          <button type="button" onClick={() => changePostStatus(post.id, "ACTIVE").then(reload)}>Active</button>
          <button className="secondary" type="button" onClick={() => changePostStatus(post.id, "HIDDEN").then(reload)}>
            Hidden
          </button>
        </div>
      ))}
    </div>
  );
}
