# 07 Folder Java

## 문서 목적

이 문서는 `post` 서비스의 Java 백엔드에서 사용하는 권장 폴더 구조를 정리하는 기준 문서다.
예시용 범용 MVC 트리가 아니라, 현재 `Project-Bible`의 대표 `post` Java 구현체와 요구사항/API 문서를 기준으로 정리한다.

## 권장 폴더 트리

```text
project-root/
├─ src/
│  ├─ main/
│  │  ├─ java/
│  │  │  └─ com/projectbible/post/.../
│  │  │     ├─ PostApplication.java
│  │  │     │
│  │  │     ├─ common/
│  │  │     │  ├─ api/
│  │  │     │  ├─ config/
│  │  │     │  ├─ exception/
│  │  │     │  ├─ presentation/
│  │  │     │  └─ security/
│  │  │     │
│  │  │     ├─ auth/
│  │  │     │  ├─ controller/
│  │  │     │  ├─ service/
│  │  │     │  ├─ dto/
│  │  │     │  ├─ entity/
│  │  │     │  └─ repository/
│  │  │     │
│  │  │     ├─ user/
│  │  │     │  ├─ controller/
│  │  │     │  ├─ service/
│  │  │     │  ├─ dto/
│  │  │     │  ├─ entity/
│  │  │     │  └─ repository/
│  │  │     │
│  │  │     ├─ admin/
│  │  │     │  ├─ controller/
│  │  │     │  ├─ service/
│  │  │     │  ├─ dto/
│  │  │     │  ├─ entity/
│  │  │     │  └─ repository/
│  │  │     │
│  │  │     ├─ board/
│  │  │     │  ├─ controller/
│  │  │     │  ├─ service/
│  │  │     │  ├─ dto/
│  │  │     │  ├─ entity/
│  │  │     │  └─ repository/
│  │  │     │
│  │  │     ├─ post/
│  │  │     │  ├─ controller/
│  │  │     │  ├─ service/
│  │  │     │  ├─ dto/
│  │  │     │  ├─ entity/
│  │  │     │  └─ repository/
│  │  │     │
│  │  │     ├─ comment/
│  │  │     │  ├─ controller/
│  │  │     │  ├─ service/
│  │  │     │  ├─ dto/
│  │  │     │  ├─ entity/
│  │  │     │  └─ repository/
│  │  │     │
│  │  │     └─ like/
│  │  │        ├─ controller/
│  │  │        ├─ service/
│  │  │        ├─ dto/
│  │  │        ├─ entity/
│  │  │        └─ repository/
│  │  │
│  │  └─ resources/
│  │     ├─ application.yml
│  │     └─ db/
│  │        └─ migration/
│  │
│  └─ test/
│     └─ java/
│        └─ com/projectbible/post/.../
│           ├─ common/
│           ├─ auth/
│           ├─ user/
│           ├─ admin/
│           ├─ board/
│           ├─ post/
│           ├─ comment/
│           └─ like/
│
├─ pom.xml
└─ README.md
```

## 구조 원칙

- 공통 기능은 `common` 아래에 모은다.
- 도메인 모듈은 `auth`, `user`, `admin`, `board`, `post`, `comment`, `like` 기준으로 나눈다.
- 각 도메인 모듈은 `controller`, `service`, `dto`, `entity`, `repository` 5계층을 기본으로 사용한다.
- `dto`는 요청과 응답에 사용하는 Java DTO를 함께 관리한다.
- `board`는 게시판 관리, `post`는 게시글 CRUD, `comment`는 댓글, `like`는 좋아요 기능을 담당한다.
- 관리자 대시보드와 운영 API는 `admin` 모듈 안에서 관리한다.
- `resources`는 실행 설정과 DB 마이그레이션처럼 애플리케이션 운영에 필요한 파일만 둔다.
