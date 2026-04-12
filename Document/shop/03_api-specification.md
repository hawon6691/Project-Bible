# 쇼핑몰 서비스 API 명세서

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
- 목록 조회 기본 쿼리는 `page`, `limit`이며, 검색/정렬이 필요한 경우 `search`, `sort`, `status`, `categoryId`를 사용한다.
- 상품 상태값은 `ACTIVE`, `HIDDEN`, `DELETED`를 기준으로 사용한다.
- 주문 상태값은 `PENDING`, `PAID`, `PREPARING`, `SHIPPING`, `DELIVERED`, `CANCELLED`를 기준으로 사용한다.
- 결제 상태값은 `READY`, `PAID`, `REFUNDED`, `FAILED`를 기준으로 사용한다.

---

## 2. 사용자 인증 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| POST | `/auth/signup` | 회원가입 | Public | `{ email, password, name, phone }` | `{ id, email, name, phone }` |
| POST | `/auth/login` | 사용자 로그인 | Public | `{ email, password }` | `{ accessToken, refreshToken, expiresIn, user }` |
| POST | `/auth/logout` | 사용자 로그아웃 | User | - | `{ message }` |
| POST | `/auth/refresh` | 사용자 토큰 갱신 | Public | `{ refreshToken }` | `{ accessToken, refreshToken, expiresIn }` |
| GET | `/users/me` | 내 정보 조회 | User | - | `{ id, email, name, phone, status, createdAt }` |
| PATCH | `/users/me` | 내 정보 수정 | User | `{ name?, phone?, password? }` | `{ id, email, name, phone, status, updatedAt }` |
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

## 4. 카테고리 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/categories` | 카테고리 목록 조회 | Public | `?status?` | `Category[]` |
| GET | `/categories/:categoryId` | 카테고리 단건 조회 | Public | - | `Category` |
| POST | `/admin/categories` | 카테고리 생성 | Admin | `{ name, displayOrder }` | `Category` |
| PATCH | `/admin/categories/:categoryId` | 카테고리 수정 | Admin | `{ name?, displayOrder?, status? }` | `Category` |
| DELETE | `/admin/categories/:categoryId` | 카테고리 삭제 | Admin | - | `{ message }` |

---

## 5. 상품 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/products` | 상품 목록 조회 | Public | `?page&limit&categoryId&search&sort&status?` | `ProductSummary[]` |
| GET | `/products/:productId` | 상품 상세 조회 | Public | - | `ProductDetail` |
| POST | `/admin/products` | 상품 등록 | Admin | `{ categoryId, name, description?, price, stock, status? }` | `ProductDetail` |
| PATCH | `/admin/products/:productId` | 상품 수정 | Admin | `{ categoryId?, name?, description?, price?, stock?, status? }` | `ProductDetail` |
| DELETE | `/admin/products/:productId` | 상품 삭제 또는 비노출 처리 | Admin | - | `{ message }` |
| POST | `/admin/products/:productId/options` | 상품 옵션 생성 | Admin | `{ name, value, additionalPrice?, stock }` | `ProductOption` |
| PATCH | `/admin/product-options/:optionId` | 상품 옵션 수정 | Admin | `{ name?, value?, additionalPrice?, stock? }` | `ProductOption` |
| DELETE | `/admin/product-options/:optionId` | 상품 옵션 삭제 | Admin | - | `{ message }` |
| POST | `/admin/products/:productId/images` | 상품 이미지 등록 | Admin | `{ imageUrl, isPrimary?, displayOrder? }` | `ProductImage` |
| PATCH | `/admin/product-images/:imageId` | 상품 이미지 수정 | Admin | `{ imageUrl?, isPrimary?, displayOrder? }` | `ProductImage` |
| DELETE | `/admin/product-images/:imageId` | 상품 이미지 삭제 | Admin | - | `{ message }` |

정렬값:

- `latest`
- `price_asc`
- `price_desc`
- `popular`

---

## 6. 장바구니 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/cart-items` | 장바구니 목록 조회 | User | - | `CartItem[]` |
| POST | `/cart-items` | 장바구니 추가 | User | `{ productId, productOptionId?, quantity }` | `CartItem` |
| PATCH | `/cart-items/:cartItemId` | 장바구니 수량 변경 | User | `{ quantity }` | `CartItem` |
| DELETE | `/cart-items/:cartItemId` | 장바구니 항목 삭제 | User | - | `{ message }` |
| DELETE | `/cart-items` | 장바구니 전체 비우기 | User | - | `{ message }` |

---

## 7. 배송지 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/addresses` | 배송지 목록 조회 | User | - | `Address[]` |
| POST | `/addresses` | 배송지 등록 | User | `{ recipientName, phone, zipCode, address1, address2?, isDefault? }` | `Address` |
| PATCH | `/addresses/:addressId` | 배송지 수정 | User | `{ recipientName?, phone?, zipCode?, address1?, address2?, isDefault? }` | `Address` |
| DELETE | `/addresses/:addressId` | 배송지 삭제 | User | - | `{ message }` |

