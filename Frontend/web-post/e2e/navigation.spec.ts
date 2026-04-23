import { expect, test } from "@playwright/test";

const envelope = (data: unknown, meta?: unknown) => ({
  success: true,
  data,
  ...(meta ? { meta } : {}),
});

test.beforeEach(async ({ page }) => {
  await page.route("**/api/v1/**", async (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname;

    if (path === "/api/v1/health") {
      await route.fulfill({ json: envelope({ status: "UP", service: "web-post-smoke" }) });
      return;
    }
    if (path === "/api/v1/boards") {
      await route.fulfill({ json: envelope([{ id: 1, name: "notice", description: "Notice", displayOrder: 1, status: "ACTIVE" }]) });
      return;
    }
    if (path.startsWith("/api/v1/posts") || path.startsWith("/api/v1/admin/posts")) {
      await route.fulfill({
        json: envelope(
          [{ id: 1, boardId: 1, title: "Smoke post", viewCount: 1, likeCount: 0, commentCount: 0, status: "ACTIVE" }],
          { page: 1, limit: 20, totalCount: 1, totalPages: 1 },
        ),
      });
      return;
    }
    if (path.startsWith("/api/v1/admin/comments")) {
      await route.fulfill({
        json: envelope(
          [{ id: 1, postId: 1, content: "Smoke comment", status: "ACTIVE" }],
          { page: 1, limit: 20, totalCount: 1, totalPages: 1 },
        ),
      });
      return;
    }
    if (path === "/api/v1/admin/dashboard") {
      await route.fulfill({ json: envelope({ boardCount: 1, postCount: 1, commentCount: 1, moderationTargetCount: 0 }) });
      return;
    }
    if (path.includes("/auth/login")) {
      await route.fulfill({ json: envelope({ accessToken: "access-token", refreshToken: "refresh-token" }) });
      return;
    }

    await route.fulfill({ json: envelope({ message: "ok" }) });
  });
});

test("navigates core post screens", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("h1")).toHaveText("Post Home");

  const screens = [
    ["Login", "Login"],
    ["Signup", "Signup"],
    ["Boards", "Boards"],
    ["Post Detail", "Post Detail"],
    ["My Page", "My Page"],
    ["Admin", "Admin Dashboard"],
    ["Admin Boards", "Admin Boards"],
    ["Admin Posts", "Admin Posts"],
    ["Admin Comments", "Admin Comments"],
  ] as const;

  for (const [link, heading] of screens) {
    await page.getByRole("link", { name: link, exact: true }).click();
    await expect(page.locator("h1")).toHaveText(heading);
  }
});
