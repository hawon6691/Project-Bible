import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { pageMeta } from "../../common/api-response";
import { AppException } from "../../common/app.exception";
import { ensureNumber, ensureString, readPage } from "../../common/sql";

@Injectable()
export class ProductsService {
  constructor(private readonly dataSource: DataSource) {}

  async list(query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const values: unknown[] = [];
    const filters: string[] = [`p.status <> 'DELETED'`, `p.status = 'ACTIVE'`];
    if (query.categoryId) {
      values.push(Number(query.categoryId));
      filters.push(`p.category_id = ?`);
    }
    if (query.search) {
      values.push(`%${query.search.trim()}%`);
      filters.push(`(p.name like ? or coalesce(p.description,'') like ?)`);
      values.push(values[values.length - 1]);
    }
    if (query.status) {
      filters.pop();
      values.push(query.status.toUpperCase());
      filters.push(`p.status = ?`);
    }
    const where = `where ${filters.join(" and ")}`;
    const countRows = await this.dataSource.query(`select count(*) as count from products p ${where}`, values);
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    values.push(limit, offset);
    const rows = await this.dataSource.query(
      `select p.id,
              p.category_id as categoryId,
              p.name,
              p.price,
              p.stock,
              p.status,
              (
                select pi.image_url
                from product_images pi
                where pi.product_id = p.id
                order by pi.is_primary desc, pi.display_order asc, pi.id asc
                limit 1
              ) as thumbnailUrl
       from products p
       ${where}
       order by ${this.resolveSort(query.sort)}
       limit ? offset ?`,
      values,
    );
    return { items: rows as Record<string, unknown>[], meta: pageMeta(page, limit, totalCount) };
  }

