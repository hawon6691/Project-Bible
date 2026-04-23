import { INestApplication, ValidationPipe } from "@nestjs/common";
import { Test, TestingModule } from "@nestjs/testing";
import request from "supertest";
import { App } from "supertest/types";
import { AppModule } from "../src/app.module";
import { HttpExceptionFilter } from "../src/common/http-exception.filter";

describe("shop domain (e2e)", () => {
  let app: INestApplication<App>;

  beforeAll(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    app.useGlobalPipes(new ValidationPipe({ transform: true, whitelist: true }));
    app.useGlobalFilters(new HttpExceptionFilter());
    await app.init();
  });

  afterAll(async () => {
    await app.close();
  });

  const unique = (prefix: string) => `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}`;

  async function signupUser(prefix: string) {
    const email = `${unique(prefix)}@example.com`;
    const name = unique(`${prefix}-name`);
    const phone = `010${Math.floor(10000000 + Math.random() * 89999999)}`;
    const response = await request(app.getHttpServer())
      .post("/api/v1/auth/signup")
      .send({ email, password: "Password1!", name, phone })
      .expect(201);

    return {
      email,
      name,
      phone,
      userId: Number(response.body.data.id),
    };
  }

  async function loginUser(email: string) {
    const response = await request(app.getHttpServer())
      .post("/api/v1/auth/login")
      .send({ email, password: "Password1!" })
      .expect(201);

    return {
      accessToken: String(response.body.data.accessToken),
      refreshToken: String(response.body.data.refreshToken),
    };
  }

  async function loginAdmin() {
    const response = await request(app.getHttpServer())
      .post("/api/v1/admin/auth/login")
      .send({ email: "admin-shop-1@example.com", password: "AdminPassword1!" })
      .expect(201);

    return {
      accessToken: String(response.body.data.accessToken),
      refreshToken: String(response.body.data.refreshToken),
    };
  }

  it("serves health", async () => {
    await request(app.getHttpServer())
      .get("/api/v1/health")
      .expect(200)
      .expect(({ body }) => {
        expect(body.success).toBe(true);
        expect(body.data.domain).toBe("shop");
      });
  });

  it("supports user/admin auth flows and boundaries", async () => {
    const signed = await signupUser("shop-auth");

    await request(app.getHttpServer())
      .post("/api/v1/auth/login")
      .send({ email: signed.email, password: "WrongPassword1!" })
      .expect(401);

    const login = await loginUser(signed.email);

    await request(app.getHttpServer())
      .get("/api/v1/users/me")
      .set("Authorization", `Bearer ${login.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.email).toBe(signed.email);
        expect(body.data.name).toBe(signed.name);
      });

    await request(app.getHttpServer())
      .patch("/api/v1/users/me")
      .set("Authorization", `Bearer ${login.accessToken}`)
      .send({ name: `${signed.name}-updated` })
      .expect(200);

    await request(app.getHttpServer())
      .get("/api/v1/users/me")
      .set("Authorization", `Bearer ${login.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.name).toBe(`${signed.name}-updated`);
      });

    const refreshed = await request(app.getHttpServer())
      .post("/api/v1/auth/refresh")
      .send({ refreshToken: login.refreshToken })
      .expect(201);

    expect(refreshed.body.data.accessToken).toBeTruthy();

    await request(app.getHttpServer())
      .post("/api/v1/auth/refresh")
      .send({ refreshToken: "invalid-refresh-token" })
      .expect(401);

    await request(app.getHttpServer()).get("/api/v1/users/me").expect(401);

    await request(app.getHttpServer())
      .get("/api/v1/admin/me")
      .set("Authorization", `Bearer ${login.accessToken}`)
      .expect(403);

    await request(app.getHttpServer())
      .post("/api/v1/auth/logout")
      .set("Authorization", `Bearer ${login.accessToken}`)
      .expect(201);

    const adminLogin = await loginAdmin();

    await request(app.getHttpServer())
      .get("/api/v1/admin/me")
      .set("Authorization", `Bearer ${adminLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.email).toBe("admin-shop-1@example.com");
      });

    await request(app.getHttpServer())
      .get("/api/v1/admin/dashboard")
      .set("Authorization", `Bearer ${adminLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data).toHaveProperty("orderSummary");
        expect(body.data).toHaveProperty("productSummary");
        expect(body.data).toHaveProperty("userSummary");
        expect(body.data).toHaveProperty("reviewSummary");
      });

    const adminRefresh = await request(app.getHttpServer())
      .post("/api/v1/admin/auth/refresh")
      .send({ refreshToken: adminLogin.refreshToken })
      .expect(201);

    expect(adminRefresh.body.data.accessToken).toBeTruthy();

    await request(app.getHttpServer())
      .post("/api/v1/admin/auth/logout")
      .set("Authorization", `Bearer ${adminLogin.accessToken}`)
      .expect(201);
  });

  it("supports categories, products, cart, addresses, orders, payments, reviews, moderation, and boundaries", async () => {
    const admin = await loginAdmin();
    const buyer = await signupUser("shop-buyer");
    const other = await signupUser("shop-other");

    const buyerLogin = await loginUser(buyer.email);
    const otherLogin = await loginUser(other.email);

    const categoryCreate = await request(app.getHttpServer())
      .post("/api/v1/admin/categories")
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ name: unique("Category"), displayOrder: 91 })
      .expect(201);
    const categoryId = Number(categoryCreate.body.data.id);

    await request(app.getHttpServer())
      .patch(`/api/v1/admin/categories/${categoryId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ displayOrder: 92, status: "ACTIVE" })
      .expect(200);

    const productOneCreate = await request(app.getHttpServer())
      .post("/api/v1/admin/products")
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        categoryId,
        name: unique("Product-one"),
        description: "Product one description",
        price: 12000,
        stock: 30,
        status: "ACTIVE",
      })
      .expect(201);
    const productOneId = Number(productOneCreate.body.data.id);

    const productTwoCreate = await request(app.getHttpServer())
      .post("/api/v1/admin/products")
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        categoryId,
        name: unique("Product-two"),
        description: "Product two description",
        price: 18000,
        stock: 40,
        status: "ACTIVE",
      })
      .expect(201);
    const productTwoId = Number(productTwoCreate.body.data.id);

    const productUpdate = await request(app.getHttpServer())
      .patch(`/api/v1/admin/products/${productOneId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        categoryId,
        name: unique("Product-one-updated"),
        description: "Product one description updated",
        price: 12500,
        stock: 30,
        status: "ACTIVE",
      });

    expect(productUpdate.status).toBe(200);
    expect(Number(productUpdate.body.data.price)).toBe(12500);

    const optionCreate = await request(app.getHttpServer())
      .post(`/api/v1/admin/products/${productOneId}/options`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        name: "Size",
        value: "Large",
        additionalPrice: 2000,
        stock: 10,
      })
      .expect(201);
    const optionId = Number(optionCreate.body.data.id);

    await request(app.getHttpServer())
      .patch(`/api/v1/admin/product-options/${optionId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ stock: 9 })
      .expect(200);

    const imageCreate = await request(app.getHttpServer())
      .post(`/api/v1/admin/products/${productOneId}/images`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        imageUrl: "https://example.com/product-one.jpg",
        isPrimary: true,
        displayOrder: 0,
      })
      .expect(201);
    const imageId = Number(imageCreate.body.data.id);

    await request(app.getHttpServer())
      .patch(`/api/v1/admin/product-images/${imageId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ displayOrder: 1 })
      .expect(200);

    await request(app.getHttpServer())
      .get("/api/v1/categories")
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.some((item: { id: number | string }) => Number(item.id) === categoryId)).toBe(true);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/categories/${categoryId}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.id)).toBe(categoryId);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/products?page=1&limit=10&categoryId=${categoryId}&search=Product&sort=price_desc`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
        expect(body.data[0]).toHaveProperty("price");
      });

    await request(app.getHttpServer())
      .get(`/api/v1/products/${productOneId}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.id)).toBe(productOneId);
        expect(body.data.options.length).toBeGreaterThan(0);
        expect(body.data.images.length).toBeGreaterThan(0);
        expect(Number(body.data.options[0].stock)).toBe(9);
        expect(Number(body.data.images[0].displayOrder)).toBe(1);
      });

    const addressCreate = await request(app.getHttpServer())
      .post("/api/v1/addresses")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({
        recipientName: "Buyer Primary",
        phone: "01012345678",
        zipCode: "12345",
        address1: "Seoul main street",
        address2: "101",
        isDefault: true,
      })
      .expect(201);
    const addressId = Number(addressCreate.body.data.id);

    await request(app.getHttpServer())
      .patch(`/api/v1/addresses/${addressId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ address2: "202" })
      .expect(200);

    await request(app.getHttpServer())
      .get("/api/v1/addresses")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.length).toBeGreaterThan(0);
        const updatedAddress = body.data.find((item: { id: number | string }) => Number(item.id) === addressId);
        expect(updatedAddress.address2).toBe("202");
      });

    const cartOneCreate = await request(app.getHttpServer())
      .post("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ productId: productOneId, productOptionId: optionId, quantity: 1 })
      .expect(201);
    const cartOneId = Number(cartOneCreate.body.data.id);

    const cartTwoCreate = await request(app.getHttpServer())
      .post("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ productId: productTwoId, quantity: 2 })
      .expect(201);
    const cartTwoId = Number(cartTwoCreate.body.data.id);

    await request(app.getHttpServer())
      .get("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.length).toBeGreaterThanOrEqual(2);
      });

    await request(app.getHttpServer())
      .patch(`/api/v1/cart-items/${cartTwoId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ quantity: 1 })
      .expect(200);

    await request(app.getHttpServer())
      .get("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        const updatedCart = body.data.find((item: { id: number | string }) => Number(item.id) === cartTwoId);
        expect(Number(updatedCart.quantity)).toBe(1);
      });

    const disposableCart = await request(app.getHttpServer())
      .post("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ productId: productTwoId, quantity: 1 })
      .expect(201);
    const disposableCartId = Number(disposableCart.body.data.id);

    await request(app.getHttpServer())
      .delete(`/api/v1/cart-items/${disposableCartId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200);

    const clearCartItem = await request(app.getHttpServer())
      .post("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ productId: productTwoId, quantity: 1 })
      .expect(201);
    expect(clearCartItem.body.data.id).toBeTruthy();

    await request(app.getHttpServer())
      .delete("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200);

    const orderCartOne = await request(app.getHttpServer())
      .post("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ productId: productOneId, productOptionId: optionId, quantity: 1 })
      .expect(201);

    const orderCartTwo = await request(app.getHttpServer())
      .post("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ productId: productTwoId, quantity: 1 })
      .expect(201);

    const orderCreate = await request(app.getHttpServer())
      .post("/api/v1/orders")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ cartItemIds: [orderCartOne.body.data.id, orderCartTwo.body.data.id], addressId })
      .expect(201);

    const orderId = Number(orderCreate.body.data.order.id);
    expect(orderCreate.body.data.orderAddress).toBeTruthy();
    expect(orderCreate.body.data.orderItems.length).toBe(2);
    expect(orderCreate.body.data.payment).toBeNull();

    await request(app.getHttpServer())
      .get(`/api/v1/orders/${orderId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.order.id)).toBe(orderId);
        expect(body.data.orderItems.length).toBe(2);
      });

    await request(app.getHttpServer())
      .get("/api/v1/orders?page=1&limit=10&status=PENDING")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
      });

    const paymentCreate = await request(app.getHttpServer())
      .post("/api/v1/payments")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ orderId, paymentMethod: "mock" })
      .expect(201);
    const paymentId = Number(paymentCreate.body.data.id);

    await request(app.getHttpServer())
      .get(`/api/v1/payments/${paymentId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.id)).toBe(paymentId);
      });

    const orderDetailAfterPayment = await request(app.getHttpServer())
      .get(`/api/v1/orders/${orderId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200);

    const reviewableOrderItem = orderDetailAfterPayment.body.data.orderItems.find(
      (item: { productId: number | string }) => Number(item.productId) === productOneId,
    );
    const orderItemId = Number(reviewableOrderItem.id);

    const reviewCreate = await request(app.getHttpServer())
      .post(`/api/v1/order-items/${orderItemId}/reviews`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ rating: 5, content: "Excellent" })
      .expect(201);
    const reviewId = Number(reviewCreate.body.data.id);

    await request(app.getHttpServer())
      .post(`/api/v1/order-items/${orderItemId}/reviews`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ rating: 4, content: "Duplicate" })
      .expect(409);

    await request(app.getHttpServer())
      .patch(`/api/v1/reviews/${reviewId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ rating: 4, content: "Updated review" })
      .expect(200);

    await request(app.getHttpServer())
      .patch(`/api/v1/reviews/${reviewId}`)
      .set("Authorization", `Bearer ${otherLogin.accessToken}`)
      .send({ rating: 1, content: "forbidden" })
      .expect(403);

    await request(app.getHttpServer())
      .get(`/api/v1/products/${productOneId}/reviews?page=1&limit=10`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
        const updatedReview = body.data.find((item: { id: number | string }) => Number(item.id) === reviewId);
        expect(Number(updatedReview.rating)).toBe(4);
      });

    await request(app.getHttpServer())
      .get("/api/v1/admin/orders?page=1&limit=10&status=PAID")
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/admin/orders/${orderId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.order.id)).toBe(orderId);
      });

    await request(app.getHttpServer())
      .patch(`/api/v1/admin/orders/${orderId}/status`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ orderStatus: "PREPARING" })
      .expect(200);

    await request(app.getHttpServer())
      .get(`/api/v1/admin/orders/${orderId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.order.orderStatus).toBe("PREPARING");
      });

    await request(app.getHttpServer())
      .get("/api/v1/admin/reviews?page=1&limit=10")
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/admin/reviews/${reviewId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.id)).toBe(reviewId);
      });

    await request(app.getHttpServer())
      .delete(`/api/v1/admin/reviews/${reviewId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200);

    await request(app.getHttpServer())
      .post(`/api/v1/payments/${paymentId}/refund`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(201);

    await request(app.getHttpServer())
      .get(`/api/v1/payments/${paymentId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.paymentStatus).toBe("REFUNDED");
      });

    const cancellableCart = await request(app.getHttpServer())
      .post("/api/v1/cart-items")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ productId: productTwoId, quantity: 1 })
      .expect(201);

    const cancellableOrder = await request(app.getHttpServer())
      .post("/api/v1/orders")
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .send({ cartItemIds: [cancellableCart.body.data.id], addressId })
      .expect(201);

    await request(app.getHttpServer())
      .post(`/api/v1/orders/${cancellableOrder.body.data.order.id}/cancel`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(201)
      .expect(({ body }) => {
        expect(body.data.orderStatus).toBe("CANCELLED");
      });

    const disposableProduct = await request(app.getHttpServer())
      .post("/api/v1/admin/products")
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        categoryId,
        name: unique("Disposable-product"),
        description: "Disposable",
        price: 9000,
        stock: 5,
      })
      .expect(201);

    const disposableOption = await request(app.getHttpServer())
      .post(`/api/v1/admin/products/${Number(disposableProduct.body.data.id)}/options`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        name: "Disposable size",
        value: "Small",
        additionalPrice: 0,
        stock: 5,
      })
      .expect(201);

    const disposableImage = await request(app.getHttpServer())
      .post(`/api/v1/admin/products/${Number(disposableProduct.body.data.id)}/images`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        imageUrl: "https://example.com/disposable-product.jpg",
        isPrimary: false,
        displayOrder: 0,
      })
      .expect(201);

    await request(app.getHttpServer())
      .delete(`/api/v1/admin/product-options/${Number(disposableOption.body.data.id)}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200);

    await request(app.getHttpServer())
      .delete(`/api/v1/admin/product-images/${Number(disposableImage.body.data.id)}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200);

    await request(app.getHttpServer())
      .delete(`/api/v1/admin/products/${Number(disposableProduct.body.data.id)}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200);

    await request(app.getHttpServer())
      .delete(`/api/v1/addresses/${addressId}`)
      .set("Authorization", `Bearer ${buyerLogin.accessToken}`)
      .expect(200);

    await request(app.getHttpServer())
      .delete(`/api/v1/admin/categories/${categoryId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200);
  });
});
