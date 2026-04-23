# 07 Folder TypeScript

## 문서 목적

이 문서는 `post` 서비스의 TypeScript 백엔드에서 사용하는 권장 폴더 구조를 정리하는 기준 문서다.
예시용 Nest 템플릿 구조가 아니라, 현재 `Project-Bible`의 대표 `post` TypeScript 구현체와 요구사항/API 문서를 기준으로 정리한다.

## 권장 폴더 트리

```text
project-root/
├─ src/
│  ├─ main.ts
│  ├─ app.module.ts
│  │
│  ├─ common/
│  │  └─ guards/
│  │
│  ├─ auth/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ users/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ admin/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ boards/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ posts/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ comments/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  └─ likes/
│     ├─ controller/
│     ├─ service/
│     ├─ request/
│     ├─ response/
│     ├─ entity/
│     └─ repository/
│
├─ test/
├─ package.json
└─ README.md
```

## 구조 원칙

- 공통 기능은 `common` 아래에 모은다.
- 도메인 모듈은 `auth`, `users`, `admin`, `boards`, `posts`, `comments`, `likes` 기준으로 나눈다.
- 각 도메인 모듈은 `controller`, `service`, `request`, `response`, `entity`, `repository` 6계층을 기본으로 사용한다.
- `request`는 입력 DTO, `response`는 출력 DTO를 분리해서 관리한다.
- `boards`는 게시판 관리, `posts`는 게시글 CRUD, `comments`는 댓글, `likes`는 좋아요 기능을 담당한다.
- 관리자 대시보드와 운영 API는 `admin` 모듈 안에서 관리한다.
- `main.ts`, `app.module.ts`는 애플리케이션 엔트리로 루트에 둔다.
