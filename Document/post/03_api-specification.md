# 게시판 서비스 API 명세서

> Base URL: `/api/v1`
> 관리자 Base URL: `/api/v1/admin`
> 인증: `Authorization: Bearer {accessToken}`
> 공통 응답 형식: `{ success, data, meta?, error? }`
> 목록 응답 `meta`: `{ page, limit, totalCount, totalPages }`

---

## 1. 문서 공통 규칙

- 일반 사용자 API는 `/api/v1/...` 경로를 사용한다.
- 관리자 API는 `/api/v1/admin/...` 경로를 사용한다.
- 성공 응답은 모두 envelope 형식을 사용한다.
- 권한 표기는 `Public`, `User`, `Admin`을 사용한다.
- 목록 조회 기본 쿼리는 `page`, `limit`이며, 검색/정렬이 필요한 경우 `search`, `sort`, `status`, `boardId`를 사용한다.
- 게시글/댓글 상태값은 `ACTIVE`, `HIDDEN`, `DELETED`를 기준으로 사용한다.

---

## 2. 사용자 인증 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| POST | `/auth/signup` | 회원가입 | Public | `{ email, password, nickname }` | `{ id, email, nickname }` |
| POST | `/auth/login` | 사용자 로그인 | Public | `{ email, password }` | `{ accessToken, refreshToken, expiresIn, user }` |
| POST | `/auth/logout` | 사용자 로그아웃 | User | - | `{ message }` |
| POST | `/auth/refresh` | 사용자 토큰 갱신 | Public | `{ refreshToken }` | `{ accessToken, refreshToken, expiresIn }` |
| GET | `/users/me` | 내 정보 조회 | User | - | `{ id, email, nickname, status, createdAt }` |
| PATCH | `/users/me` | 내 정보 수정 | User | `{ nickname?, password? }` | `{ id, email, nickname, status, updatedAt }` |
| DELETE | `/users/me` | 회원 탈퇴 | User | - | `{ message }` |

---

## 3. 관리자 인증 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| POST | `/admin/auth/login` | 관리자 로그인 | Admin | `{ email, password }` | `{ accessToken, refreshToken, expiresIn, admin }` |
| POST | `/admin/auth/logout` | 관리자 로그아웃 | Admin | - | `{ message }` |
| POST | `/admin/auth/refresh` | 관리자 토큰 갱신 | Admin | `{ refreshToken }` | `{ accessToken, refreshToken, expiresIn }` |
| GET | `/admin/me` | 관리자 정보 조회 | Admin | - | `{ id, email, name, status, createdAt }` |

---

## 4. 게시판 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/boards` | 게시판 목록 조회 | Public | `?status?` | `Board[]` |
| GET | `/boards/:boardId` | 게시판 단건 조회 | Public | - | `Board` |
| POST | `/admin/boards` | 게시판 생성 | Admin | `{ name, description?, displayOrder }` | `Board` |
| PATCH | `/admin/boards/:boardId` | 게시판 수정 | Admin | `{ name?, description?, displayOrder?, status? }` | `Board` |
| DELETE | `/admin/boards/:boardId` | 게시판 삭제 또는 비활성화 | Admin | - | `{ message }` |

---

## 5. 게시글 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/posts` | 게시글 목록 조회 | Public | `?page&limit&boardId&search&sort&status?` | `PostSummary[]` |
| GET | `/posts/:postId` | 게시글 상세 조회 | Public | - | `PostDetail` |
| POST | `/posts` | 게시글 작성 | User | `{ boardId, title, content }` | `PostDetail` |
| PATCH | `/posts/:postId` | 본인 게시글 수정 | User | `{ title?, content? }` | `PostDetail` |
| DELETE | `/posts/:postId` | 본인 게시글 삭제 | User | - | `{ message }` |

정렬값:

- `latest`
- `view_count`
- `like_count`

---

## 6. 댓글 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/posts/:postId/comments` | 댓글 목록 조회 | Public | `?page&limit` | `Comment[]` |
| POST | `/posts/:postId/comments` | 댓글 작성 | User | `{ content }` | `Comment` |
| PATCH | `/comments/:commentId` | 본인 댓글 수정 | User | `{ content }` | `Comment` |
| DELETE | `/comments/:commentId` | 본인 댓글 삭제 | User | - | `{ message }` |

---

## 7. 좋아요 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| POST | `/posts/:postId/likes` | 게시글 좋아요 등록 | User | - | `{ postId, liked, likeCount }` |
| DELETE | `/posts/:postId/likes` | 게시글 좋아요 취소 | User | - | `{ postId, liked, likeCount }` |

---

## 8. 관리자 페이지 운영 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/admin/dashboard` | 관리자 대시보드 조회 | Admin | - | `{ boardCount, postCount, commentCount, hiddenPostCount, hiddenCommentCount }` |
| GET | `/admin/posts` | 관리자 게시글 목록 조회 | Admin | `?page&limit&boardId&search&status&sort` | `AdminPostSummary[]` |
| GET | `/admin/posts/:postId` | 관리자 게시글 상세 조회 | Admin | - | `AdminPostDetail` |
| PATCH | `/admin/posts/:postId/status` | 게시글 상태 변경 | Admin | `{ status }` | `{ id, status, updatedAt }` |
| GET | `/admin/comments` | 관리자 댓글 목록 조회 | Admin | `?page&limit&postId&search&status&sort` | `AdminCommentSummary[]` |
| GET | `/admin/comments/:commentId` | 관리자 댓글 상세 조회 | Admin | - | `AdminCommentDetail` |
| PATCH | `/admin/comments/:commentId/status` | 댓글 상태 변경 | Admin | `{ status }` | `{ id, status, updatedAt }` |