---

## 8. 주문 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| POST | `/orders` | 주문 생성 | User | `{ cartItemIds, addressId }` | `OrderDetail` |
| GET | `/orders` | 내 주문 목록 조회 | User | `?page&limit&status?` | `OrderSummary[]` |
| GET | `/orders/:orderId` | 내 주문 상세 조회 | User | - | `OrderDetail` |
| POST | `/orders/:orderId/cancel` | 주문 취소 | User | - | `{ id, orderStatus, cancelledAt }` |
| GET | `/admin/orders` | 관리자 주문 목록 조회 | Admin | `?page&limit&search&status&sort` | `AdminOrderSummary[]` |
| GET | `/admin/orders/:orderId` | 관리자 주문 상세 조회 | Admin | - | `OrderDetail` |
| PATCH | `/admin/orders/:orderId/status` | 주문 상태 변경 | Admin | `{ orderStatus }` | `{ id, orderStatus, updatedAt }` |

주문 생성 기본 흐름:

- v1에서는 `cartItemIds[] + addressId` 조합을 기본으로 사용한다.

---

## 9. 결제 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| POST | `/payments` | 모의 결제 요청 | User | `{ orderId, paymentMethod }` | `Payment` |
| GET | `/payments/:paymentId` | 결제 상태 조회 | User | - | `Payment` |
| POST | `/payments/:paymentId/refund` | 환불 처리 | User | - | `{ id, paymentStatus, refundedAt }` |

비고:

- `paymentMethod`는 v1에서 `MOCK_CARD`, `MOCK_BANK` 정도의 모의 결제 수단만 문서화한다.

---

## 10. 리뷰 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/products/:productId/reviews` | 상품 리뷰 목록 조회 | Public | `?page&limit&sort` | `Review[]` |
| POST | `/order-items/:orderItemId/reviews` | 리뷰 작성 | User | `{ rating, content }` | `Review` |
| PATCH | `/reviews/:reviewId` | 본인 리뷰 수정 | User | `{ rating?, content? }` | `Review` |
| DELETE | `/reviews/:reviewId` | 본인 리뷰 삭제 | User | - | `{ message }` |
| GET | `/admin/reviews` | 관리자 리뷰 목록 조회 | Admin | `?page&limit&search&status&sort` | `AdminReviewSummary[]` |
| GET | `/admin/reviews/:reviewId` | 관리자 리뷰 상세 조회 | Admin | - | `AdminReviewDetail` |
| DELETE | `/admin/reviews/:reviewId` | 관리자 리뷰 삭제 | Admin | - | `{ message }` |

리뷰 정렬값:

- `latest`
- `rating_desc`
- `rating_asc`

---

## 11. 관리자 페이지 운영 API

| Method | Endpoint | 설명 | 권한 | Request | Response |
| --- | --- | --- | --- | --- | --- |
| GET | `/admin/dashboard` | 관리자 대시보드 조회 | Admin | - | `{ orderSummary, productSummary, userSummary, reviewSummary }` |

---

## 12. 주요 요청/응답 예시

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
      "name": "홍길동",
      "phone": "010-1234-5678",
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

### GET `/api/v1/products?page=1&limit=20&categoryId=2&search=키보드&sort=price_asc`

