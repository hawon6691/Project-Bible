# 07 Folder TypeScript

## 문서 목적

이 문서는 `shop` 서비스의 TypeScript 백엔드에서 사용하는 권장 폴더 구조를 정리하는 기준 문서다.
예시용 Nest 템플릿 구조가 아니라, 현재 `Project-Bible`의 대표 `shop` TypeScript 구현체와 요구사항/API 문서를 기준으로 정리한다.

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
│  ├─ categories/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ products/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ cart/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ addresses/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ orders/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  ├─ payments/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ request/
│  │  ├─ response/
│  │  ├─ entity/
│  │  └─ repository/
│  │
│  └─ reviews/
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
- 도메인 모듈은 `auth`, `users`, `admin`, `categories`, `products`, `cart`, `addresses`, `orders`, `payments`, `reviews` 기준으로 나눈다.
- 각 도메인 모듈은 `controller`, `service`, `request`, `response`, `entity`, `repository` 6계층을 기본으로 사용한다.
- `request`는 입력 DTO, `response`는 출력 DTO를 분리해서 관리한다.
- `products` 모듈 안에서 상품 기본 정보와 옵션, 이미지 관리를 함께 처리한다.
- `orders` 모듈 안에서 주문 본문, 주문 상품 라인, 주문 배송지 스냅샷 구조를 함께 관리한다.
- 관리자 대시보드와 운영 API는 `admin` 모듈 안에서 관리한다.
- `main.ts`, `app.module.ts`는 애플리케이션 엔트리로 루트에 둔다.
