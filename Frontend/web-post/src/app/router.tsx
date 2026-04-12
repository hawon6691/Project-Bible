import { createBrowserRouter } from "react-router-dom";
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
  { path: "/admin-dashboard", element: <AdminDashboardPage /> },
  { path: "/admin-boards", element: <AdminBoardsPage /> },
  { path: "/admin-posts", element: <AdminPostsPage /> },
  { path: "/admin-comments", element: <AdminCommentsPage /> },
]);
