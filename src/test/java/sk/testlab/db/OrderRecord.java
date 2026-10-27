package sk.testlab.db;

import java.time.LocalDateTime;

/** One row from {@code orders} — fields the demo / homework assert. */
public record OrderRecord(
        String id,
        long workspaceId,
        String status,
        int subtotalCents,
        int discountCents,
        int shippingCents,
        int totalCents,
        String couponCode,
        String deliveryMethod,
        LocalDateTime createdAt,
        LocalDateTime cancelledAt) {
}

/** One row from {@code order_items}. */
record OrderItemRecord(
        String orderId,
        String sku,
        int unitPriceCents,
        int quantity,
        int lineTotalCents) {
}
