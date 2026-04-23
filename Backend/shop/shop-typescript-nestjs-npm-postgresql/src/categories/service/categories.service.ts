import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { AppException } from "../../common/app.exception";
import { ensureNumber, ensureString } from "../../common/sql";

@Injectable()
export class CategoriesService {
  constructor(private readonly dataSource: DataSource) {}

  async list(status?: string) {
    const values: unknown[] = [];
    let where = ` where status <> 'DELETED'`;
    if (status) {
      values.push(status.toUpperCase());
      where = ` where status = $1`;
    }
    return this.dataSource.query(
      `select id, name, display_order as "displayOrder", status, created_at as "createdAt", updated_at as "updatedAt"
       from categories ${where} order by display_order asc, id asc`,
      values,
    ) as Promise<Record<string, unknown>[]>;
  }

  async one(categoryId: number) {
    const rows = await this.dataSource.query(
      `select id, name, display_order as "displayOrder", status, created_at as "createdAt", updated_at as "updatedAt"
       from categories where id = $1`,
      [categoryId],
    );
    const category = rows[0] as Record<string, unknown> | undefined;
    if (!category) {
      throw new AppException("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND);
    }
    return category;
  }

  async create(body: Record<string, unknown>) {
    const name = ensureString(body.name, "name");
    const displayOrder = ensureNumber(body.displayOrder ?? 0, "displayOrder");
    const rows = await this.dataSource.query(
      `insert into categories (name, display_order, status)
       values ($1, $2, 'ACTIVE')
       returning id, name, display_order as "displayOrder", status, created_at as "createdAt", updated_at as "updatedAt"`,
      [name, displayOrder],
    );
    return rows[0] as Record<string, unknown>;
  }

  async update(categoryId: number, body: Record<string, unknown>) {
    const current = await this.one(categoryId);
    const name = typeof body.name === "string" && body.name.trim() ? body.name.trim() : String(current.name);
    const displayOrder =
      body.displayOrder !== undefined ? ensureNumber(body.displayOrder, "displayOrder") : Number(current.displayOrder);
    const status =
      typeof body.status === "string" && body.status.trim() ? body.status.trim().toUpperCase() : String(current.status);
    const rows = await this.dataSource.query(
      `update categories
       set name = $2, display_order = $3, status = $4, updated_at = now()
       where id = $1
       returning id, name, display_order as "displayOrder", status, created_at as "createdAt", updated_at as "updatedAt"`,
      [categoryId, name, displayOrder, status],
    );
    return rows[0] as Record<string, unknown>;
  }

  async remove(categoryId: number) {
    await this.one(categoryId);
    await this.dataSource.query(`update categories set status = 'DELETED', updated_at = now() where id = $1`, [categoryId]);
    return { message: "Category deleted successfully" };
  }
}
