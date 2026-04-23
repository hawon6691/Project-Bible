import type { TokenPair } from "./model";

const userTokenKey = "web-post-user-access-token";
const userRefreshKey = "web-post-user-refresh-token";
const adminTokenKey = "web-post-admin-access-token";
const adminRefreshKey = "web-post-admin-refresh-token";

const read = (key: string) => window.localStorage.getItem(key) ?? "";
const write = (key: string, value: string) => window.localStorage.setItem(key, value);
const remove = (key: string) => window.localStorage.removeItem(key);

export function authHeader(kind: "user" | "admin") {
  const token = kind === "user" ? read(userTokenKey) : read(adminTokenKey);
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const sessionStore = {
  userRefresh: () => read(userRefreshKey),
  adminRefresh: () => read(adminRefreshKey),
  saveUser(tokens: TokenPair) {
    write(userTokenKey, tokens.accessToken);
    write(userRefreshKey, tokens.refreshToken);
  },
  saveAdmin(tokens: TokenPair) {
    write(adminTokenKey, tokens.accessToken);
    write(adminRefreshKey, tokens.refreshToken);
  },
  clearUser() {
    remove(userTokenKey);
    remove(userRefreshKey);
  },
  clearAdmin() {
    remove(adminTokenKey);
    remove(adminRefreshKey);
  },
};
