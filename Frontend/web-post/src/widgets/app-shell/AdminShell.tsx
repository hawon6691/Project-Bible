import { PropsWithChildren } from "react";
import { NavLink } from "react-router-dom";

interface Props extends PropsWithChildren {
  title: string;
  description: string;
}

const adminLinks = [
  ["/admin", "Dashboard"],
  ["/admin/boards", "Boards"],
  ["/admin/posts", "Posts"],
  ["/admin/comments", "Comments"],
  ["/login", "Login"],
  ["/boards", "Public Board"],
];

export function AdminShell({ title, description, children }: Props) {
  return (
    <main className="admin-page">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span className="brand-mark">PB</span>
          <div>
            <strong>Post Admin</strong>
            <span>Operations</span>
          </div>
        </div>
        <nav className="admin-nav" aria-label="Post admin navigation">
          {adminLinks.map(([to, label]) => (
            <NavLink key={to} to={to} end={to === "/admin"} className={({ isActive }) => (isActive ? "active" : undefined)}>
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <section className="admin-main">
        <header className="admin-header">
          <div className="eyebrow">Project-Bible Post Admin</div>
          <h1>{title}</h1>
          <p>{description}</p>
        </header>
        <div className="admin-panel">{children}</div>
      </section>
    </main>
  );
}
