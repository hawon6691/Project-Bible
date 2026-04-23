import { useQuery } from "@tanstack/react-query";
import { fetchAdminPosts } from "../../entities/post/api";
import { AdminPostModeration } from "../../features/admin-post-moderate/AdminPostModeration";

export function AdminPostPanel() {
  const posts = useQuery({ queryKey: ["admin-posts"], queryFn: fetchAdminPosts });
  return (
    <>
      <p className="muted">Total: {posts.data?.meta?.totalCount ?? 0}</p>
      <AdminPostModeration posts={posts.data?.data ?? []} />
    </>
  );
}
