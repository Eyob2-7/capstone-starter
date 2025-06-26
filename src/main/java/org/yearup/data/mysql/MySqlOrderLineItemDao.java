package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderLineItemDao;
import org.yearup.models.OrderLineItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlOrderLineItemDao extends MySqlDaoBase implements OrderLineItemDao {
    public MySqlOrderLineItemDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void create(OrderLineItem orderLineItem) {
        String sql = """
                INSERT INTO order_line_items
                (order_id, product_id
                ,sales_price
                ,quantity
                ,discount)
                VALUES
                (?, ?, ?, ?, ?)
                """;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderLineItem.getOrderId());
            ps.setInt(2, orderLineItem.getProduct().getProductId());
            ps.setBigDecimal(3, orderLineItem.getSalesPrice());
            ps.setInt(4, orderLineItem.getQuantity());
            ps.setDouble(5, orderLineItem.getDiscount());

            ps.executeUpdate();
            //Fetch and set thr generated id
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    orderLineItem.setOrderLineItemId(generatedId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting order line item", e);
        }
    }
}
