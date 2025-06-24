package org.yearup.data.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yearup.models.ShoppingCart;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MySqlShoppingCartDaoTest extends BaseDaoTestClass {


    private MySqlShoppingCartDao dao;

    @BeforeEach
    void setup() {
        dao = new MySqlShoppingCartDao(dataSource);
    }


    @Test
    void getByUserId_shouldReturnCartWithItems() {
        // Arrange : Set up test user and product IDs
        int userId = 1;
        dao.addProduct(userId, 1);

        // Act: Add product to cart
        ShoppingCart cart = dao.getByUserId(userId);

        // Assert: the cart should contain the added product with quantity 1
        assertNotNull(cart, "Cart should not be null");
        assertFalse(cart.getItems().isEmpty(), "Cart should have at least one item");
    }

    @Test
    void addProduct_shouldAddNewProductToCart() {
        // Arrange
        int userId = 1;
        int productId = 2;
        // Act
        dao.addProduct(userId, productId);
        ShoppingCart cart = dao.getByUserId(userId);

        // Assert
        assertNotNull(cart, "Cart should not be null");
        assertFalse(cart.getItems().isEmpty(), "Cart should have at least one item");
        assertTrue(cart.contains(productId), "Cart should contain the added product");
        assertEquals(1, cart.getItems().get(productId).getQuantity(), "Quantity should be 1 after first add");
    }

    @Test
    void updateProductQuantity_shouldChangeQuantity() {

        // Arrange
        int userId = 1;
        int productId = 3;
        dao.addProduct(userId, productId);

        // Act
        dao.updateProductQuantity(userId, productId, 5);
        ShoppingCart cart = dao.getByUserId(userId);
        int quantity = cart.getItems().get(productId).getQuantity();

        // Assert
        assertEquals(5, quantity, "Quantity should be updated to 5");
    }

    @Test
    void clearCart_shouldRemoveAllItems() {

        // Arrange: Add multiple products to the cart
        int userId = 1;
        dao.addProduct(userId, 1);
        dao.addProduct(userId, 2);

        // Act: clear the cart
        dao.clearCart(userId);
        ShoppingCart cart = dao.getByUserId(userId);

        // Assert: cart should be empty
        assertTrue(cart.getItems().isEmpty(), "Cart should be empty after clearing");
        assertEquals(BigDecimal.ZERO, cart.getTotal(), "Total should be zero after clearing");
    }
}