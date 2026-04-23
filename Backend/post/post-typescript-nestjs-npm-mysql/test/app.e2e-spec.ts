import { INestApplication, ValidationPipe } from "@nestjs/common";
import { Test, TestingModule } from "@nestjs/testing";
import request from "supertest";
import { App } from "supertest/types";
import { AppModule } from "../src/app.module";
import { HttpExceptionFilter } from "../src/common/http-exception.filter";

describe("post domain (e2e)", () => {
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
    const nickname = unique(`${prefix}-nick`);
    const response = await request(app.getHttpServer())
      .post("/api/v1/auth/signup")
      .send({ email, password: "Password1!", nickname })
      .expect(201);

    return {
      email,
      nickname,
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
      .send({ email: "admin-post-1@example.com", password: "AdminPassword1!" })
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
        expect(body.data.domain).toBe("post");
      });
  });

  it("supports user/admin auth flows and boundaries", async () => {
    const signed = await signupUser("post-auth");

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
        expect(body.data.nickname).toBe(signed.nickname);
      });

    await request(app.getHttpServer())
      .patch("/api/v1/users/me")
      .set("Authorization", `Bearer ${login.accessToken}`)
      .send({ nickname: `${signed.nickname}-updated` })
      .expect(200);

    await request(app.getHttpServer())
      .get("/api/v1/users/me")
      .set("Authorization", `Bearer ${login.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.nickname).toBe(`${signed.nickname}-updated`);
      });

    const refreshed = await request(app.getHttpServer())
      .post("/api/v1/auth/refresh")
      .send({ refreshToken: login.refreshToken })
      .expect(201);

    expect(refreshed.body.data.accessToken).toBeTruthy();
    expect(refreshed.body.data.refreshToken).toBeTruthy();

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
      .expect(201)
      .expect(({ body }) => {
        expect(body.data.message).toContain("Logged out");
      });

    const adminLogin = await loginAdmin();

    await request(app.getHttpServer())
      .get("/api/v1/admin/me")
      .set("Authorization", `Bearer ${adminLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.email).toBe("admin-post-1@example.com");
      });

    await request(app.getHttpServer())
      .get("/api/v1/admin/dashboard")
      .set("Authorization", `Bearer ${adminLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data).toHaveProperty("boardCount");
        expect(body.data).toHaveProperty("postCount");
        expect(body.data).toHaveProperty("commentCount");
        expect(body.data).toHaveProperty("hiddenPostCount");
        expect(body.data).toHaveProperty("hiddenCommentCount");
      });

    const adminRefresh = await request(app.getHttpServer())
      .post("/api/v1/admin/auth/refresh")
      .send({ refreshToken: adminLogin.refreshToken })
      .expect(201);

    expect(adminRefresh.body.data.accessToken).toBeTruthy();

    await request(app.getHttpServer())
      .post("/api/v1/admin/auth/logout")
      .set("Authorization", `Bearer ${adminLogin.accessToken}`)
      .expect(201)
      .expect(({ body }) => {
        expect(body.data.message).toContain("Admin logged out");
      });
  });

  it("supports boards, posts, comments, likes, moderation, and authorization boundaries", async () => {
    const admin = await loginAdmin();
    const author = await signupUser("post-author");
    const other = await signupUser("post-other");
    const commenter = await signupUser("post-commenter");

    const authorLogin = await loginUser(author.email);
    const otherLogin = await loginUser(other.email);
    const commenterLogin = await loginUser(commenter.email);

    const boardCreate = await request(app.getHttpServer())
      .post("/api/v1/admin/boards")
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({
        name: unique("Board"),
        description: "Board for post e2e",
        displayOrder: 99,
      })
      .expect(201);

    const boardId = Number(boardCreate.body.data.id);

    await request(app.getHttpServer())
      .patch(`/api/v1/admin/boards/${boardId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ description: "Board for post e2e updated", status: "ACTIVE" })
      .expect(200);

    await request(app.getHttpServer())
      .get("/api/v1/boards")
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.length).toBeGreaterThan(0);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/boards/${boardId}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.id)).toBe(boardId);
      });

    const createPost = await request(app.getHttpServer())
      .post("/api/v1/posts")
      .set("Authorization", `Bearer ${authorLogin.accessToken}`)
      .send({
        boardId,
        title: unique("Post title"),
        content: "Post content for e2e verification",
      })
      .expect(201);

    const postId = Number(createPost.body.data.id);

    await request(app.getHttpServer())
      .patch(`/api/v1/posts/${postId}`)
      .set("Authorization", `Bearer ${authorLogin.accessToken}`)
      .send({ title: "Updated post title" })
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.title).toBe("Updated post title");
      });

    await request(app.getHttpServer())
      .patch(`/api/v1/posts/${postId}`)
      .set("Authorization", `Bearer ${otherLogin.accessToken}`)
      .send({ title: "forbidden" })
      .expect(403);

    const firstDetail = await request(app.getHttpServer()).get(`/api/v1/posts/${postId}`).expect(200);
    const secondDetail = await request(app.getHttpServer()).get(`/api/v1/posts/${postId}`).expect(200);
    expect(Number(secondDetail.body.data.viewCount)).toBeGreaterThanOrEqual(Number(firstDetail.body.data.viewCount));

    await request(app.getHttpServer())
      .get(`/api/v1/posts?page=1&limit=10&boardId=${boardId}&search=Updated&sort=like_count`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.page).toBe(1);
        expect(body.meta.limit).toBe(10);
        expect(body.meta.totalCount).toBeGreaterThan(0);
        expect(body.data[0]).toHaveProperty("viewCount");
        expect(body.data[0]).toHaveProperty("likeCount");
        expect(body.data[0]).toHaveProperty("commentCount");
      });

    const commentCreate = await request(app.getHttpServer())
      .post(`/api/v1/posts/${postId}/comments`)
      .set("Authorization", `Bearer ${commenterLogin.accessToken}`)
      .send({ content: "Comment content" })
      .expect(201);

    const commentId = Number(commentCreate.body.data.id);

    await request(app.getHttpServer())
      .get(`/api/v1/posts/${postId}/comments?page=1&limit=10`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
      });

    await request(app.getHttpServer())
      .patch(`/api/v1/comments/${commentId}`)
      .set("Authorization", `Bearer ${commenterLogin.accessToken}`)
      .send({ content: "Comment content updated" })
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.content).toBe("Comment content updated");
      });

    await request(app.getHttpServer())
      .patch(`/api/v1/comments/${commentId}`)
      .set("Authorization", `Bearer ${authorLogin.accessToken}`)
      .send({ content: "forbidden" })
      .expect(403);

    const likeCreate = await request(app.getHttpServer())
      .post(`/api/v1/posts/${postId}/likes`)
      .set("Authorization", `Bearer ${commenterLogin.accessToken}`)
      .expect(201);

    expect(likeCreate.body.data.liked).toBe(true);

    await request(app.getHttpServer())
      .post(`/api/v1/posts/${postId}/likes`)
      .set("Authorization", `Bearer ${commenterLogin.accessToken}`)
      .expect(409);

    await request(app.getHttpServer())
      .delete(`/api/v1/posts/${postId}/likes`)
      .set("Authorization", `Bearer ${commenterLogin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.liked).toBe(false);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/admin/posts?page=1&limit=10&status=ACTIVE`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/admin/posts/${postId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.id)).toBe(postId);
      });

    const adminPostStatus = await request(app.getHttpServer())
      .patch(`/api/v1/admin/posts/${postId}/status`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ status: "HIDDEN" });

    expect(adminPostStatus.status).toBe(200);

    await request(app.getHttpServer())
      .get(`/api/v1/admin/posts/${postId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.status).toBe("HIDDEN");
      });

    await request(app.getHttpServer())
      .get(`/api/v1/admin/comments?page=1&limit=10&status=ACTIVE`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.meta.totalCount).toBeGreaterThan(0);
      });

    await request(app.getHttpServer())
      .get(`/api/v1/admin/comments/${commentId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(Number(body.data.id)).toBe(commentId);
      });

    await request(app.getHttpServer())
      .patch(`/api/v1/admin/comments/${commentId}/status`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .send({ status: "HIDDEN" })
      .expect(200);

    await request(app.getHttpServer())
      .get(`/api/v1/admin/comments/${commentId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200)
      .expect(({ body }) => {
        expect(body.data.status).toBe("HIDDEN");
      });

    const disposablePost = await request(app.getHttpServer())
      .post("/api/v1/posts")
      .set("Authorization", `Bearer ${authorLogin.accessToken}`)
      .send({
        boardId,
        title: unique("Disposable post"),
        content: "Disposable content",
      })
      .expect(201);

    const disposablePostId = Number(disposablePost.body.data.id);

    const disposableComment = await request(app.getHttpServer())
      .post(`/api/v1/posts/${disposablePostId}/comments`)
      .set("Authorization", `Bearer ${commenterLogin.accessToken}`)
      .send({ content: "Disposable comment" })
      .expect(201);

    const disposableCommentId = Number(disposableComment.body.data.id);

    await request(app.getHttpServer())
      .delete(`/api/v1/comments/${disposableCommentId}`)
      .set("Authorization", `Bearer ${commenterLogin.accessToken}`)
      .expect(200);

    await request(app.getHttpServer())
      .delete(`/api/v1/posts/${disposablePostId}`)
      .set("Authorization", `Bearer ${authorLogin.accessToken}`)
      .expect(200);

    await request(app.getHttpServer())
      .delete(`/api/v1/admin/boards/${boardId}`)
      .set("Authorization", `Bearer ${admin.accessToken}`)
      .expect(200);
  });
});
