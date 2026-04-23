import { useQuery } from "@tanstack/react-query";
import { fetchAdminDashboard } from "../../entities/admin/api";
import { fetchAdminMe } from "../../entities/session/api";

export function AdminDashboardPanel() {
  const me = useQuery({ queryKey: ["admin-me"], queryFn: fetchAdminMe, retry: false });
  const dashboard = useQuery({ queryKey: ["admin-dashboard"], queryFn: fetchAdminDashboard, retry: false });

  return (
    <>
      {me.error && <p className="error">Login as admin first.</p>}
      {me.data && <p>Admin: {me.data.email}</p>}
      {dashboard.data && (
        <div className="grid">
          <div className="card">Boards: {dashboard.data.boardCount}</div>
          <div className="card">Posts: {dashboard.data.postCount}</div>
          <div className="card">Comments: {dashboard.data.commentCount}</div>
          <div className="card">Hidden posts: {dashboard.data.hiddenPostCount}</div>
          <div className="card">Hidden comments: {dashboard.data.hiddenCommentCount}</div>
        </div>
      )}
    </>
  );
}
