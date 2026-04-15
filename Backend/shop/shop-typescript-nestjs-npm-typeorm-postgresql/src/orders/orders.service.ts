import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { pageMeta } from "../common/api-response";
import { AppException } from "../common/app.exception";
import { CurrentUserPayload } from "../common/current-user.decorator";
import { ensureString, readPage } from "../common/sql";

@Injectable()
export class OrdersService {
  constructor(private readonly dataSource: DataSource) {}

  async create(user: CurrentUserPayload, body: Record<string, unknown>) {
    this.assertUser(user);
    const cartItemIds = Array.isArray(body.cartItemIds) ? body.cartItemIds.map((value) => Number(value)) : [];
    if (cartItemIds.length === 0) {
      throw new AppException("VALIDATION_ERROR", "cartItemIds is required", HttpStatus.BAD_REQUEST);
    }
    const addressId = Number(body.addressId);
    if (!Number.isFinite(addressId)) {
      throw new AppException("VALIDATION_ERROR", "addressId is required", HttpStatus.BAD_REQUEST);
    }
    const addressRows = await this.dataSource.query(
      `select id, recipient_name as "recipientName", phone, zip_code as "zipCode", address1, address2
       from addresses where id = $1 and user_id = $2`,
      [addressId, user.sub],
    );
    const address = addressRows[0] as Record<string, unknown> | undefined;
    if (!address) {
      throw new AppException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND);
    }

    const items = await this.dataSource.query(
      `select c.id, c.product_id as "productId", c.product_option_id as "productOptionId", c.quantity,
              p.name as "productName", p.price, p.stock,
              po.name as "optionName", po.value as "optionValue", po.additional_price as "additionalPrice", po.stock as "optionStock"
       from cart_items c
       join products p on p.id = c.product_id
       left join product_options po on po.id = c.product_option_id
       where c.user_id = $1 and c.id = any($2::bigint[])`,
      [user.sub, cartItemIds],
    );
    if (items.length !== cartItemIds.length) {
      throw new AppException("CART_ITEM_NOT_FOUND", "Some cart items were not found", HttpStatus.NOT_FOUND);
    }

    let totalAmount = 0;
    for (const item of items as Array<Record<string, unknown>>) {
      const available = item.productOptionId ? Number(item.optionStock ?? 0) : Number(item.stock ?? 0);
      if (Number(item.quantity) > available) {
        throw new AppException("OUT_OF_STOCK", "Stock is insufficient", HttpStatus.CONFLICT);
      }
      const unitPrice = Number(item.price) + Number(item.additionalPrice ?? 0);
      totalAmount += unitPrice * Number(item.quantity);
    }

    const orderNumber = `ORD-${new Date().toISOString().slice(0, 10).replaceAll("-", "")}-${Date.now()}`;
    const orderInsert = await this.dataSource.query(
      `insert into orders (user_id, order_number, order_status, total_amount, payment_status, ordered_at)
       values ($1, $2, 'PENDING', $3, 'READY', now())
       returning id`,
      [user.sub, orderNumber, totalAmount],
    );
    const orderId = Number((orderInsert[0] as { id: number }).id);

    await this.dataSource.query(
      `insert into order_addresses (order_id, recipient_name, phone, zip_code, address1, address2)
       values ($1, $2, $3, $4, $5, $6)`,
      [orderId, address.recipientName, address.phone, address.zipCode, address.address1, address.address2 ?? null],
    );

    for (const item of items as Array<Record<string, unknown>>) {
      const unitPrice = Number(item.price) + Number(item.additionalPrice ?? 0);
      const optionSnapshot = item.productOptionId ? `${String(item.optionName)}:${String(item.optionValue)}` : null;
      await this.dataSource.query(
        `insert into order_items (order_id, product_id, product_option_id, product_name_snapshot, option_name_snapshot, unit_price, quantity, line_amount)
         values ($1, $2, $3, $4, $5, $6, $7, $8)`,
        [
          orderId,
          item.productId,
          item.productOptionId ?? null,
          item.productName,
          optionSnapshot,
          unitPrice,
          item.quantity,
          unitPrice * Number(item.quantity),
        ],
      );
      if (item.productOptionId) {
        await this.dataSource.query(`update product_options set stock = stock - $2, updated_at = now() where id = $1`, [item.productOptionId, item.quantity]);
      }
      await this.dataSource.query(`update products set stock = stock - $2, updated_at = now() where id = $1`, [item.productId, item.quantity]);
    }

