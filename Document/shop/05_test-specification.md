# 05 Test Specification

## 목적

이 문서는 `shop` 서비스에서 공통으로 유지해야 하는 테스트 기준을 정의한다.
기준 문서는 `01_requirements.md`, `02_erd.md`, `03_api-specification.md`, `04_language.md`이며, `shop` 도메인의 12개 백엔드 구현체가 동일한 테스트 목적을 공유하도록 만드는 것이 목표다.

공용 Python CLI는 테스트 대상 구현체가 아니라 테스트 실행을 오케스트레이션하는 도구로만 사용한다.

## 대상 구현체 범위

`shop` 서비스의 테스트 기준 대상은 아래 12개 백엔드 구현체다.

- Java 8개
- TypeScript 4개

예시:

- `shop-java-springboot-maven-jdbc-postgresql`
- `shop-java-springboot-gradle-jpa-mysql`
- `shop-typescript-nestjs-npm-knex-postgresql`
- `shop-typescript-nestjs-npm-typeorm-mysql`

## 테스트 계층

### 1. 품질/기본 검증

목적:

- 코드 품질 저하 차단
- 빠른 피드백 제공
- 핵심 서비스 로직의 단위 수준 검증

공통 테스트 축:

- formatter check
- lint
- type check 또는 static analysis
- unit tests
- service or repository tests

### 2. 도메인 통합 테스트

`shop` 서비스 핵심 기능을 API와 데이터 흐름 기준으로 검증한다.

공통 테스트 이름:

- `AuthApi`
- `UserApi`
- `CategoryApi`
- `ProductApi`
- `CartApi`
- `AddressApi`
- `OrderApi`
- `PaymentApi`
- `ReviewApi`

검증 범위:

- 회원가입, 로그인, 로그아웃, 토큰 갱신
- 사용자 본인 정보 조회/수정/탈퇴
- 카테고리 조회
- 상품 목록, 상세, 검색, 정렬, 페이지네이션
- 상품 옵션/이미지 CRUD
- 장바구니 조회, 추가, 수량 변경, 개별 삭제, 전체 비우기
- 배송지 조회, 추가, 수정, 삭제
- 주문 생성, 목록, 상세, 취소
- 모의 결제 요청, 상태 조회, 환불 처리
- 리뷰 목록, 작성, 수정, 삭제

### 3. 관리자/보안 테스트

관리자 전용 로그인, 운영 기능, 권한 경계를 검증한다.

공통 테스트 이름:

- `AdminAuthApi`
- `AdminCategoryApi`
- `AdminProductApi`
- `AdminOrderApi`
- `AdminReviewApi`
- `AdminDashboardApi`
- `AdminAuthorizationBoundaryE2E`
- `SecurityRegressionE2E`
- `RateLimitRegressionE2E`

검증 범위:

- 일반 로그인과 관리자 로그인 분리
- 관리자 토큰으로만 관리자 API 접근 가능
- 카테고리 관리 CRUD
- 상품/옵션/이미지 관리
- 주문 목록 조회와 상태 변경
- 리뷰 목록 조회와 관리자 삭제
- 관리자 대시보드 집계 응답
- 비인가 요청 차단
- 인증 우회, 상태 위조, 잘못된 주문 상태 전이, 재고 경계 회귀
- 로그인/결제/주문/주요 쓰기 API 레이트 리밋 회귀

### 4. 성능 테스트

`shop` 도메인에 맞는 최소 성능 smoke와 주요 조회/주문 시나리오를 검증한다.

공통 테스트 이름:

- `product-list.perf`
- `product-detail.perf`
- `order-create.perf`
- `smoke.perf`

검증 범위:

- 상품 목록 조회 응답 시간
- 상품 상세 조회 응답 시간
- 주문 생성 응답 시간
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

### 상품/카테고리

- 상품 목록 검색/정렬/페이지네이션
- 비노출 상품 처리
- 카테고리 삭제/상태 변경 후 상품 노출 정책
- 옵션과 이미지 CRUD 일관성

### 장바구니/주문/결제

- 장바구니 수량 변경과 전체 비우기
- 주문 생성 시 `cartItemIds + addressId` 사용
- 주문 상세 응답에 `orderAddress`, `orderItems`, `payment` 포함
- 잘못된 상태 전이 차단
- 모의 결제 성공/실패/환불 흐름

### 리뷰

- `orderItemId` 기준 구매 검증
- 하나의 주문 상품에 하나의 리뷰만 허용
- 본인 리뷰만 수정/삭제 가능
- 관리자 리뷰 삭제 가능

### 관리자 기능

- 관리자만 카테고리/상품/주문/리뷰 관리 가능
- 일반 사용자가 `/api/v1/admin/...` 접근 시 차단
- 관리자 대시보드가 주문, 상품, 회원, 리뷰 요약을 반환

## 언어별 적용 규칙

### Java

- `shop-java-...` 8개 구현체는 동일한 테스트 목적을 가져야 한다.
- 테스트 프레임워크와 디렉토리 구조는 Maven/Gradle, JUnit, Spring 관례에 맞게 달라질 수 있다.
- 다만 테스트 이름, 검증 범위, 운영 목적은 본 문서와 동일해야 한다.

### TypeScript

- `shop-typescript-...` 4개 구현체는 동일한 테스트 목적을 가져야 한다.
- NestJS와 데이터 접근 방식 차이로 파일 구조는 달라질 수 있다.
- 다만 테스트 이름, 검증 범위, 운영 목적은 본 문서와 동일해야 한다.

## 완료 기준

`shop` 구현체가 테스트 기준을 충족했다고 보려면 최소한 아래를 만족해야 한다.

1. 품질 검사 경로가 있다.
2. `AuthApi`, `UserApi`, `CategoryApi`, `ProductApi`, `CartApi`, `AddressApi`, `OrderApi`, `PaymentApi`, `ReviewApi`가 구현되어 있다.
3. 관리자 로그인과 관리자 운영 API 검증 경로가 있다.
4. 관리자 권한 경계 검증이 있다.
5. 최소 성능 smoke 자산이 있다.
6. 마이그레이션 검증 또는 roundtrip 경로가 있다.
7. live smoke 또는 이에 준하는 수동 운영 검증 경로가 있다.
