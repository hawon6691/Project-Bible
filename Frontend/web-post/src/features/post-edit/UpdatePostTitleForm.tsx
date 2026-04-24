import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updatePost } from "../../entities/post/api";

interface Props {
  postId: number;
}

export function UpdatePostTitleForm({ postId }: Props) {
  const queryClient = useQueryClient();
  const [title, setTitle] = useState("");
  const update = useMutation({
    mutationFn: () => updatePost(postId, { title }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["post-detail", postId] }),
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    update.mutate();
  }

  return (
    <form className="stack compact-form" onSubmit={submit}>
      <label>
        New title
        <input placeholder="New title" value={title} onChange={(event) => setTitle(event.target.value)} />
      </label>
      <button type="submit">Update title</button>
    </form>
  );
}