```json
{
  "success": true,
  "data": [
    {
      "id": 201,
      "categoryId": 2,
      "name": "기계식 키보드",
      "price": 89000,
      "stock": 15,
      "status": "ACTIVE",
      "thumbnailUrl": "https://cdn.example.com/products/201/main.jpg"
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

### POST `/api/v1/cart-items`

```json
{
  "productId": 201,
  "productOptionId": 301,
  "quantity": 2
}
```

```json
{
  "success": true,
  "data": {
    "id": 401,
    "productId": 201,
    "productOptionId": 301,
    "quantity": 2,
    "createdAt": "2026-04-12T11:00:00Z"
  }
}
```

### POST `/api/v1/orders`

```json
{
  "cartItemIds": [401, 402],
  "addressId": 501
}
```

```json
{
  "success": true,
  "data": {
    "order": {
      "id": 601,
      "orderNumber": "ORD-20260412-0001",
      "orderStatus": "PENDING",
      "paymentStatus": "READY",
      "totalAmount": 178000,
      "orderedAt": "2026-04-12T11:10:00Z"
    },
    "orderAddress": {
      "recipientName": "홍길동",
      "phone": "010-1234-5678",
      "zipCode": "06236",
      "address1": "서울시 강남구 테헤란로 1",
      "address2": "101호"
    },
    "orderItems": [
      {
        "id": 701,
        "productId": 201,
        "productNameSnapshot": "기계식 키보드",
        "optionNameSnapshot": "블랙 / 적축",
        "unitPrice": 89000,
        "quantity": 2,
        "lineAmount": 178000
      }
    ],
    "payment": null
  }
}
```

### POST `/api/v1/payments`

```json
{
  "orderId": 601,
  "paymentMethod": "MOCK_CARD"
}
```

```json
{
  "success": true,
  "data": {
    "id": 801,
    "orderId": 601,
    "paymentMethod": "MOCK_CARD",
    "paymentStatus": "PAID",
    "paidAmount": 178000,
    "paidAt": "2026-04-12T11:15:00Z"
  }
}
```

### POST `/api/v1/order-items/701/reviews`

```json
{
  "rating": 5,
  "content": "배송이 빠르고 만족합니다."
}
```

```json
{
  "success": true,
  "data": {
    "id": 901,
    "orderItemId": 701,
    "productId": 201,
    "rating": 5,
    "content": "배송이 빠르고 만족합니다.",
    "status": "ACTIVE",
    "createdAt": "2026-04-12T11:30:00Z"
  }
}
```

### PATCH `/api/v1/admin/orders/601/status`

```json
{
  "orderStatus": "PREPARING"
}
```

```json
{
  "success": true,
  "data": {
    "id": 601,
    "orderStatus": "PREPARING",
    "updatedAt": "2026-04-12T11:40:00Z"
  }
}
```

### GET `/api/v1/admin/dashboard`

```json
{
  "success": true,
  "data": {
    "orderSummary": {
      "pending": 12,
      "paid": 48,
      "shipping": 9,
      "cancelled": 3
    },
    "productSummary": {
      "active": 120,
      "hidden": 4,
      "deleted": 2
    },
    "userSummary": {
      "active": 320,
      "newUsersToday": 7
    },
    "reviewSummary": {
      "active": 980,
      "hidden": 6,
      "deleted": 3
    }
  }
}
```

---

## 13. 공통 에러 코드

| Code | HTTP Status | 설명 |
| --- | --- | --- |
| `VALIDATION_ERROR` | `400` | 요청 필드 검증 실패 |
| `UNAUTHORIZED` | `401` | 로그인 필요 또는 유효하지 않은 토큰 |
| `FORBIDDEN` | `403` | 권한 없음 |
| `USER_NOT_FOUND` | `404` | 사용자를 찾을 수 없음 |
| `ADMIN_NOT_FOUND` | `404` | 관리자를 찾을 수 없음 |
| `CATEGORY_NOT_FOUND` | `404` | 카테고리를 찾을 수 없음 |
| `PRODUCT_NOT_FOUND` | `404` | 상품을 찾을 수 없음 |
| `PRODUCT_OPTION_NOT_FOUND` | `404` | 상품 옵션을 찾을 수 없음 |
| `CART_ITEM_NOT_FOUND` | `404` | 장바구니 항목을 찾을 수 없음 |
| `ADDRESS_NOT_FOUND` | `404` | 배송지를 찾을 수 없음 |
| `ORDER_NOT_FOUND` | `404` | 주문을 찾을 수 없음 |
| `PAYMENT_NOT_FOUND` | `404` | 결제 정보를 찾을 수 없음 |
| `REVIEW_NOT_FOUND` | `404` | 리뷰를 찾을 수 없음 |
| `DUPLICATE_EMAIL` | `409` | 이미 사용 중인 이메일 |
| `INVALID_CREDENTIALS` | `401` | 이메일 또는 비밀번호 불일치 |
| `INVALID_STATUS` | `400` | 허용되지 않는 상태값 |
| `OUT_OF_STOCK` | `409` | 재고 부족 |
| `REVIEW_ALREADY_EXISTS` | `409` | 해당 주문 상품에 이미 리뷰가 존재함 |
| `PAYMENT_NOT_ALLOWED` | `409` | 현재 주문 상태에서 결제를 진행할 수 없음 |

엔드포인트별 대표 오류:

- `POST /auth/signup`: `DUPLICATE_EMAIL`, `VALIDATION_ERROR`
- `POST /auth/login`: `INVALID_CREDENTIALS`, `UNAUTHORIZED`
- `POST /admin/auth/login`: `INVALID_CREDENTIALS`, `FORBIDDEN`
- `POST /cart-items`: `PRODUCT_NOT_FOUND`, `PRODUCT_OPTION_NOT_FOUND`, `OUT_OF_STOCK`
- `POST /orders`: `CART_ITEM_NOT_FOUND`, `ADDRESS_NOT_FOUND`, `OUT_OF_STOCK`
- `POST /payments`: `ORDER_NOT_FOUND`, `PAYMENT_NOT_ALLOWED`
- `POST /order-items/:orderItemId/reviews`: `ORDER_NOT_FOUND`, `REVIEW_ALREADY_EXISTS`, `FORBIDDEN`
- `PATCH /admin/orders/:orderId/status`: `ORDER_NOT_FOUND`, `INVALID_STATUS`
- `DELETE /admin/reviews/:reviewId`: `REVIEW_NOT_FOUND`, `FORBIDDEN`
