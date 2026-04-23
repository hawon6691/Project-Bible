import { apiClient, unwrap } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { BoardSummary } from "./model";

export const fetchBoards = () => unwrap<BoardSummary[]>(apiClient.get("/api/v1/boards"));
export const fetchBoard = (boardId: number) => unwrap<BoardSummary>(apiClient.get(`/api/v1/boards/${boardId}`));
export const createBoard = (payload: { name: string; description: string; displayOrder: number }) =>
  unwrap<BoardSummary>(apiClient.post("/api/v1/admin/boards", payload, { headers: authHeader("admin") }));
export const updateBoard = (boardId: number, payload: Partial<BoardSummary>) =>
  unwrap<BoardSummary>(apiClient.patch(`/api/v1/admin/boards/${boardId}`, payload, { headers: authHeader("admin") }));
export const deleteBoard = (boardId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/admin/boards/${boardId}`, { headers: authHeader("admin") }));
