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
  ["/boards", "Boards"],
  ["/post-detail", "Post Detail"],
  ["/mypage", "My Page"],
  ["/admin-dashboard", "Admin"],
  ["/admin-boards", "Admin Boards"],
  ["/admin-posts", "Admin Posts"],
  ["/admin-comments", "Admin Comments"],
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
        <div className="eyebrow">Project-Bible Post</div>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>
      <section className="panel">{children}</section>
    </main>
  );
}
