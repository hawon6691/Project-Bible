import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createBoard, deleteBoard, updateBoard } from "../../entities/board/api";
import type { BoardSummary } from "../../entities/board/model";

interface Props {
  boards: BoardSummary[];
}

export function AdminBoardManager({ boards }: Props) {
  const queryClient = useQueryClient();
  const [name, setName] = useState("frontend-board");
  const [description, setDescription] = useState("Board from frontend admin");
  const [displayOrder, setDisplayOrder] = useState(10);
  const reload = () => queryClient.invalidateQueries({ queryKey: ["boards"] });
  const create = useMutation({ mutationFn: () => createBoard({ name, description, displayOrder }), onSuccess: reload });

  function submit(event: FormEvent) {
    event.preventDefault();
    create.mutate();
  }

  return (
    <div className="admin-stack">
      <form className="admin-form" onSubmit={submit}>
        <label>
          Name
          <input value={name} onChange={(event) => setName(event.target.value)} />
        </label>
        <label>
          Description
          <input value={description} onChange={(event) => setDescription(event.target.value)} />
        </label>
        <label>
          Display order
          <input type="number" value={displayOrder} onChange={(event) => setDisplayOrder(Number(event.target.value))} />
        </label>
        <button type="submit">Create board</button>
      </form>
      <div className="admin-table">
        <div className="admin-table-head">
          <span>Board</span>
          <span>Status</span>
          <span>Actions</span>
        </div>
        {boards.map((board) => (
          <article className="admin-table-row" key={board.id}>
            <div>
              <strong>#{board.id} {board.name}</strong>
              <p className="muted">{board.description}</p>
            </div>
            <span className="pill">{board.status}</span>
            <div className="row">
              <button className="secondary" type="button" onClick={() => updateBoard(board.id, { status: "HIDDEN" }).then(reload)}>
                Hide
              </button>
              <button className="secondary danger" type="button" onClick={() => deleteBoard(board.id).then(reload)}>
                Delete
              </button>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
