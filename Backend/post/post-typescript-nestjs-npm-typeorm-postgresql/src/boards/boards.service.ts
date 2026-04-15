import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { AppException } from "../common/app.exception";
import { ensureNumber, ensureString } from "../common/sql";

@Injectable()
export class BoardsService {
  constructor(private readonly dataSource: DataSource) {}

  async list(status?: string): Promise<Record<string, unknown>[]> {
    const values: unknown[] = [];
    let where = ` where b.status <> 'DELETED'`;
    if (status) {
      values.push(status.toUpperCase());
      where = ` where b.status = $1`;
    }
    return this.dataSource.query(
      `select b.id, b.name, b.description, b.display_order as "displayOrder", b.status, b.created_at as "createdAt", b.updated_at as "updatedAt"
       from boards b ${where}
       order by b.display_order asc, b.id asc`,
      values,
    ) as Promise<Record<string, unknown>[]>;
  }

  async one(boardId: number): Promise<Record<string, unknown>> {
    const rows = await this.dataSource.query(
      `select b.id, b.name, b.description, b.display_order as "displayOrder", b.status, b.created_at as "createdAt", b.updated_at as "updatedAt"
       from boards b where b.id = $1`,
      [boardId],
    );
    const board = rows[0] as Record<string, unknown> | undefined;
    if (!board) {
      throw new AppException("BOARD_NOT_FOUND", "Board not found", HttpStatus.NOT_FOUND);
    }
    return board;
  }

  async create(body: Record<string, unknown>, adminId: number): Promise<Record<string, unknown>> {
    const name = ensureString(body.name, "name");
    const description = typeof body.description === "string" ? body.description.trim() : null;
    const displayOrder = ensureNumber(body.displayOrder ?? 0, "displayOrder");
    const rows = await this.dataSource.query(
      `insert into boards (created_by_admin_id, name, description, display_order, status)
       values ($1, $2, $3, $4, 'ACTIVE')
       returning id, name, description, display_order as "displayOrder", status, created_at as "createdAt", updated_at as "updatedAt"`,
      [adminId, name, description, displayOrder],
    );
    return rows[0] as Record<string, unknown>;
  }

  async update(boardId: number, body: Record<string, unknown>): Promise<Record<string, unknown>> {
    const current = await this.one(boardId);
    const name = typeof body.name === "string" && body.name.trim() ? body.name.trim() : String(current.name);
    const description =
      typeof body.description === "string" ? body.description.trim() : (current.description as string | null | undefined) ?? null;
    const displayOrder =
      body.displayOrder !== undefined ? ensureNumber(body.displayOrder, "displayOrder") : Number(current.displayOrder);
    const status =
      typeof body.status === "string" && body.status.trim() ? body.status.trim().toUpperCase() : String(current.status);
    const rows = await this.dataSource.query(
      `update boards
       set name = $2, description = $3, display_order = $4, status = $5, updated_at = now()
       where id = $1
       returning id, name, description, display_order as "displayOrder", status, created_at as "createdAt", updated_at as "updatedAt"`,
      [boardId, name, description, displayOrder, status],
    );
    return rows[0] as Record<string, unknown>;
  }

  async remove(boardId: number): Promise<Record<string, string>> {
    await this.one(boardId);
    await this.dataSource.query(`update boards set status = 'DELETED', updated_at = now() where id = $1`, [boardId]);
    return { message: "Board deleted successfully" };
  }
}
