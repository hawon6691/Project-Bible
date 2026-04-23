import { apiClient, unwrap } from "../../shared/api/client";
import type { HealthPayload } from "../../shared/types";
import type { AdminMe, TokenPair, UserMe } from "./model";
import { authHeader, sessionStore } from "./storage";

export const fetchHealth = () => unwrap<HealthPayload>(apiClient.get("/api/v1/health"));

export const signupUser = (payload: { email: string; password: string; nickname: string }) =>
  unwrap<UserMe>(apiClient.post("/api/v1/auth/signup", payload));

export async function loginUser(payload: { email: string; password: string }) {
  const tokens = await unwrap<TokenPair>(apiClient.post("/api/v1/auth/login", payload));
  sessionStore.saveUser(tokens);
  return tokens;
}

export async function refreshUser() {
  const tokens = await unwrap<TokenPair>(
    apiClient.post("/api/v1/auth/refresh", { refreshToken: sessionStore.userRefresh() }),
  );
  sessionStore.saveUser(tokens);
  return tokens;
}

export async function logoutUser() {
  const result = await unwrap<{ message: string }>(
    apiClient.post("/api/v1/auth/logout", null, { headers: authHeader("user") }),
  );
  sessionStore.clearUser();
  return result;
}

export const fetchMe = () => unwrap<UserMe>(apiClient.get("/api/v1/users/me", { headers: authHeader("user") }));

export const updateMe = (payload: { nickname: string }) =>
  unwrap<UserMe>(apiClient.patch("/api/v1/users/me", payload, { headers: authHeader("user") }));

export async function loginAdmin(payload: { email: string; password: string }) {
  const tokens = await unwrap<TokenPair>(apiClient.post("/api/v1/admin/auth/login", payload));
  sessionStore.saveAdmin(tokens);
  return tokens;
}

export async function refreshAdmin() {
  const tokens = await unwrap<TokenPair>(
    apiClient.post("/api/v1/admin/auth/refresh", { refreshToken: sessionStore.adminRefresh() }),
  );
  sessionStore.saveAdmin(tokens);
  return tokens;
}

export async function logoutAdmin() {
  const result = await unwrap<{ message: string }>(
    apiClient.post("/api/v1/admin/auth/logout", null, { headers: authHeader("admin") }),
  );
  sessionStore.clearAdmin();
  return result;
}

export const fetchAdminMe = () =>
  unwrap<AdminMe>(apiClient.get("/api/v1/admin/me", { headers: authHeader("admin") }));
