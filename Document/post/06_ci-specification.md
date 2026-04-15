# 06 CI Specification

## 1. 목적

본 문서는 `post` 서비스의 12개 백엔드 구현체에 공통으로 적용할 CI 기준을 정의한다.
현재 저장소에 존재하는 Java/Nest 워크플로를 기준 구현체로 삼고, 나머지 구현체는 동일한 검증 목적을 가지는 개별 workflow 파일로 확장하는 것을 원칙으로 한다.

공용 Python CLI는 CI 대상 애플리케이션이 아니라 실행과 검증을 오케스트레이션하는 도구로만 취급한다.

## 2. 기준 구현체

현재 존재하는 아래 workflow 파일을 대표 구현체로 사용한다.

- `.github/workflows/java-spring-maven-jpa-postgresql-ci.yml`
- `.github/workflows/typescript-nest-npm-typeorm-postgresql-ci.yml`

이 두 워크플로는 `post` 전용 파일은 아니지만, CI 계층과 검증 축을 정의하는 기준 구현체로 사용한다.

## 3. 워크플로 파일 규칙

`post`용 모든 구현체는 프로젝트별 개별 workflow 파일을 가진다.

파일명 규칙:

`<domain>-<language>-<framework>-<build>-<dataaccess>-<db>-ci.yml`

예시:

- `post-java-springboot-maven-postgresql-ci.yml`
- `post-java-springboot-gradle-jdbc-mysql-ci.yml`
- `post-typescript-nestjs-npm-postgresql-ci.yml`
- `post-typescript-nestjs-npm-knex-mysql-ci.yml`

경로 감시 규칙:

- 각 workflow는 자기 자신의 workflow 파일과 해당 프로젝트 폴더만 감시해야 한다.
- 프로젝트 폴더 경로는 `Backend/post/<project-folder>` 규칙을 따른다.

## 4. 자동 CI 계층

`pull_request`, `push`에서 자동 실행되는 기본 계층은 다음 목적을 커버해야 한다.

대표 잡:

- `validate-dispatch-inputs`
- `quality`
- `platform-e2e` 또는 `e2e-critical`
- `swagger-export` 또는 계약 산출 잡
- `perf-smoke`

역할:

- 수동 실행 입력값 검증
- 포맷, 린트, 타입/정적 분석, 빠른 단위 테스트
- 핵심 API 회귀 검증
- OpenAPI 또는 계약 산출물 검증
- 게시글 목록/상세 기준 성능 smoke 검증

## 5. 수동 CI 계층

`workflow_dispatch`에서 수동 실행되는 확장 계층은 다음 잡을 포함해야 한다.

- `release-gate`
- `contract-e2e`
- `migration-validation`
- `migration-roundtrip`
- `stability-check`
- `security-regression`
- `admin-boundary`
- `rate-limit-regression`
- `dependency-failure`
- `perf-extended`
- `live-smoke`

역할:

- 릴리스 직전 최종 게이트
- 무거운 계약 회귀 검증
- 마이그레이션/DB 안정성 검증
- 보안 회귀와 관리자 경계 검증
- 확장 성능 시나리오
- 실제 대상 환경 live smoke

## 6. 공통 검증 축

### 6.1 품질

- formatter check
- lint
- type check 또는 static analysis
- unit tests

### 6.2 도메인 회귀

`post` 도메인 CI는 최소한 아래 회귀를 커버해야 한다.

- 인증/회원 API
- 게시판 관리 API
- 게시글 CRUD, 검색, 정렬, 페이지네이션
- 댓글 CRUD
- 좋아요 등록/취소/중복 방지
- 관리자 대시보드 요약

### 6.3 관리자/보안

- 관리자 로그인과 일반 로그인 분리 검증
- `/api/v1/admin/...` 권한 경계
- 게시글/댓글 상태 변경 회귀
- 인증 우회 및 잘못된 상태 변경 회귀
- 레이트 리밋 회귀

### 6.4 DB/마이그레이션

- `validate-migrations`
- `migration-roundtrip`
- 필요 시 DB 통합 테스트

### 6.5 계약/문서 산출

- OpenAPI 산출 또는 계약 스냅샷
- 테스트 결과 아티팩트 업로드

### 6.6 성능

- `perf-smoke` 자동 경로
- `perf-extended` 수동 경로
- 게시글 목록/상세, 댓글 목록 중심 시나리오

## 7. 아티팩트 규칙

각 workflow는 최소한 아래 산출물을 보존해야 한다.

- 테스트 리포트
- 커버리지 또는 품질 리포트
- OpenAPI/계약 산출물
- 성능 요약 리포트
- 실행 로그

아티팩트 명명은 프로젝트 폴더명과 잡 이름을 반영해 재현 가능하게 유지한다.

## 8. 언어별 적용 규칙

### Java

- Maven과 Gradle 구현체는 각각 별도 workflow 파일을 가진다.
- `jdbc`, `jpa`, `postgresql`, `mysql` 조합별로 개별 workflow를 둔다.
- 대표 구현체는 현재 Java Maven JPA PostgreSQL workflow다.

### TypeScript

- `knex`, `typeorm`, `postgresql`, `mysql` 조합별로 개별 workflow 파일을 둔다.
- 대표 구현체는 현재 TypeScript Nest TypeORM PostgreSQL workflow다.

### 공용 CLI

- `Tools/cli`는 CI 대상 앱이 아니다.
- 다만 로컬/수동 실행 자동화 도구로 문서상 참고 위치를 유지한다.

## 9. 완성 기준

`post` 구현체의 CI가 준비됐다고 보려면 아래를 만족해야 한다.

1. 각 구현체별 workflow 파일이 있다.
2. `pull_request`, `push`, `workflow_dispatch`를 지원한다.
3. 품질 계층이 있다.
4. 핵심 도메인 회귀 검증이 있다.
5. 관리자 권한 경계 검증이 있다.
6. DB/마이그레이션 검증 경로가 있다.
7. 계약 산출 또는 OpenAPI 산출 경로가 있다.
8. 성능 smoke 경로가 있다.
9. 릴리스 전 수동 게이트가 있다.
