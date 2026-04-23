import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { AppException } from "../../common/app.exception";
import { CurrentUserPayload } from "../../common/current-user.decorator";
import { ensureString } from "../../common/sql";

@Injectable()
export class AddressesService {
  constructor(private readonly dataSource: DataSource) {}

  async list(user: CurrentUserPayload) {
    this.assertUser(user);
    return this.dataSource.query(
      `select id, recipient_name as recipientName, phone, zip_code as zipCode, address1, address2, is_default as isDefault,
              created_at as createdAt, updated_at as updatedAt
       from addresses where user_id = ? order by is_default desc, id asc`,
      [user.sub],
    ) as Promise<Record<string, unknown>[]>;
  }

  async create(user: CurrentUserPayload, body: Record<string, unknown>) {
    this.assertUser(user);
    const isDefault = Boolean(body.isDefault ?? false);
    if (isDefault) {
      await this.dataSource.query(`update addresses set is_default = false, updated_at = now() where user_id = ?`, [user.sub]);
    }
    const result = (await this.dataSource.query(
      `insert into addresses (user_id, recipient_name, phone, zip_code, address1, address2, is_default)
       values (?, ?, ?, ?, ?, ?, ?)`,
      [
        user.sub,
        ensureString(body.recipientName, "recipientName"),
        ensureString(body.phone, "phone"),
        ensureString(body.zipCode, "zipCode"),
        ensureString(body.address1, "address1"),
        typeof body.address2 === "string" ? body.address2.trim() : null,
        isDefault,
      ],
    )) as { insertId: number };
    return this.one(user, result.insertId);
  }

  async update(user: CurrentUserPayload, addressId: number, body: Record<string, unknown>) {
    this.assertUser(user);
    const current = await this.one(user, addressId);
    const isDefault = body.isDefault !== undefined ? Boolean(body.isDefault) : Boolean(current.isDefault);
    if (isDefault) {
      await this.dataSource.query(`update addresses set is_default = false, updated_at = now() where user_id = ?`, [user.sub]);
    }
    await this.dataSource.query(
      `update addresses
       set recipient_name = ?, phone = ?, zip_code = ?, address1 = ?, address2 = ?, is_default = ?, updated_at = now()
       where id = ? and user_id = ?`,
      [
        typeof body.recipientName === "string" && body.recipientName.trim() ? body.recipientName.trim() : String(current.recipientName),
        typeof body.phone === "string" && body.phone.trim() ? body.phone.trim() : String(current.phone),
        typeof body.zipCode === "string" && body.zipCode.trim() ? body.zipCode.trim() : String(current.zipCode),
        typeof body.address1 === "string" && body.address1.trim() ? body.address1.trim() : String(current.address1),
        typeof body.address2 === "string" ? body.address2.trim() : (current.address2 as string | null | undefined) ?? null,
        isDefault,
        addressId,
        user.sub,
      ],
    );
    return this.one(user, addressId);
  }

  async remove(user: CurrentUserPayload, addressId: number) {
    this.assertUser(user);
    await this.one(user, addressId);
    await this.dataSource.query(`delete from addresses where id = ? and user_id = ?`, [addressId, user.sub]);
    return { message: "Address deleted successfully" };
  }

  async one(user: CurrentUserPayload, addressId: number) {
    const rows = await this.dataSource.query(
      `select id, recipient_name as recipientName, phone, zip_code as zipCode, address1, address2, is_default as isDefault,
              created_at as createdAt, updated_at as updatedAt
       from addresses where id = ? and user_id = ?`,
      [addressId, user.sub],
    );
    const address = rows[0] as Record<string, unknown> | undefined;
    if (!address) {
      throw new AppException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND);
    }
    return address;
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
