# Document Index

`Document` 폴더는 `Project-Bible`의 구현 기준 문서를 모아 두는 루트입니다. 문서는 `post`, `shop`, `cli` 세 축으로 정리합니다.

## 문서 구성

- [post](post)
- [shop](shop)
- [cli](cli/README.md)
- [.code-convention](.code-convention)

## Post 문서

- [01_requirements.md](post/01_requirements.md)
  - `post` 도메인 범위와 제외 범위
- [02_erd.md](post/02_erd.md)
  - `users`, `admins`, `boards`, `posts`, `comments`, `post_likes` 중심 ERD
- [03_api-specification.md](post/03_api-specification.md)
  - 사용자/관리자 인증, 게시판, 게시글, 댓글, 좋아요, 관리자 운영 API
- [04_language.md](post/04_language.md)
  - 현재 실제 `post` 백엔드 6개 구현체 기준
- [05_test-specification.md](post/05_test-specification.md)
  - `post` 도메인 테스트 세트
- [06_ci-specification.md](post/06_ci-specification.md)
  - `post` CI 기준과 검증 축
- [07_folder-java.md](post/07_folder-java.md)
  - Java 표준 폴더 구조
- [07_folder-typescript.md](post/07_folder-typescript.md)
  - TypeScript 표준 폴더 구조

## Shop 문서

- [01_requirements.md](shop/01_requirements.md)
  - `shop` 도메인 범위와 제외 범위
- [02_erd.md](shop/02_erd.md)
  - 카테고리, 상품, 주문, 결제, 리뷰 중심 ERD
- [03_api-specification.md](shop/03_api-specification.md)
  - 사용자/관리자 인증, 카테고리, 상품, 장바구니, 배송지, 주문, 결제, 리뷰, 관리자 운영 API
- [04_language.md](shop/04_language.md)
  - 현재 실제 `shop` 백엔드 6개 구현체 기준
- [05_test-specification.md](shop/05_test-specification.md)
  - `shop` 도메인 테스트 세트
- [06_ci-specification.md](shop/06_ci-specification.md)
  - `shop` CI 기준과 검증 축
- [07_folder-java.md](shop/07_folder-java.md)
  - Java 표준 폴더 구조
- [07_folder-typescript.md](shop/07_folder-typescript.md)
  - TypeScript 표준 폴더 구조

## CLI 문서

- [README.md](cli/README.md)
  - CLI 문서 인덱스
- [01_command-reference.md](cli/01_command-reference.md)
  - 명령어 표와 구현 상태
- [02_setup-and-target-registration.md](cli/02_setup-and-target-registration.md)
  - 설치, 실행, 타깃 등록 규칙
- [03_runbook.md](cli/03_runbook.md)
  - 실제 사용 시나리오

## 코드/협업 규칙

- [.code-convention/convention.md](.code-convention/convention.md)
  - 이슈, 브랜치, 커밋 메시지 규칙
- `.github/ISSUE_TEMPLATE`
  - GitHub 이슈 템플릿

## 문서 읽는 순서

### 도메인 구현을 이해할 때

1. `01_requirements.md`
2. `02_erd.md`
3. `03_api-specification.md`
4. `04_language.md`
5. `05_test-specification.md`
6. `06_ci-specification.md`
7. `07_folder-*.md`

### 저장소 실행/운영을 이해할 때

1. [../README.md](../README.md)
2. [cli/README.md](cli/README.md)
3. [cli/01_command-reference.md](cli/01_command-reference.md)
4. [cli/02_setup-and-target-registration.md](cli/02_setup-and-target-registration.md)
5. [cli/03_runbook.md](cli/03_runbook.md)

## 현재 기준 요약

- Backend
  - `post` 6개
  - `shop` 6개
  - 총 12개
- Frontend
  - `web-post`
  - `web-shop`
- Database
  - PostgreSQL, MySQL 공용 인프라
- Tools
  - 공용 CLI와 CI 보조 스크립트
