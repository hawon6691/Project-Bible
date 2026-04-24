import { PropsWithChildren } from "react";
import { NavLink } from "react-router-dom";

interface Props extends PropsWithChildren {
  title: string;
  description: string;
  kicker?: string;
}

const links = [
  ["/", "Home"],
  ["/boards", "Boards"],
  ["/post-detail", "Post Detail"],
  ["/mypage", "My Page"],
  ["/login", "Login"],
  ["/signup", "Signup"],
  ["/admin", "Admin"],
];

export function PageShell({ title, description, kicker = "Project-Bible Post", children }: Props) {
  return (
    <main className="page">
      <nav className="nav" aria-label="Post navigation">
        <div className="brand">
          <span className="brand-mark">PB</span>
          <span>Post Board</span>
        </div>
        {links.map(([to, label]) => (
          <NavLink key={to} to={to} className={({ isActive }) => (isActive ? "active" : undefined)}>
            {label}
          </NavLink>
        ))}
      </nav>
      <header className="header">
        <div className="eyebrow">{kicker}</div>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>
      <section className="panel">{children}</section>
    </main>
  );
}
