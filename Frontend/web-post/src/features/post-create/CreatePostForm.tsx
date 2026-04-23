import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createPost } from "../../entities/post/api";

export function CreatePostForm() {
  const queryClient = useQueryClient();
  const [boardId, setBoardId] = useState(1);
  const [title, setTitle] = useState("New post from frontend");
  const [content, setContent] = useState("Simple content");
  const create = useMutation({
    mutationFn: () => createPost({ boardId, title, content }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["posts"] }),
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    create.mutate();
  }

  return (
    <form className="stack" onSubmit={submit}>
      <input type="number" value={boardId} onChange={(event) => setBoardId(Number(event.target.value))} />
      <input value={title} onChange={(event) => setTitle(event.target.value)} />
      <textarea value={content} onChange={(event) => setContent(event.target.value)} />
      <button type="submit">Create</button>
      {create.error && <p className="error">{create.error.message}</p>}
    </form>
  );
}
