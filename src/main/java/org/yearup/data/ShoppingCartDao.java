package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao {

    // Get the shopping cart for the given user
    ShoppingCart getByUserId(int userId);

    // add additional method signatures here

    // Post - Add a product to the cart
    void addProduct(int userId, int productId);

    // Put - Update the quantity of a product already in the cart
    void updateProductQuantity(int userId, int productId, int quantity);

    // Delete - Clear all items from the current user's cart
    void clearCart(int userId);

}
