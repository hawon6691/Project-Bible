import { apiClient, unwrap, unwrapPaged } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { PostDetail, PostSummary } from "./model";

export const fetchPosts = (params?: Record<string, string | number>) =>
  unwrapPaged<PostSummary>(apiClient.get("/api/v1/posts", { params }));
export const fetchPostDetail = (postId: number) => unwrap<PostDetail>(apiClient.get(`/api/v1/posts/${postId}`));
export const createPost = (payload: { boardId: number; title: string; content: string }) =>
  unwrap<PostDetail>(apiClient.post("/api/v1/posts", payload, { headers: authHeader("user") }));
export const updatePost = (postId: number, payload: { title?: string; content?: string }) =>
  unwrap<PostDetail>(apiClient.patch(`/api/v1/posts/${postId}`, payload, { headers: authHeader("user") }));
export const deletePost = (postId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/posts/${postId}`, { headers: authHeader("user") }));
export const fetchAdminPosts = () =>
  unwrapPaged<PostSummary>(
    apiClient.get("/api/v1/admin/posts", { headers: authHeader("admin"), params: { page: 1, limit: 20 } }),
  );
export const changePostStatus = (postId: number, status: "ACTIVE" | "HIDDEN" | "DELETED") =>
  unwrap<PostSummary>(apiClient.patch(`/api/v1/admin/posts/${postId}/status`, { status }, { headers: authHeader("admin") }));
