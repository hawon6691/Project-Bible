import { FormEvent, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createImage, createOption, createProduct, deleteProduct, updateProduct } from "../../entities/product/api";
import type { ProductSummary } from "../../entities/product/model";

export function AdminProductManager({ products }: { products: ProductSummary[] }) {
  const queryClient = useQueryClient();
  const [categoryId, setCategoryId] = useState(1);
  const [name, setName] = useState("frontend-product");
  const [price, setPrice] = useState(10000);
  const reload = () => queryClient.invalidateQueries({ queryKey: ["products"] });
  const create = useMutation({
    mutationFn: () => createProduct({ categoryId, name, description: "Created in frontend", price, stock: 10, status: "ACTIVE" }),
    onSuccess: reload,
  });

  function submit(event: FormEvent) {
    event.preventDefault();
    create.mutate();
  }

  return (
    <>
      <form className="grid" onSubmit={submit}>
        <input type="number" value={categoryId} onChange={(event) => setCategoryId(Number(event.target.value))} />
        <input value={name} onChange={(event) => setName(event.target.value)} />
        <input type="number" value={price} onChange={(event) => setPrice(Number(event.target.value))} />
        <button type="submit">Create product</button>
      </form>
      <div className="stack">
        {products.map((product) => (
          <div className="card stack" key={product.id}>
            <strong>#{product.id} {product.name}</strong>
            <span>{Number(product.price).toLocaleString()} KRW / {product.status}</span>
            <div className="row">
              <button type="button" onClick={() => updateProduct(product.id, { status: "HIDDEN" }).then(reload)}>Hide</button>
              <button className="secondary" type="button" onClick={() => createOption(product.id, { name: "size", value: "basic", additionalPrice: 0, stock: 5 })}>Add option</button>
              <button className="secondary" type="button" onClick={() => createImage(product.id, { imageUrl: "https://example.com/simple.jpg", isPrimary: false, displayOrder: 1 })}>Add image</button>
              <button className="secondary" type="button" onClick={() => deleteProduct(product.id).then(reload)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
