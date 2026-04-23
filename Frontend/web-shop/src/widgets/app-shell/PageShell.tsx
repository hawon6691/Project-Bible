import { PropsWithChildren } from "react";
import { Link } from "react-router-dom";

interface Props extends PropsWithChildren {
  title: string;
  description: string;
}

const links = [
  ["/", "Home"],
  ["/login", "Login"],
  ["/signup", "Signup"],
  ["/products", "Products"],
  ["/product-detail", "Product Detail"],
  ["/cart", "Cart"],
  ["/checkout", "Checkout"],
  ["/orders", "Orders"],
  ["/mypage", "My Page"],
  ["/admin-dashboard", "Admin"],
  ["/admin-categories", "Admin Categories"],
  ["/admin-products", "Admin Products"],
  ["/admin-orders", "Admin Orders"],
  ["/admin-reviews", "Admin Reviews"],
];

export function PageShell({ title, description, children }: Props) {
  return (
    <main className="page">
      <nav className="nav">
        {links.map(([to, label]) => (
          <Link key={to} to={to}>
            {label}
          </Link>
        ))}
      </nav>
      <header className="header">
        <div className="eyebrow">Project-Bible Shop</div>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>
      <section className="panel">{children}</section>
    </main>
  );
}
