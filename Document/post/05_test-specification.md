# 05 Test Specification

## 목적

이 문서는 `post` 서비스에서 공통으로 유지해야 하는 테스트 기준을 정의한다.
기준 문서는 `01_requirements.md`, `02_erd.md`, `03_api-specification.md`, `04_language.md`이며, `post` 도메인의 12개 백엔드 구현체가 동일한 테스트 목적을 공유하도록 만드는 것이 목표다.

공용 Python CLI는 테스트 대상 구현체가 아니라 테스트 실행을 오케스트레이션하는 도구로만 사용한다.

## 대상 구현체 범위

`post` 서비스의 테스트 기준 대상은 아래 12개 백엔드 구현체다.

- Java 8개
- TypeScript 4개

예시:

- `post-java-springboot-maven-jdbc-postgresql`
- `post-java-springboot-gradle-mysql`
- `post-typescript-nestjs-npm-knex-postgresql`
- `post-typescript-nestjs-npm-mysql`

## 테스트 계층

### 1. 품질/기본 검증

목적:

- 코드 품질 저하 차단
- 빠른 피드백 제공
- 주요 비즈니스 로직의 단위 수준 검증

공통 테스트 축:

- formatter check
- lint
- type check 또는 static analysis
- unit tests
- service or repository tests

### 2. 도메인 통합 테스트

`post` 서비스 핵심 기능을 API와 데이터 흐름 기준으로 검증한다.

공통 테스트 이름:

- `AuthApi`
- `UserApi`
- `BoardApi`
- `PostApi`
- `CommentApi`
- `LikeApi`

검증 범위:

- 회원가입, 로그인, 로그아웃, 토큰 갱신
- 사용자 본인 정보 조회/수정/탈퇴
- 게시판 목록 조회
- 게시판 생성, 수정, 삭제
- 게시글 목록, 상세, 작성, 수정, 삭제
- 게시글 검색, 정렬, 페이지네이션
- 댓글 목록, 작성, 수정, 삭제
- 좋아요 등록, 취소, 중복 방지

### 3. 관리자/보안 테스트

관리자 전용 로그인, 운영 기능, 권한 경계를 검증한다.

공통 테스트 이름:

- `AdminAuthApi`
- `AdminBoardApi`
- `AdminPostModerationApi`
- `AdminCommentModerationApi`
- `AdminDashboardApi`
- `AdminAuthorizationBoundaryE2E`
- `SecurityRegressionE2E`
- `RateLimitRegressionE2E`

검증 범위:

- 일반 로그인과 관리자 로그인 분리
- 관리자 토큰으로만 관리자 API 접근 가능
- 게시판 관리 CRUD
- 게시글 상태 변경 `ACTIVE`, `HIDDEN`, `DELETED`
- 댓글 상태 변경 `ACTIVE`, `HIDDEN`, `DELETED`
- 관리자 대시보드 집계 응답
- 비인가 요청 차단
- 인증 우회, 권한 상승, 중복 좋아요, 잘못된 상태값 처리 회귀
- 로그인/토큰/주요 쓰기 API 레이트 리밋 회귀

### 4. 성능 테스트

`post` 도메인에 맞는 최소 성능 smoke와 주요 조회 시나리오를 검증한다.

공통 테스트 이름:

- `post-list.perf`
- `post-detail.perf`
- `comment-list.perf`
- `smoke.perf`

검증 범위:

- 게시글 목록 조회 응답 시간
- 게시글 상세 조회 응답 시간
- 댓글 목록 조회 응답 시간
- 핵심 API smoke 성능

### 5. 테스트 스크립트

릴리스 전 점검과 DB 안정성 검증을 위한 보조 스크립트다.

공통 스크립트 이름:

- `analyze-stability`
- `live-smoke`
- `migration-roundtrip`
- `validate-migrations`

검증 범위:

- flaky 테스트 분석
- 배포 대상 환경 live smoke
- 마이그레이션 왕복 안정성
- 마이그레이션 검증 및 스키마 정합성

## 도메인별 검증 포인트

### 게시글/댓글

- 본인만 게시글 수정/삭제 가능
- 본인만 댓글 수정/삭제 가능
- 삭제된 게시글과 숨김 게시글 노출 정책 분리
- 게시글 상세 조회 시 조회수 증가
- 목록 응답에 `viewCount`, `likeCount`, `commentCount` 포함

### 좋아요

- 같은 사용자 중복 좋아요 방지
- 좋아요 등록과 취소 API 분리
- 목록/상세 응답의 좋아요 수 일관성 유지

### 관리자 기능

- 관리자만 게시판 생성/수정/삭제 가능
- 관리자만 게시글/댓글 상태 변경 가능
- 일반 사용자가 `/api/v1/admin/...` 접근 시 차단
- 관리자 대시보드가 게시판 수, 게시글 수, 댓글 수, 운영 대상 건수를 반환

## 언어별 적용 규칙

### Java

- `post-java-...` 8개 구현체는 동일한 테스트 목적을 가져야 한다.
- 테스트 프레임워크와 디렉토리 구조는 Maven/Gradle, JUnit, Spring 관례에 맞게 달라질 수 있다.
- 다만 테스트 이름, 검증 범위, 운영 목적은 본 문서와 동일해야 한다.

### TypeScript

- `post-typescript-...` 4개 구현체는 동일한 테스트 목적을 가져야 한다.
- NestJS와 데이터 접근 방식 차이로 파일 구조는 달라질 수 있다.
- 다만 테스트 이름, 검증 범위, 운영 목적은 본 문서와 동일해야 한다.

## 완료 기준

`post` 구현체가 테스트 기준을 충족했다고 보려면 최소한 아래를 만족해야 한다.

1. 품질 검사 경로가 있다.
2. `AuthApi`, `UserApi`, `BoardApi`, `PostApi`, `CommentApi`, `LikeApi`가 구현되어 있다.
3. 관리자 로그인과 관리자 운영 API 검증 경로가 있다.
4. 관리자 권한 경계 검증이 있다.
5. 최소 성능 smoke 자산이 있다.
6. 마이그레이션 검증 또는 roundtrip 경로가 있다.
7. live smoke 또는 이에 준하는 수동 운영 검증 경로가 있다.
