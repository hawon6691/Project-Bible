import { Navigate, createBrowserRouter } from "react-router-dom";
import { HomePage } from "../pages/home/page";
import { LoginPage } from "../pages/login/page";
import { SignupPage } from "../pages/signup/page";
import { BoardsPage } from "../pages/boards/page";
import { PostDetailPage } from "../pages/post-detail/page";
import { MypagePage } from "../pages/mypage/page";
import { AdminDashboardPage } from "../pages/admin-dashboard/page";
import { AdminBoardsPage } from "../pages/admin-boards/page";
import { AdminPostsPage } from "../pages/admin-posts/page";
import { AdminCommentsPage } from "../pages/admin-comments/page";

export const router = createBrowserRouter([
  { path: "/", element: <HomePage /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/signup", element: <SignupPage /> },
  { path: "/boards", element: <BoardsPage /> },
  { path: "/post-detail", element: <PostDetailPage /> },
  { path: "/mypage", element: <MypagePage /> },
  { path: "/admin", element: <AdminDashboardPage /> },
  { path: "/admin/boards", element: <AdminBoardsPage /> },
  { path: "/admin/posts", element: <AdminPostsPage /> },
  { path: "/admin/comments", element: <AdminCommentsPage /> },
  { path: "/admin-dashboard", element: <Navigate to="/admin" replace /> },
  { path: "/admin-boards", element: <Navigate to="/admin/boards" replace /> },
  { path: "/admin-posts", element: <Navigate to="/admin/posts" replace /> },
  { path: "/admin-comments", element: <Navigate to="/admin/comments" replace /> },
]);