  async one(productId: number) {
    const rows = await this.dataSource.query(
      `select p.id, p.category_id as categoryId, p.name, p.description, p.price, p.stock, p.status,
              p.created_at as createdAt, p.updated_at as updatedAt
       from products p where p.id = ? and p.status <> 'DELETED'`,
      [productId],
    );
    const product = rows[0] as Record<string, unknown> | undefined;
    if (!product) {
      throw new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND);
    }
    const [options, images] = await Promise.all([
      this.dataSource.query(
        `select id, product_id as productId, name, value, additional_price as additionalPrice, stock,
                created_at as createdAt, updated_at as updatedAt
         from product_options where product_id = ? order by id asc`,
        [productId],
      ),
      this.dataSource.query(
        `select id, product_id as productId, image_url as imageUrl, is_primary as isPrimary, display_order as displayOrder,
                created_at as createdAt, updated_at as updatedAt
         from product_images where product_id = ? order by display_order asc, id asc`,
        [productId],
      ),
    ]);
    return { ...product, options, images };
  }

  async create(body: Record<string, unknown>) {
    const categoryId = ensureNumber(body.categoryId, "categoryId");
    const name = ensureString(body.name, "name");
    const description = typeof body.description === "string" ? body.description.trim() : null;
    const price = ensureNumber(body.price, "price");
    const stock = ensureNumber(body.stock, "stock");
    const status = typeof body.status === "string" && body.status.trim() ? body.status.trim().toUpperCase() : "ACTIVE";
    await this.ensureCategory(categoryId);
    const result = (await this.dataSource.query(
      `insert into products (category_id, name, description, price, stock, status)
       values (?, ?, ?, ?, ?, ?)`,
      [categoryId, name, description, price, stock, status],
    )) as { insertId: number };
    return this.one(result.insertId);
  }

  async update(productId: number, body: Record<string, unknown>) {
    const current = (await this.one(productId)) as Record<string, unknown>;
    const categoryId =
      body.categoryId !== undefined ? ensureNumber(body.categoryId, "categoryId") : Number(current.categoryId);
    await this.ensureCategory(categoryId);
    await this.dataSource.query(
      `update products
       set category_id = ?,
           name = ?,
           description = ?,
           price = ?,
           stock = ?,
           status = ?,
           updated_at = now()
       where id = ?`,
      [
        categoryId,
        typeof body.name === "string" && body.name.trim() ? body.name.trim() : String(current.name),
        typeof body.description === "string" ? body.description.trim() : (current.description as string | null | undefined) ?? null,
        body.price !== undefined ? ensureNumber(body.price, "price") : Number(current.price),
        body.stock !== undefined ? ensureNumber(body.stock, "stock") : Number(current.stock),
        typeof body.status === "string" && body.status.trim() ? body.status.trim().toUpperCase() : String(current.status),
        productId,
      ],
    );
    return this.one(productId);
  }

  async remove(productId: number) {
    await this.one(productId);
    await this.dataSource.query(`update products set status = 'DELETED', deleted_at = now(), updated_at = now() where id = ?`, [productId]);
    return { message: "Product deleted successfully" };
  }

  async createOption(productId: number, body: Record<string, unknown>) {
    await this.one(productId);
    const result = (await this.dataSource.query(
      `insert into product_options (product_id, name, value, additional_price, stock)
       values (?, ?, ?, ?, ?)`,
      [
        productId,
        ensureString(body.name, "name"),
        ensureString(body.value, "value"),
        ensureNumber(body.additionalPrice ?? 0, "additionalPrice"),
        ensureNumber(body.stock, "stock"),
      ],
    )) as { insertId: number };
    const rows = await this.dataSource.query(
      `select id, product_id as productId, name, value, additional_price as additionalPrice, stock,
              created_at as createdAt, updated_at as updatedAt
       from product_options where id = ?`,
      [result.insertId],
    );
    return rows[0] as Record<string, unknown>;
  }

  async updateOption(optionId: number, body: Record<string, unknown>) {
    const rows = await this.dataSource.query(
      `select id, product_id as productId, name, value, additional_price as additionalPrice, stock from product_options where id = ?`,
      [optionId],
    );
    const current = rows[0] as Record<string, unknown> | undefined;
    if (!current) {
      throw new AppException("PRODUCT_OPTION_NOT_FOUND", "Product option not found", HttpStatus.NOT_FOUND);
    }
    await this.dataSource.query(
      `update product_options
       set name = ?, value = ?, additional_price = ?, stock = ?, updated_at = now()
       where id = ?`,
      [
        typeof body.name === "string" && body.name.trim() ? body.name.trim() : String(current.name),
        typeof body.value === "string" && body.value.trim() ? body.value.trim() : String(current.value),
        body.additionalPrice !== undefined ? ensureNumber(body.additionalPrice, "additionalPrice") : Number(current.additionalPrice),
        body.stock !== undefined ? ensureNumber(body.stock, "stock") : Number(current.stock),
        optionId,
      ],
    );
    const updatedRows = await this.dataSource.query(
      `select id, product_id as productId, name, value, additional_price as additionalPrice, stock,
              created_at as createdAt, updated_at as updatedAt
       from product_options where id = ?`,
      [optionId],
    );
    return updatedRows[0] as Record<string, unknown>;
  }

  async removeOption(optionId: number) {
    const rows = await this.dataSource.query(`select id from product_options where id = ?`, [optionId]);
    if (rows.length === 0) {
      throw new AppException("PRODUCT_OPTION_NOT_FOUND", "Product option not found", HttpStatus.NOT_FOUND);
    }
    await this.dataSource.query(`delete from product_options where id = ?`, [optionId]);
    return { message: "Product option deleted successfully" };
  }

  async createImage(productId: number, body: Record<string, unknown>) {
    await this.one(productId);
    const result = (await this.dataSource.query(
      `insert into product_images (product_id, image_url, is_primary, display_order)
       values (?, ?, ?, ?)`,
      [
        productId,
        ensureString(body.imageUrl, "imageUrl"),
        Boolean(body.isPrimary ?? false),
        ensureNumber(body.displayOrder ?? 0, "displayOrder"),
      ],
    )) as { insertId: number };
    const rows = await this.dataSource.query(
      `select id, product_id as productId, image_url as imageUrl, is_primary as isPrimary, display_order as displayOrder,
              created_at as createdAt, updated_at as updatedAt
       from product_images where id = ?`,
      [result.insertId],
    );
    return rows[0] as Record<string, unknown>;
  }

  async updateImage(imageId: number, body: Record<string, unknown>) {
    const rows = await this.dataSource.query(
      `select id, product_id as productId, image_url as imageUrl, is_primary as isPrimary, display_order as displayOrder
       from product_images where id = ?`,
      [imageId],
    );
    const current = rows[0] as Record<string, unknown> | undefined;
    if (!current) {
      throw new AppException("PRODUCT_NOT_FOUND", "Product image not found", HttpStatus.NOT_FOUND);
    }
    await this.dataSource.query(
      `update product_images
       set image_url = ?, is_primary = ?, display_order = ?, updated_at = now()
       where id = ?`,
      [
        typeof body.imageUrl === "string" && body.imageUrl.trim() ? body.imageUrl.trim() : String(current.imageUrl),
        body.isPrimary !== undefined ? Boolean(body.isPrimary) : Boolean(current.isPrimary),
        body.displayOrder !== undefined ? ensureNumber(body.displayOrder, "displayOrder") : Number(current.displayOrder),
        imageId,
      ],
    );
    const updatedRows = await this.dataSource.query(
      `select id, product_id as productId, image_url as imageUrl, is_primary as isPrimary, display_order as displayOrder,
              created_at as createdAt, updated_at as updatedAt
       from product_images where id = ?`,
      [imageId],
    );
    return updatedRows[0] as Record<string, unknown>;
  }

  async removeImage(imageId: number) {
    const rows = await this.dataSource.query(`select id from product_images where id = ?`, [imageId]);
    if (rows.length === 0) {
      throw new AppException("PRODUCT_NOT_FOUND", "Product image not found", HttpStatus.NOT_FOUND);
    }
    await this.dataSource.query(`delete from product_images where id = ?`, [imageId]);
    return { message: "Product image deleted successfully" };
  }

  private async ensureCategory(categoryId: number): Promise<void> {
    const rows = await this.dataSource.query(`select id from categories where id = ? and status <> 'DELETED'`, [categoryId]);
    if (rows.length === 0) {
      throw new AppException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND);
    }
  }

  private resolveSort(sort?: string): string {
    switch ((sort ?? "latest").toLowerCase()) {
      case "price_asc":
        return `p.price asc, p.id desc`;
      case "price_desc":
        return `p.price desc, p.id desc`;
      case "popular":
        return `p.stock desc, p.id desc`;
      default:
        return `p.created_at desc, p.id desc`;
    }
  }
}
