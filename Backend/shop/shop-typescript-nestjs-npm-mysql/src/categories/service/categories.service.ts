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
      where = ` where status = ?`;
    }
    return this.dataSource.query(
      `select id, name, display_order as displayOrder, status, created_at as createdAt, updated_at as updatedAt
       from categories ${where} order by display_order asc, id asc`,
      values,
    ) as Promise<Record<string, unknown>[]>;
  }

  async one(categoryId: number) {
    const rows = await this.dataSource.query(
      `select id, name, display_order as displayOrder, status, created_at as createdAt, updated_at as updatedAt
       from categories where id = ?`,
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
    const result = (await this.dataSource.query(
      `insert into categories (name, display_order, status)
       values (?, ?, 'ACTIVE')`,
      [name, displayOrder],
    )) as { insertId: number };
    return this.one(result.insertId);
  }

  async update(categoryId: number, body: Record<string, unknown>) {
    const current = await this.one(categoryId);
    const name = typeof body.name === "string" && body.name.trim() ? body.name.trim() : String(current.name);
    const displayOrder =
      body.displayOrder !== undefined ? ensureNumber(body.displayOrder, "displayOrder") : Number(current.displayOrder);
    const status =
      typeof body.status === "string" && body.status.trim() ? body.status.trim().toUpperCase() : String(current.status);
    await this.dataSource.query(
      `update categories
       set name = ?, display_order = ?, status = ?, updated_at = now()
       where id = ?`,
      [name, displayOrder, status, categoryId],
    );
    return this.one(categoryId);
  }

  async remove(categoryId: number) {
    await this.one(categoryId);
    await this.dataSource.query(`update categories set status = 'DELETED', updated_at = now() where id = ?`, [categoryId]);
    return { message: "Category deleted successfully" };
  }
}
