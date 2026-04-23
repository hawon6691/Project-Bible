import { HttpStatus, Injectable } from "@nestjs/common";
import { DataSource } from "typeorm";
import { AppException } from "../../common/app.exception";
import { CurrentUserPayload } from "../../common/current-user.decorator";
import { ensureString } from "../../common/sql";

@Injectable()
export class PaymentsService {
  constructor(private readonly dataSource: DataSource) {}

  async create(user: CurrentUserPayload, body: Record<string, unknown>) {
    this.assertUser(user);
    const orderId = Number(body.orderId);
    if (!Number.isFinite(orderId)) {
      throw new AppException("VALIDATION_ERROR", "orderId is required", HttpStatus.BAD_REQUEST);
    }
    const paymentMethod = ensureString(body.paymentMethod, "paymentMethod");
    const orderRows = await this.dataSource.query(
      `select id, total_amount as totalAmount, payment_status as paymentStatus, order_status as orderStatus
       from orders where id = ? and user_id = ?`,
      [orderId, user.sub],
    );
    const order = orderRows[0] as Record<string, unknown> | undefined;
    if (!order) {
      throw new AppException("ORDER_NOT_FOUND", "Order not found", HttpStatus.NOT_FOUND);
    }
    if (String(order.paymentStatus) !== "READY") {
      throw new AppException("PAYMENT_NOT_ALLOWED", "Payment is not allowed", HttpStatus.CONFLICT);
    }
    const existing = await this.dataSource.query(`select id from payments where order_id = ?`, [orderId]);
    const rows =
      existing.length === 0
        ? await this.dataSource.query(
            `insert into payments (order_id, payment_method, payment_status, paid_amount, paid_at)
             values (?, ?, 'PAID', ?, now())`,
            [orderId, paymentMethod, order.totalAmount],
          )
        : await this.dataSource.query(
            `update payments
             set payment_method = ?, payment_status = 'PAID', paid_amount = ?, paid_at = now(), updated_at = now()
             where order_id = ?`,
            [paymentMethod, order.totalAmount, orderId],
          );
    const paymentId = existing.length === 0 ? Number((rows as { insertId: number }).insertId) : Number((existing[0] as { id: number }).id);
    await this.dataSource.query(`update orders set payment_status = 'PAID', order_status = 'PAID', updated_at = now() where id = ?`, [orderId]);
    return this.one(user, paymentId);
  }

  async one(user: CurrentUserPayload, paymentId: number) {
    this.assertUser(user);
    const rows = await this.dataSource.query(
      `select p.id, p.order_id as orderId, p.payment_method as paymentMethod, p.payment_status as paymentStatus,
              p.paid_amount as paidAmount, p.paid_at as paidAt, p.refunded_at as refundedAt
       from payments p
       join orders o on o.id = p.order_id
       where p.id = ? and o.user_id = ?`,
      [paymentId, user.sub],
    );
    const payment = rows[0] as Record<string, unknown> | undefined;
    if (!payment) {
      throw new AppException("PAYMENT_NOT_FOUND", "Payment not found", HttpStatus.NOT_FOUND);
    }
    return payment;
  }

  async refund(user: CurrentUserPayload, paymentId: number) {
    const payment = await this.one(user, paymentId);
    if (String(payment.paymentStatus) !== "PAID") {
      throw new AppException("PAYMENT_NOT_ALLOWED", "Refund is not allowed", HttpStatus.CONFLICT);
    }
    await this.dataSource.query(
      `update payments
       set payment_status = 'REFUNDED', refunded_at = now(), updated_at = now()
       where id = ?`,
      [paymentId],
    );
    await this.dataSource.query(`update orders set payment_status = 'REFUNDED', order_status = 'CANCELLED', cancelled_at = now(), updated_at = now() where id = ?`, [payment.orderId]);
    const updated = await this.one(user, paymentId);
    return { id: updated.id, paymentStatus: updated.paymentStatus, refundedAt: updated.refundedAt };
  }

  private assertUser(user: CurrentUserPayload): void {
    if (user.subjectType !== "user") {
      throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
    }
  }
}
