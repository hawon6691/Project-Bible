# Project-Bible

`Project-Bible`는 동일한 `post`/`shop` 도메인을 여러 기술 스택으로 구현하고, 공용 `Database`, `Tools/cli`, 프론트엔드 앱, 문서 세트를 함께 관리하는 저장소입니다.

[![frontend-cli CI](https://github.com/hawon6691/Project-Bible/actions/workflows/frontend-cli-ci.yml/badge.svg)](https://github.com/hawon6691/Project-Bible/actions/workflows/frontend-cli-ci.yml)
[![post-java-springboot-maven-postgresql CI](https://github.com/hawon6691/Project-Bible/actions/workflows/post-java-springboot-maven-postgresql-ci.yml/badge.svg)](https://github.com/hawon6691/Project-Bible/actions/workflows/post-java-springboot-maven-postgresql-ci.yml)
[![post-typescript-nestjs-npm-postgresql CI](https://github.com/hawon6691/Project-Bible/actions/workflows/post-typescript-nestjs-npm-postgresql-ci.yml/badge.svg)](https://github.com/hawon6691/Project-Bible/actions/workflows/post-typescript-nestjs-npm-postgresql-ci.yml)
[![shop-java-springboot-maven-postgresql CI](https://github.com/hawon6691/Project-Bible/actions/workflows/shop-java-springboot-maven-postgresql-ci.yml/badge.svg)](https://github.com/hawon6691/Project-Bible/actions/workflows/shop-java-springboot-maven-postgresql-ci.yml)
[![shop-typescript-nestjs-npm-postgresql CI](https://github.com/hawon6691/Project-Bible/actions/workflows/shop-typescript-nestjs-npm-postgresql-ci.yml/badge.svg)](https://github.com/hawon6691/Project-Bible/actions/workflows/shop-typescript-nestjs-npm-postgresql-ci.yml)

## 개요

- Backend
  - `post` 6개
  - `shop` 6개
  - 총 12개 구현체
- Frontend
  - `Frontend/web-post`
  - `Frontend/web-shop`
- Database
  - `Database/postgresql`
  - `Database/mysql`
  - 공용 Docker 인프라: `Database/docker`
- Tools
  - 공용 CLI: `Tools/cli`
  - CI 보조 스크립트: `Tools/ci`
- Document
  - 요구사항, ERD, API, 테스트, CI, 폴더 구조, CLI 문서

## 저장소 구조

```text
Project-Bible/
├─ Backend/
│  ├─ post/
│  └─ shop/
├─ Frontend/
│  ├─ web-post/
│  └─ web-shop/
├─ Database/
│  ├─ docker/
│  ├─ postgresql/
│  └─ mysql/
├─ Tools/
│  ├─ cli/
│  └─ ci/
└─ Document/
   ├─ post/
   ├─ shop/
   └─ cli/
```

## 백엔드 매트릭스

### Post

- Java
  - `post-java-springboot-maven-postgresql`
  - `post-java-springboot-maven-mysql`
  - `post-java-springboot-gradle-postgresql`
  - `post-java-springboot-gradle-mysql`
- TypeScript
  - `post-typescript-nestjs-npm-postgresql`
  - `post-typescript-nestjs-npm-mysql`

### Shop

- Java
  - `shop-java-springboot-maven-postgresql`
  - `shop-java-springboot-maven-mysql`
  - `shop-java-springboot-gradle-postgresql`
  - `shop-java-springboot-gradle-mysql`
- TypeScript
  - `shop-typescript-nestjs-npm-postgresql`
  - `shop-typescript-nestjs-npm-mysql`

## 프론트엔드 구조

프론트엔드는 `React + Vite + TypeScript` 기반이며 FSD 5-layer를 사용합니다.

- `app`
- `pages`
- `widgets`
- `features`
- `entities`
- `shared`

화면 구성은 흰 배경과 검은 글씨 중심의 단순한 스타일을 유지합니다.

## 공통 계약

- API 경로
  - 일반 API: `/api/v1/...`
  - 관리자 API: `/api/v1/admin/...`
- Swagger
  - `/docs`
- 응답 envelope
  - 성공: `{ success: true, data, meta? }`
  - 실패: `{ success: false, error: { code, message, details? } }`
- 인증
  - 사용자 JWT와 관리자 JWT 분리

## 빠른 시작

### 1. 문서 확인

- 전체 문서 인덱스: [Document/README.md](Document/README.md)
- `post` 문서: [Document/post](Document/post)
- `shop` 문서: [Document/shop](Document/shop)
- CLI 문서: [Document/cli/README.md](Document/cli/README.md)

### 2. CLI 설치

```powershell
cd Tools/cli
python -m pip install -e .
```

### 3. 타깃 확인

```powershell
pb list
pb /?
pb search post
```

### 4. Database 기동

```powershell
pb db up
pb db reset postgresql post
pb db reset postgresql shop
```

### 5. 앱 실행

```powershell
pb up post-java-springboot-maven-postgresql
pb up shop-typescript-nestjs-npm-postgresql
pb up web-post
pb up web-shop
```

필요하면 포트를 직접 지정할 수 있습니다.

```powershell
pb up web-post --port 3005
pb up post-java-springboot-maven-postgresql --port 8011
```

## 대표 실행 예시

### Backend

```powershell
pb up post-java-springboot-maven-postgresql --port 8011
pb up shop-typescript-nestjs-npm-postgresql --port 8021
pb down post-java-springboot-maven-postgresql
pb down shop-typescript-nestjs-npm-postgresql
```

### Frontend

```powershell
pb up web-post --port 3000
pb up web-shop --port 3001
pb down web-post
pb down web-shop
```

### Database

```powershell
pb db up
pb db reset postgresql post
pb db reset mysql shop
pb db down
```

### 검증

```powershell
pb test post-java-springboot-maven-postgresql
pb test shop-typescript-nestjs-npm-postgresql
```

## 검증 기준

현재 저장소는 아래 기준으로 정리되어 있습니다.

- Backend 12개 workflow 개별 GitHub Actions 구성
- Frontend/CLI 전용 workflow 구성
- PostgreSQL/MySQL schema 및 seed 제공
- CLI 기반 실행/중지/DB 제어 지원
- 프론트엔드 build/test/smoke, 백엔드 build/test/e2e, HTTP matrix 검증 기준 정리

## 문서 맵

- `Document/post`
  - 요구사항, ERD, API, 테스트, CI, 언어/폴더 구조
- `Document/shop`
  - 요구사항, ERD, API, 테스트, CI, 언어/폴더 구조
- `Document/cli`
  - 명령어, 설정, runbook

상세 문서 인덱스는 [Document/README.md](Document/README.md)에서 확인합니다.
