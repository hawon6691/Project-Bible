import { useQuery } from "@tanstack/react-query";
import { fetchAdminPosts } from "../../entities/post/api";
import { AdminPostModeration } from "../../features/admin-post-moderate/AdminPostModeration";

export function AdminPostPanel() {
  const posts = useQuery({ queryKey: ["admin-posts"], queryFn: fetchAdminPosts });
  return (
    <section className="admin-stack">
      <div className="section-heading split">
        <div>
          <span className="eyebrow">Queue</span>
          <h2>게시글 운영</h2>
        </div>
        <span className="pill">{posts.data?.meta?.totalCount ?? 0} posts</span>
      </div>
      <AdminPostModeration posts={posts.data?.data ?? []} />
    </section>
  );
}
