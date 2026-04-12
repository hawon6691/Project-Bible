export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8000",
  appDomain: import.meta.env.VITE_APP_DOMAIN ?? "unknown",
  appPort: Number(import.meta.env.VITE_APP_PORT ?? 3000),
};
