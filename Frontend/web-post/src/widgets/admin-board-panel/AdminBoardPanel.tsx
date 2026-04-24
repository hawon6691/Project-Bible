import { useQuery } from "@tanstack/react-query";
import { fetchBoards } from "../../entities/board/api";
import { AdminBoardManager } from "../../features/admin-board-manage/AdminBoardManager";

export function AdminBoardPanel() {
  const boards = useQuery({ queryKey: ["boards", "admin"], queryFn: fetchBoards });
  return (
    <section className="admin-stack">
      <div className="section-heading split">
        <div>
          <span className="eyebrow">Catalog</span>
          <h2>게시판 운영</h2>
        </div>
        <span className="pill">{boards.data?.length ?? 0} boards</span>
      </div>
      <AdminBoardManager boards={boards.data ?? []} />
    </section>
  );
}
