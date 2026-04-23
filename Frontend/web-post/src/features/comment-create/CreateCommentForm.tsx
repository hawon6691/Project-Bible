import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createComment } from "../../entities/comment/api";

interface Props {
  postId: number;
}

export function CreateCommentForm({ postId }: Props) {
  const queryClient = useQueryClient();
  const [content, setContent] = useState("Comment from frontend");
  const create = useMutation({
    mutationFn: () => createComment(postId, content),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["comments", postId] }),
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    create.mutate();
  }

  return (
    <form className="stack" onSubmit={submit}>
      <textarea value={content} onChange={(event) => setContent(event.target.value)} />
      <button type="submit">Add comment</button>
    </form>
  );
}
