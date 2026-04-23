import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchProductDetail } from "../../entities/product/api";
import { fetchProductReviews } from "../../entities/review/api";
import { AddCartButton } from "../../features/cart-add/AddCartButton";
import { CreateReviewForm } from "../../features/review-create/CreateReviewForm";
import { UpdateReviewButton } from "../../features/review-update/UpdateReviewButton";

export function ProductDetailPanel() {
  const [productId, setProductId] = useState(1);
  const detail = useQuery({ queryKey: ["product-detail", productId], queryFn: () => fetchProductDetail(productId) });
  const reviews = useQuery({ queryKey: ["reviews", productId], queryFn: () => fetchProductReviews(productId) });

  return (
    <>
      <label>
        Product ID
        <input type="number" value={productId} onChange={(event) => setProductId(Number(event.target.value))} />
      </label>
      {detail.data && (
        <div className="card">
          <h2>{detail.data.name}</h2>
          <p>{detail.data.description}</p>
          <p>{Number(detail.data.price).toLocaleString()} KRW / stock {detail.data.stock}</p>
          <AddCartButton productId={productId} />
          <h3>Options</h3>
          <pre>{JSON.stringify(detail.data.options, null, 2)}</pre>
          <h3>Images</h3>
          <pre>{JSON.stringify(detail.data.images, null, 2)}</pre>
        </div>
      )}
      <CreateReviewForm productId={productId} />
      <h2>Reviews</h2>
      <div className="stack">
        {(reviews.data?.data ?? []).map((item) => (
          <div className="card row" key={item.id}>
            <span>{item.rating} / {item.content}</span>
            <UpdateReviewButton reviewId={item.id} productId={productId} content={item.content} />
          </div>
        ))}
      </div>
    </>
  );
}
