import { useQuery } from "@tanstack/react-query";
import { fetchAdminDashboard } from "../../entities/admin/api";
import { fetchAdminMe } from "../../entities/session/api";

export function AdminDashboardPanel() {
  const me = useQuery({ queryKey: ["admin-me"], queryFn: fetchAdminMe, retry: false });
  const dashboard = useQuery({ queryKey: ["admin-dashboard"], queryFn: fetchAdminDashboard, retry: false });

  return (
    <div className="admin-stack">
      {me.error && <p className="error notice">Login as admin first.</p>}
      {me.data && (
        <div className="admin-account">
          <span className="eyebrow">Signed in</span>
          <strong>{me.data.email}</strong>
        </div>
      )}
      {dashboard.data && (
        <div className="metric-grid admin-metrics">
          <div className="metric-card">
            <span>Boards</span>
            <strong>{dashboard.data.boardCount}</strong>
            <small>managed board groups</small>
          </div>
          <div className="metric-card">
            <span>Posts</span>
            <strong>{dashboard.data.postCount}</strong>
            <small>published and hidden</small>
          </div>
          <div className="metric-card">
            <span>Comments</span>
            <strong>{dashboard.data.commentCount}</strong>
            <small>thread replies</small>
          </div>
          <div className="metric-card warning">
            <span>Hidden posts</span>
            <strong>{dashboard.data.hiddenPostCount}</strong>
            <small>moderation queue</small>
          </div>
          <div className="metric-card warning">
            <span>Hidden comments</span>
            <strong>{dashboard.data.hiddenCommentCount}</strong>
            <small>comment moderation</small>
          </div>
        </div>
      )}
    </div>
  );
}
