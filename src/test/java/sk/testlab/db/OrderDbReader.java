package sk.testlab.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only JDBC observer. Uses parameterized SELECT only — never INSERT/UPDATE/DELETE.
 * Look up orders by the UUID from GUI, never by "latest created_at".
 */
public final class OrderDbReader implements AutoCloseable {
    private final Connection connection;

    public OrderDbReader(DbConfig config) throws SQLException {
        connection = DriverManager.getConnection(config.url, config.user, config.password);

        // Driver-side hint: this Connection should refuse writes from our code.
        // Real protection is the MariaDB account (GRANT SELECT only) — see
        // database/create-readonly-user.sql. We intentionally do NOT parse SHOW GRANTS:
        // grant text differs by server version/host, is easy to get wrong, and still
        // cannot prove the server would reject an UPDATE. Prefer a dedicated reader
        // user over a brittle privilege regex.
        connection.setReadOnly(true);
        connection.setAutoCommit(true);
        if (!connection.isReadOnly()) {
            throw new SQLException(
                    "JDBC connection is not read-only after setReadOnly(true). "
                            + "Use a SELECT-only account (testlab_reader), not the app user.");
        }
    }

    public long workspaceId(String workspaceCode) throws SQLException {
        String sql = "SELECT id FROM workspaces WHERE code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, workspaceCode);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Workspace not found: " + workspaceCode);
                }
                long id = result.getLong("id");
                if (result.next()) {
                    throw new SQLException("Workspace code is not unique: " + workspaceCode);
                }
                return id;
            }
        }
    }

    public int stock(long workspaceId, String sku) throws SQLException {
        String sql = """
            SELECT stock FROM products
            WHERE workspace_id = ? AND sku = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, workspaceId);
            statement.setString(2, sku);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Product " + sku + " missing in workspace " + workspaceId);
                }
                int stock = result.getInt("stock");
                if (result.next()) {
                    throw new SQLException("Duplicate SKU " + sku + " in workspace " + workspaceId);
                }
                return stock;
            }
        }
    }

    public OrderRecord findOrder(String orderId) throws SQLException {
        String sql = """
            SELECT id, workspace_id, status, subtotal_cents, discount_cents, shipping_cents,
                   total_cents, coupon_code, delivery_method, created_at, cancelled_at
            FROM orders
            WHERE id = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Order UUID not in database: " + orderId);
                }
                OrderRecord order = mapOrder(result);
                if (result.next()) {
                    throw new SQLException("Order primary key is not unique: " + orderId);
                }
                return order;
            }
        }
    }

    public List<OrderItemRecord> findItems(String orderId) throws SQLException {
        String sql = """
            SELECT order_id, sku, unit_price_cents, quantity, line_total_cents
            FROM order_items
            WHERE order_id = ?
            ORDER BY sku
            """;
        List<OrderItemRecord> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    items.add(new OrderItemRecord(
                            result.getString("order_id"),
                            result.getString("sku"),
                            result.getInt("unit_price_cents"),
                            result.getInt("quantity"),
                            result.getInt("line_total_cents")
                    ));
                }
            }
        }
        return List.copyOf(items);
    }

    private static OrderRecord mapOrder(ResultSet result) throws SQLException {
        return new OrderRecord(
                result.getString("id"),
                result.getLong("workspace_id"),
                result.getString("status"),
                result.getInt("subtotal_cents"),
                result.getInt("discount_cents"),
                result.getInt("shipping_cents"),
                result.getInt("total_cents"),
                result.getString("coupon_code"),
                result.getString("delivery_method"),
                toLocalDateTime(result.getTimestamp("created_at")),
                toLocalDateTime(result.getTimestamp("cancelled_at"))
        );
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
