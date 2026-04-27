import { PropsWithChildren } from "react";
import { NavLink } from "react-router-dom";

interface Props extends PropsWithChildren {
  title: string;
  description: string;
  kicker?: string;
}

const mainLinks = [
  ["/", "실시간 베스트"],
  ["/boards", "갤러리"],
  ["/post-detail", "게시글"],
  ["/mypage", "마이페이지"],
  ["/admin", "관리자"],
];

const accountLinks = [
  ["/login", "로그인"],
  ["/signup", "회원가입"],
];

export function PageShell({ title, description, kicker = "Project-Bible Post", children }: Props) {
  return (
    <main className="page dc-page">
      <header className="masthead">
        <div className="masthead-inner">
          <NavLink to="/" className="wordmark" aria-label="Project Bible home">
            Connecting Code
          </NavLink>
          <form className="global-search" onSubmit={(event) => event.preventDefault()}>
            <input aria-label="통합검색" placeholder="갤러리 & 통합검색" />
            <button type="submit">검색</button>
          </form>
          <div className="masthead-count">
            총 게시글 수 <strong>Project-Bible</strong>
          </div>
        </div>
      </header>
      <nav className="nav" aria-label="Post navigation">
        <div className="nav-inner">
          {mainLinks.map(([to, label]) => (
            <NavLink key={to} to={to} className={({ isActive }) => (isActive ? "active" : undefined)}>
              {label}
            </NavLink>
          ))}
          <span className="nav-spacer" />
          {accountLinks.map(([to, label]) => (
            <NavLink key={to} to={to} className={({ isActive }) => (isActive ? "active" : undefined)}>
              {label}
            </NavLink>
          ))}
        </div>
      </nav>
      <div className="recent-bar">
        <button type="button" className="recent-button">최근 방문 갤러리</button>
        <span>post</span>
        <span>free</span>
        <span>qna</span>
      </div>
      <header className="header gallery-page-header">
        <div className="eyebrow">{kicker}</div>
        <h1>{title}</h1>
        <p>{description}</p>
      </header>
      <section className="panel">{children}</section>
    </main>
  );
}
