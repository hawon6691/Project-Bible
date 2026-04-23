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
      await route.fulfill({ json: envelope({ status: "UP", service: "web-shop-smoke" }) });
      return;
    }
    if (path === "/api/v1/categories") {
      await route.fulfill({ json: envelope([{ id: 1, name: "Books", displayOrder: 1, status: "ACTIVE" }]) });
      return;
    }
    if (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/admin/products")) {
      await route.fulfill({
        json: envelope(
          [{ id: 1, categoryId: 1, name: "Smoke product", price: 10000, stock: 10, status: "ACTIVE" }],
          { page: 1, limit: 20, totalCount: 1, totalPages: 1 },
        ),
      });
      return;
    }
    if (path.startsWith("/api/v1/cart-items")) {
      await route.fulfill({ json: envelope([{ id: 1, productId: 1, productOptionId: 1, quantity: 1 }]) });
      return;
    }
    if (path.startsWith("/api/v1/addresses")) {
      await route.fulfill({ json: envelope([{ id: 1, recipientName: "Smoke", address1: "Seoul", isDefault: true }]) });
      return;
    }
    if (path.startsWith("/api/v1/orders") || path.startsWith("/api/v1/admin/orders")) {
      await route.fulfill({
        json: envelope(
          [{ id: 1, orderNumber: "SMOKE-1", orderStatus: "PAID", totalAmount: 10000 }],
          { page: 1, limit: 20, totalCount: 1, totalPages: 1 },
        ),
      });
      return;
    }
    if (path.startsWith("/api/v1/admin/reviews") || path.includes("/reviews")) {
      await route.fulfill({
        json: envelope(
          [{ id: 1, productId: 1, rating: 5, content: "Smoke review", status: "ACTIVE" }],
          { page: 1, limit: 20, totalCount: 1, totalPages: 1 },
        ),
      });
      return;
    }
    if (path === "/api/v1/admin/dashboard") {
      await route.fulfill({ json: envelope({ orderCount: 1, productCount: 1, userCount: 1, reviewCount: 1 }) });
      return;
    }
    if (path.includes("/auth/login")) {
      await route.fulfill({ json: envelope({ accessToken: "access-token", refreshToken: "refresh-token" }) });
      return;
    }

    await route.fulfill({ json: envelope({ message: "ok" }) });
  });
});

test("navigates core shop screens", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("h1")).toHaveText("Shop Home");

  const screens = [
    ["Login", "Login"],
    ["Signup", "Signup"],
    ["Products", "Products"],
    ["Product Detail", "Product Detail"],
    ["Cart", "Cart"],
    ["Checkout", "Checkout"],
    ["Orders", "Orders"],
    ["My Page", "My Page"],
    ["Admin", "Admin Dashboard"],
    ["Admin Categories", "Admin Categories"],
    ["Admin Products", "Admin Products"],
    ["Admin Orders", "Admin Orders"],
    ["Admin Reviews", "Admin Reviews"],
  ] as const;

  for (const [link, heading] of screens) {
    await page.getByRole("link", { name: link, exact: true }).click();
    await expect(page.locator("h1")).toHaveText(heading);
  }
});
