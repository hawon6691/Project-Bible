import { apiClient, unwrap } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { AdminDashboard } from "./model";

export const fetchAdminDashboard = () =>
  unwrap<AdminDashboard>(apiClient.get("/api/v1/admin/dashboard", { headers: authHeader("admin") }));
