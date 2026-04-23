import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { AdminBoardManager } from "../../features/admin-board-manage/AdminBoardManager";

export function AdminBoardPanel() {
  const boards = useQuery({ queryKey: ["boards", "admin"], queryFn: fetchBoards });
  return <AdminBoardManager boards={boards.data ?? []} />;
}