---

## 9. 주요 요청/응답 예시

### POST `/api/v1/auth/login`

```json
{
  "email": "user@example.com",
  "password": "Password1!"
}
```

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "nickname": "writer1",
      "status": "ACTIVE"
    }
  }
}
```

### POST `/api/v1/admin/auth/login`

```json
{
  "email": "admin@example.com",
  "password": "AdminPassword1!"
}
```

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "expiresIn": 1800,
    "admin": {
      "id": 10,
      "email": "admin@example.com",
      "name": "운영관리자",
      "status": "ACTIVE"
    }
  }
}
```

### GET `/api/v1/posts?page=1&limit=20&boardId=1&search=공지&sort=latest`

```json
{
  "success": true,
  "data": [
    {
      "id": 101,
      "boardId": 1,
      "boardName": "자유게시판",
      "title": "공지 테스트",
      "author": {
        "id": 1,
        "nickname": "writer1"
      },
      "viewCount": 120,
      "likeCount": 15,
      "commentCount": 4,
      "status": "ACTIVE",
      "createdAt": "2026-04-12T10:00:00Z"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 20,
    "totalCount": 1,
    "totalPages": 1
  }
}
```

### POST `/api/v1/posts`

```json
{
  "boardId": 1,
  "title": "첫 게시글",
  "content": "게시글 본문입니다."
}
```

```json
{
  "success": true,
  "data": {
    "id": 102,
    "boardId": 1,
    "title": "첫 게시글",
    "content": "게시글 본문입니다.",
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0,
    "status": "ACTIVE",
    "createdAt": "2026-04-12T10:10:00Z"
  }
}
```

### POST `/api/v1/posts/102/comments`

```json
{
  "content": "첫 댓글입니다."
}
```

```json
{
  "success": true,
  "data": {
    "id": 501,
    "postId": 102,
    "author": {
      "id": 1,
      "nickname": "writer1"
    },
    "content": "첫 댓글입니다.",
    "status": "ACTIVE",
    "createdAt": "2026-04-12T10:12:00Z"
  }
}
```

### PATCH `/api/v1/admin/posts/102/status`

```json
{
  "status": "HIDDEN"
}
```

```json
{
  "success": true,
  "data": {
    "id": 102,
    "status": "HIDDEN",
    "updatedAt": "2026-04-12T10:20:00Z"
  }
}
```

### GET `/api/v1/admin/dashboard`

```json
{
  "success": true,
  "data": {
    "boardCount": 3,
    "postCount": 1240,
    "commentCount": 8420,
    "hiddenPostCount": 12,
    "hiddenCommentCount": 5
  }
}
```

---

## 10. 공통 에러 코드

| Code | HTTP Status | 설명 |
| --- | --- | --- |
| `VALIDATION_ERROR` | `400` | 요청 필드 검증 실패 |
| `UNAUTHORIZED` | `401` | 로그인 필요 또는 유효하지 않은 토큰 |
| `FORBIDDEN` | `403` | 권한 없음 |
| `USER_NOT_FOUND` | `404` | 사용자를 찾을 수 없음 |
| `ADMIN_NOT_FOUND` | `404` | 관리자를 찾을 수 없음 |
| `BOARD_NOT_FOUND` | `404` | 게시판을 찾을 수 없음 |
| `POST_NOT_FOUND` | `404` | 게시글을 찾을 수 없음 |
| `COMMENT_NOT_FOUND` | `404` | 댓글을 찾을 수 없음 |
| `DUPLICATE_EMAIL` | `409` | 이미 사용 중인 이메일 |
| `DUPLICATE_LIKE` | `409` | 이미 좋아요를 누른 게시글 |
| `INVALID_CREDENTIALS` | `401` | 이메일 또는 비밀번호 불일치 |
| `INVALID_STATUS` | `400` | 허용되지 않는 상태값 |

엔드포인트별 대표 오류:

- `POST /auth/signup`: `DUPLICATE_EMAIL`, `VALIDATION_ERROR`
- `POST /auth/login`: `INVALID_CREDENTIALS`, `UNAUTHORIZED`
- `POST /admin/auth/login`: `INVALID_CREDENTIALS`, `FORBIDDEN`
- `POST /posts`: `BOARD_NOT_FOUND`, `VALIDATION_ERROR`
- `PATCH /posts/:postId`: `POST_NOT_FOUND`, `FORBIDDEN`
- `POST /posts/:postId/comments`: `POST_NOT_FOUND`, `VALIDATION_ERROR`
- `POST /posts/:postId/likes`: `POST_NOT_FOUND`, `DUPLICATE_LIKE`
- `PATCH /admin/posts/:postId/status`: `POST_NOT_FOUND`, `INVALID_STATUS`
- `PATCH /admin/comments/:commentId/status`: `COMMENT_NOT_FOUND`, `INVALID_STATUS`
