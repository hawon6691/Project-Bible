import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { AppException } from "../common/app.exception";
import { CurrentUserPayload } from "../common/current-user.decorator";
import { ensureNumber } from "../common/sql";

@Injectable()
export class CartService {
  constructor(private readonly dataSource: DataSource) {}

  async list(user: CurrentUserPayload) {
    this.assertUser(user);
    return this.dataSource.query(
      `select c.id, c.product_id as "productId", c.product_option_id as "productOptionId", c.quantity, c.created_at as "createdAt",
              p.name as "productName", p.price, p.stock
       from cart_items c
       join products p on p.id = c.product_id
       where c.user_id = $1
       order by c.id asc`,
      [user.sub],
    ) as Promise<Record<string, unknown>[]>;
  }

  async create(user: CurrentUserPayload, body: Record<string, unknown>) {
    this.assertUser(user);
    const productId = ensureNumber(body.productId, "productId");
    const productOptionId = body.productOptionId !== undefined ? ensureNumber(body.productOptionId, "productOptionId") : null;
    const quantity = ensureNumber(body.quantity, "quantity");
    await this.ensureStock(productId, productOptionId, quantity);
    const rows = await this.dataSource.query(
      `insert into cart_items (user_id, product_id, product_option_id, quantity)
       values ($1, $2, $3, $4)
       returning id, product_id as "productId", product_option_id as "productOptionId", quantity, created_at as "createdAt", updated_at as "updatedAt"`,
      [user.sub, productId, productOptionId, quantity],
    );
    return rows[0] as Record<string, unknown>;
  }

  async update(user: CurrentUserPayload, cartItemId: number, body: Record<string, unknown>) {
    this.assertUser(user);
    const current = await this.one(user, cartItemId);
    const quantity = ensureNumber(body.quantity, "quantity");
    await this.ensureStock(Number(current.productId), (current.productOptionId as number | null | undefined) ?? null, quantity);
    const rows = await this.dataSource.query(
      `update cart_items set quantity = $3, updated_at = now()
       where id = $1 and user_id = $2
       returning id, product_id as "productId", product_option_id as "productOptionId", quantity, created_at as "createdAt", updated_at as "updatedAt"`,
      [cartItemId, user.sub, quantity],
    );
    return rows[0] as Record<string, unknown>;
  }

  async remove(user: CurrentUserPayload, cartItemId: number) {
    this.assertUser(user);
    await this.one(user, cartItemId);
    await this.dataSource.query(`delete from cart_items where id = $1 and user_id = $2`, [cartItemId, user.sub]);
    return { message: "Cart item deleted successfully" };
  }

  async clear(user: CurrentUserPayload) {
    this.assertUser(user);
    await this.dataSource.query(`delete from cart_items where user_id = $1`, [user.sub]);
    return { message: "Cart cleared successfully" };
  }

  async one(user: CurrentUserPayload, cartItemId: number) {
    const rows = await this.dataSource.query(
      `select id, product_id as "productId", product_option_id as "productOptionId", quantity from cart_items where id = $1 and user_id = $2`,
      [cartItemId, user.sub],
    );
    const item = rows[0] as Record<string, unknown> | undefined;
    if (!item) {
      throw new AppException("CART_ITEM_NOT_FOUND", "Cart item not found", HttpStatus.NOT_FOUND);
    }
    return item;
  }

  private async ensureStock(productId: number, optionId: number | null, quantity: number): Promise<void> {
    const productRows = await this.dataSource.query(`select id, stock, status from products where id = $1 and status = 'ACTIVE'`, [productId]);
    const product = productRows[0] as { stock: number; status: string } | undefined;
    if (!product) {
      throw new AppException("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND);
    }
    if (optionId !== null) {
      const optionRows = await this.dataSource.query(`select stock from product_options where id = $1 and product_id = $2`, [optionId, productId]);
      const option = optionRows[0] as { stock: number } | undefined;
      if (!option) {
        throw new AppException("PRODUCT_OPTION_NOT_FOUND", "Product option not found", HttpStatus.NOT_FOUND);
      }
      if (quantity > Number(option.stock)) {
        throw new AppException("OUT_OF_STOCK", "Option stock is insufficient", HttpStatus.CONFLICT);
      }
    } else if (quantity > Number(product.stock)) {
      throw new AppException("OUT_OF_STOCK", "Product stock is insufficient", HttpStatus.CONFLICT);
    }
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
