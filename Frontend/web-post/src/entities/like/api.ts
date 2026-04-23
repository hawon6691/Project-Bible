import { apiClient, unwrap } from "../../shared/api/client";
import { authHeader } from "../session/storage";

export const likePost = (postId: number) =>
  unwrap<{ liked: boolean }>(apiClient.post(`/api/v1/posts/${postId}/likes`, null, { headers: authHeader("user") }));
export const unlikePost = (postId: number) =>
  unwrap<{ liked: boolean }>(apiClient.delete(`/api/v1/posts/${postId}/likes`, { headers: authHeader("user") }));
