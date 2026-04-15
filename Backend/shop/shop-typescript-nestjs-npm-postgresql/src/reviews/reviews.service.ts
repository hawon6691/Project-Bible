import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { pageMeta } from "../common/api-response";
import { AppException } from "../common/app.exception";
import { CurrentUserPayload } from "../common/current-user.decorator";
import { ensureNumber, ensureString, readPage } from "../common/sql";

@Injectable()
export class ReviewsService {
  constructor(private readonly dataSource: DataSource) {}

  async list(productId: number, query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const countRows = await this.dataSource.query(
      `select count(*)::int as count from reviews where product_id = $1 and status = 'ACTIVE' and deleted_at is null`,
      [productId],
    );
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    const rows = await this.dataSource.query(
      `select r.id, r.order_item_id as "orderItemId", r.product_id as "productId", r.user_id as "userId", r.rating, r.content, r.status,
              r.created_at as "createdAt", json_build_object('id', u.id, 'name', u.name) as author
       from reviews r
       join users u on u.id = r.user_id
       where r.product_id = $1 and r.status = 'ACTIVE' and r.deleted_at is null
       order by ${this.resolveSort(query.sort)}
       limit $2 offset $3`,
      [productId, limit, offset],
    );
    return { items: rows as Record<string, unknown>[], meta: pageMeta(page, limit, totalCount) };
  }

  async create(user: CurrentUserPayload, orderItemId: number, body: Record<string, unknown>) {
    this.assertUser(user);
    const orderItemRows = await this.dataSource.query(
      `select oi.id, oi.product_id as "productId", o.user_id as "userId"
       from order_items oi
       join orders o on o.id = oi.order_id
       where oi.id = $1`,
      [orderItemId],
    );
    const orderItem = orderItemRows[0] as { productId: number; userId: number } | undefined;
    if (!orderItem) {
      throw new AppException("ORDER_NOT_FOUND", "Order item not found", HttpStatus.NOT_FOUND);
    }
    if (orderItem.userId !== user.sub) {
      throw new AppException("FORBIDDEN", "You cannot review this order item", HttpStatus.FORBIDDEN);
    }
    const duplicate = await this.dataSource.query(`select id from reviews where order_item_id = $1`, [orderItemId]);
    if (duplicate.length > 0) {
      throw new AppException("REVIEW_ALREADY_EXISTS", "Review already exists", HttpStatus.CONFLICT);
    }
    const rows = await this.dataSource.query(
      `insert into reviews (order_item_id, product_id, user_id, rating, content, status)
       values ($1, $2, $3, $4, $5, 'ACTIVE')
       returning id, order_item_id as "orderItemId", product_id as "productId", user_id as "userId", rating, content, status, created_at as "createdAt"`,
      [orderItemId, orderItem.productId, user.sub, ensureNumber(body.rating, "rating"), typeof body.content === "string" ? body.content.trim() : null],
    );
    return rows[0] as Record<string, unknown>;
  }

  async one(reviewId: number) {
    const rows = await this.dataSource.query(
      `select r.id, r.order_item_id as "orderItemId", r.product_id as "productId", r.user_id as "userId", r.rating, r.content, r.status,
              r.created_at as "createdAt", r.updated_at as "updatedAt",
              json_build_object('id', u.id, 'name', u.name) as author
       from reviews r
       join users u on u.id = r.user_id
       where r.id = $1`,
      [reviewId],
    );
    const review = rows[0] as Record<string, unknown> | undefined;
    if (!review) {
      throw new AppException("REVIEW_NOT_FOUND", "Review not found", HttpStatus.NOT_FOUND);
    }
    return review;
  }

  async update(user: CurrentUserPayload, reviewId: number, body: Record<string, unknown>) {
    this.assertUser(user);
    const current = await this.one(reviewId);
    if (Number(current.userId) !== user.sub) {
      throw new AppException("FORBIDDEN", "Only the author can update this review", HttpStatus.FORBIDDEN);
    }
    const rows = await this.dataSource.query(
      `update reviews
       set rating = $2, content = $3, updated_at = now()
       where id = $1
       returning id, order_item_id as "orderItemId", product_id as "productId", user_id as "userId", rating, content, status, updated_at as "updatedAt"`,
      [reviewId, body.rating !== undefined ? ensureNumber(body.rating, "rating") : Number(current.rating), typeof body.content === "string" ? body.content.trim() : String(current.content ?? "")],
    );
    return rows[0] as Record<string, unknown>;
  }

  async remove(user: CurrentUserPayload, reviewId: number) {
    this.assertUser(user);
    const current = await this.one(reviewId);
    if (Number(current.userId) !== user.sub) {
      throw new AppException("FORBIDDEN", "Only the author can delete this review", HttpStatus.FORBIDDEN);
    }
    await this.dataSource.query(`update reviews set status = 'DELETED', deleted_at = now(), updated_at = now() where id = $1`, [reviewId]);
    return { message: "Review deleted successfully" };
  }

  async adminList(query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const values: unknown[] = [];
    const filters: string[] = [`1=1`];
    if (query.search) {
      values.push(`%${query.search.trim()}%`);
      filters.push(`(coalesce(r.content,'') ilike $${values.length} or u.name ilike $${values.length})`);
    }
    if (query.status) {
      values.push(query.status.toUpperCase());
      filters.push(`r.status = $${values.length}`);
    }
    const where = `where ${filters.join(" and ")}`;
    const countRows = await this.dataSource.query(`select count(*)::int as count from reviews r join users u on u.id = r.user_id ${where}`, values);
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    values.push(limit, offset);
    const rows = await this.dataSource.query(
      `select r.id, r.product_id as "productId", r.rating, r.status, r.created_at as "createdAt",
              json_build_object('id', u.id, 'name', u.name) as author
       from reviews r
       join users u on u.id = r.user_id
       ${where}
       order by r.created_at desc
       limit $${values.length - 1} offset $${values.length}`,
      values,
    );
    return { items: rows as Record<string, unknown>[], meta: pageMeta(page, limit, totalCount) };
  }

  async adminRemove(reviewId: number) {
    await this.one(reviewId);
    await this.dataSource.query(`update reviews set status = 'DELETED', deleted_at = now(), updated_at = now() where id = $1`, [reviewId]);
    return { message: "Review deleted successfully" };
  }

  private resolveSort(sort?: string): string {
    switch ((sort ?? "latest").toLowerCase()) {
      case "rating_desc":
        return `r.rating desc, r.id desc`;
      case "rating_asc":
        return `r.rating asc, r.id desc`;
      default:
        return `r.created_at desc, r.id desc`;
    }
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