    await this.dataSource.query(`delete from cart_items where user_id = $1 and id = any($2::bigint[])`, [user.sub, cartItemIds]);
    return this.one(orderId, user);
  }

  async list(user: CurrentUserPayload, query: Record<string, string | undefined>) {
    this.assertUser(user);
    const { page, limit, offset } = readPage(query.page, query.limit);
    const values: unknown[] = [user.sub];
    const filters: string[] = [`o.user_id = $1`];
    if (query.status) {
      values.push(query.status.toUpperCase());
      filters.push(`o.order_status = $${values.length}`);
    }
    const where = `where ${filters.join(" and ")}`;
    const countRows = await this.dataSource.query(`select count(*)::int as count from orders o ${where}`, values);
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    values.push(limit, offset);
    const rows = await this.dataSource.query(
      `select id, order_number as "orderNumber", order_status as "orderStatus", payment_status as "paymentStatus",
              total_amount as "totalAmount", ordered_at as "orderedAt", cancelled_at as "cancelledAt"
       from orders o ${where}
       order by o.ordered_at desc
       limit $${values.length - 1} offset $${values.length}`,
      values,
    );
    return { items: rows as Record<string, unknown>[], meta: pageMeta(page, limit, totalCount) };
  }

  async one(orderId: number, user?: CurrentUserPayload) {
    const values: unknown[] = [orderId];
    let where = `where o.id = $1`;
    if (user && user.subjectType === "user") {
      values.push(user.sub);
      where += ` and o.user_id = $2`;
    }
    const rows = await this.dataSource.query(
      `select o.id, o.order_number as "orderNumber", o.order_status as "orderStatus", o.payment_status as "paymentStatus",
              o.total_amount as "totalAmount", o.ordered_at as "orderedAt", o.cancelled_at as "cancelledAt"
       from orders o ${where}`,
      values,
    );
    const order = rows[0] as Record<string, unknown> | undefined;
    if (!order) {
      throw new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND);
    }
    const [orderAddressRows, orderItemsRows, paymentRows] = await Promise.all([
      this.dataSource.query(`select recipient_name as "recipientName", phone, zip_code as "zipCode", address1, address2 from order_addresses where order_id = $1`, [orderId]),
      this.dataSource.query(
        `select id, product_id as "productId", product_option_id as "productOptionId", product_name_snapshot as "productNameSnapshot",
                option_name_snapshot as "optionNameSnapshot", unit_price as "unitPrice", quantity, line_amount as "lineAmount"
         from order_items where order_id = $1 order by id asc`,
        [orderId],
      ),
      this.dataSource.query(
        `select id, order_id as "orderId", payment_method as "paymentMethod", payment_status as "paymentStatus",
                paid_amount as "paidAmount", paid_at as "paidAt", refunded_at as "refundedAt"
         from payments where order_id = $1`,
        [orderId],
      ),
    ]);
    return {
      order,
      orderAddress: (orderAddressRows[0] as Record<string, unknown>) ?? null,
      orderItems: orderItemsRows as Record<string, unknown>[],
      payment: (paymentRows[0] as Record<string, unknown>) ?? null,
    };
  }

  async cancel(user: CurrentUserPayload, orderId: number) {
    this.assertUser(user);
    const detail = await this.one(orderId, user);
    const order = detail.order as Record<string, unknown>;
    if (["SHIPPING", "DELIVERED", "CANCELLED"].includes(String(order.orderStatus))) {
      throw new AppException("PAYMENT_NOT_ALLOWED", "Order cannot be cancelled", HttpStatus.CONFLICT);
    }
    await this.dataSource.query(`update orders set order_status = 'CANCELLED', cancelled_at = now(), updated_at = now() where id = $1`, [orderId]);
    return { id: orderId, orderStatus: "CANCELLED", cancelledAt: new Date().toISOString() };
  }

  async adminList(query: Record<string, string | undefined>) {
    const { page, limit, offset } = readPage(query.page, query.limit);
    const values: unknown[] = [];
    const filters: string[] = [`1=1`];
    if (query.search) {
      values.push(`%${query.search.trim()}%`);
      filters.push(`(o.order_number ilike $${values.length} or u.name ilike $${values.length})`);
    }
    if (query.status) {
      values.push(query.status.toUpperCase());
      filters.push(`o.order_status = $${values.length}`);
    }
    const where = `where ${filters.join(" and ")}`;
    const countRows = await this.dataSource.query(`select count(*)::int as count from orders o join users u on u.id = o.user_id ${where}`, values);
    const totalCount = Number((countRows[0] as { count: number }).count ?? 0);
    values.push(limit, offset);
    const rows = await this.dataSource.query(
      `select o.id, o.order_number as "orderNumber", o.order_status as "orderStatus", o.payment_status as "paymentStatus",
              o.total_amount as "totalAmount", o.ordered_at as "orderedAt",
              json_build_object('id', u.id, 'name', u.name) as customer
       from orders o
       join users u on u.id = o.user_id
       ${where}
       order by o.ordered_at desc
       limit $${values.length - 1} offset $${values.length}`,
      values,
    );
    return { items: rows as Record<string, unknown>[], meta: pageMeta(page, limit, totalCount) };
  }

  async adminSetStatus(orderId: number, orderStatus: string) {
    const upper = orderStatus.toUpperCase();
    if (!["PENDING", "PAID", "PREPARING", "SHIPPING", "DELIVERED", "CANCELLED"].includes(upper)) {
      throw new AppException("INVALID_STATUS", "Invalid order status", HttpStatus.BAD_REQUEST);
    }
    await this.one(orderId);
    const rows = await this.dataSource.query(
      `update orders set order_status = $2, updated_at = now() where id = $1
       returning id, order_status as "orderStatus", updated_at as "updatedAt"`,
      [orderId, upper],
    );
    return rows[0] as Record<string, unknown>;
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
