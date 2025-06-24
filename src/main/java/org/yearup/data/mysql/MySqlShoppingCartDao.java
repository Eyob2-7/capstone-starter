package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    // Get the full shopping cart for a given user
    @Override
    public ShoppingCart getByUserId(int userId) {
        // SQL to join cart_items and products table
        String sql = """
                SELECT
                sc.product_id, sc.quantity
                ,p.product_id
                ,p.name
                ,p.price
                ,p.category_id
                ,p.description
                ,p.color
                ,p.stock
                ,p.image_url
                ,p.featured
                FROM
                shopping_cart sc
                JOIN products p ON sc.product_id = p.product_id
                WHERE sc.user_id = ?
                """;

        Map<Integer, ShoppingCartItem> items = new HashMap<>();

        BigDecimal total = BigDecimal.ZERO;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId); // Set user ID in query

            ResultSet rs = stmt.executeQuery(); // Execute query

            while (rs.next()) {
                // Create Product object from result row
                Product product = mapRowToProduct(rs);

                // Get quantity and calculate line total
                int quantity = rs.getInt("quantity");

                BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

                // Create ShoppingCartItem
                ShoppingCartItem item = new ShoppingCartItem();

                item.setProduct(product);
                item.setQuantity(quantity);


                // Add to cart map and update total
                items.put(product.getProductId(), item);
                total = total.add(lineTotal);
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }

        // Build and return ShoppingCart
        ShoppingCart cart = new ShoppingCart();

        cart.setItems(items);

        return cart;
    }

    // Add a product to the cart: insert if new, increment if it already exists
    @Override
    public void addProduct(int userId, int productId) {

        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection()) {
            // Check if the item is already in the cart
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {

                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, productId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Item already exists → increment quantity
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {

                        updateStmt.setInt(1, userId);
                        updateStmt.setInt(2, productId);
                        updateStmt.executeUpdate();

                    }

                } else {

                    // Item doesn't exist → insert with quantity 1
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, productId);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update the quantity of a specific product in the cart
    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) {

        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, productId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Clear all items in the cart for the given user
    @Override
    public void clearCart(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Map a ResultSet row to a Product object
    private Product mapRowToProduct(ResultSet rs) throws SQLException {

        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setDescription(rs.getString("description"));
        product.setColor(rs.getString("color"));
        product.setStock(rs.getInt("stock"));
        product.setImageUrl(rs.getString("image_url"));
        product.setFeatured(rs.getBoolean("featured"));

        return product;
    }
}
