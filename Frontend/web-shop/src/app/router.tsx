import { createBrowserRouter } from "react-router-dom";
import { HomePage } from "../pages/home/page";
import { LoginPage } from "../pages/login/page";
import { SignupPage } from "../pages/signup/page";
import { ProductsPage } from "../pages/products/page";
import { ProductDetailPage } from "../pages/product-detail/page";
import { CartPage } from "../pages/cart/page";
import { CheckoutPage } from "../pages/checkout/page";
import { OrdersPage } from "../pages/orders/page";
import { MypagePage } from "../pages/mypage/page";
import { AdminDashboardPage } from "../pages/admin-dashboard/page";
import { AdminCategoriesPage } from "../pages/admin-categories/page";
import { AdminProductsPage } from "../pages/admin-products/page";
import { AdminOrdersPage } from "../pages/admin-orders/page";
import { AdminReviewsPage } from "../pages/admin-reviews/page";

export const router = createBrowserRouter([
  { path: "/", element: <HomePage /> },
  { path: "/login", element: <LoginPage /> },
  { path: "/signup", element: <SignupPage /> },
  { path: "/products", element: <ProductsPage /> },
  { path: "/product-detail", element: <ProductDetailPage /> },
  { path: "/cart", element: <CartPage /> },
  { path: "/checkout", element: <CheckoutPage /> },
  { path: "/orders", element: <OrdersPage /> },
  { path: "/mypage", element: <MypagePage /> },
  { path: "/admin-dashboard", element: <AdminDashboardPage /> },
  { path: "/admin-categories", element: <AdminCategoriesPage /> },
  { path: "/admin-products", element: <AdminProductsPage /> },
  { path: "/admin-orders", element: <AdminOrdersPage /> },
  { path: "/admin-reviews", element: <AdminReviewsPage /> },
]);
