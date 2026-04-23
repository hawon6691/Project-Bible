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
    <>
      <form className="grid" onSubmit={submit}>
        <input value={name} onChange={(event) => setName(event.target.value)} />
        <input value={description} onChange={(event) => setDescription(event.target.value)} />
        <input type="number" value={displayOrder} onChange={(event) => setDisplayOrder(Number(event.target.value))} />
        <button type="submit">Create board</button>
      </form>
      <div className="stack">
        {boards.map((board) => (
          <div className="card row" key={board.id}>
            <strong>#{board.id} {board.name}</strong>
            <span>{board.status}</span>
            <button className="secondary" type="button" onClick={() => updateBoard(board.id, { status: "HIDDEN" }).then(reload)}>
              Hide
            </button>
            <button className="secondary" type="button" onClick={() => deleteBoard(board.id).then(reload)}>
              Delete
            </button>
          </div>
        ))}
      </div>
    </>
  );
}
