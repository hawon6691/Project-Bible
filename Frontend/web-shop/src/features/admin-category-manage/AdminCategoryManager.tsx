import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createCategory, deleteCategory, updateCategory } from "../../entities/category/api";
import type { CategorySummary } from "../../entities/category/model";

export function AdminCategoryManager({ categories }: { categories: CategorySummary[] }) {
  const queryClient = useQueryClient();
  const [name, setName] = useState("frontend-category");
  const [displayOrder, setDisplayOrder] = useState(10);
  const reload = () => queryClient.invalidateQueries({ queryKey: ["categories"] });
  const create = useMutation({ mutationFn: () => createCategory({ name, displayOrder }), onSuccess: reload });

  function submit(event: FormEvent) {
    event.preventDefault();
    create.mutate();
  }

  return (
    <>
      <form className="row" onSubmit={submit}>
        <input value={name} onChange={(event) => setName(event.target.value)} />
        <input type="number" value={displayOrder} onChange={(event) => setDisplayOrder(Number(event.target.value))} />
        <button type="submit">Create category</button>
      </form>
      <div className="stack">
        {categories.map((category) => (
          <div className="card row" key={category.id}>
            <strong>#{category.id} {category.name}</strong>
            <span>{category.status}</span>
            <button type="button" onClick={() => updateCategory(category.id, { status: "HIDDEN" }).then(reload)}>Hide</button>
            <button className="secondary" type="button" onClick={() => deleteCategory(category.id).then(reload)}>Delete</button>
          </div>
        ))}
      </div>
    </>
  );
}
