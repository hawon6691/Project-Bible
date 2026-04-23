import { apiClient, unwrap, unwrapPaged } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { CommentSummary } from "./model";

export const fetchComments = (postId: number) =>
  unwrapPaged<CommentSummary>(apiClient.get(`/api/v1/posts/${postId}/comments`, { params: { page: 1, limit: 20 } }));
export const createComment = (postId: number, content: string) =>
  unwrap<CommentSummary>(apiClient.post(`/api/v1/posts/${postId}/comments`, { content }, { headers: authHeader("user") }));
export const updateComment = (commentId: number, content: string) =>
  unwrap<CommentSummary>(apiClient.patch(`/api/v1/comments/${commentId}`, { content }, { headers: authHeader("user") }));
export const deleteComment = (commentId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/comments/${commentId}`, { headers: authHeader("user") }));
export const fetchAdminComments = () =>
  unwrapPaged<CommentSummary>(
    apiClient.get("/api/v1/admin/comments", { headers: authHeader("admin"), params: { page: 1, limit: 20 } }),
  );
export const changeCommentStatus = (commentId: number, status: "ACTIVE" | "HIDDEN" | "DELETED") =>
  unwrap<CommentSummary>(
    apiClient.patch(`/api/v1/admin/comments/${commentId}/status`, { status }, { headers: authHeader("admin") }),
  );
