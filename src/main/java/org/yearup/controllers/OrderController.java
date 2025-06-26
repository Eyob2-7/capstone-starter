package org.yearup.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yearup.data.*;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController                               // Marks this class as a REST controller
@CrossOrigin                                  // Enables Cross-Origin requests (for frontend/backend communication)
@PreAuthorize("hasRole('USER')")             // Only authenticated users with role USER can access this controller
@RequestMapping("/order")                     // Base URL mapping for all endpoints in this controller

public class OrderController {

    // === Dependencies ===
    private final OrderDao orderDao;                  // Handles DB operations for orders
    private final OrderLineItemDao orderLineItemDao;  // Handles DB operations for order line items
    private final ShoppingCartDao shoppingCartDao;    // Handles access to the user's shopping cart
    private final UserDao userDao;                    // Used to retrieve user info based on username
    private final ProfileDao profileDao;              // Used to fetch user's saved address info

    // === Constructor Injection ===
    public OrderController(OrderDao orderDao,
                           OrderLineItemDao orderLineItemDao,
                           ShoppingCartDao shoppingCartDao,
                           UserDao userDao,
                           ProfileDao profileDao) {

        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    // === Endpoint: POST /order ===
    // This method handles the checkout process for the logged-in user.
    @PostMapping
    public Order checkout(Principal principal) {
        try {

            // Create a new Order instance to populate and save
            Order order = new Order();

            // Step 1: Identify the user making the request
            String userName = principal.getName();             // Extract logged-in username from JWT token

            User user = userDao.getByUserName(userName);       // Fetch the User from the database
            int userId = user.getId();                          // Get the user's ID

            // Step 2: Retrieve user's shopping cart and profile (address info)
            ShoppingCart cart = shoppingCartDao.getByUserId(userId); // Fetch the user's shopping cart
            Profile profile = profileDao.getByUserId(userId);        // Fetch the user's saved address info

            // Step 3: Set order details
            order.setUserId(userId);                                // Associate order with the user
            order.setCreatedAt(LocalDate.now());                    // Set the current date as order date

            order.setShippingAmount(BigDecimal.valueOf(20));        // Fixed shipping cost
            order.setTotalAmount(cart.getTotal().add(order.getShippingAmount())); // Total = cart total + shipping

            // Step 4: Add shipping address info from the user profile
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());

            // Step 5: Convert shopping cart items into order line items
            List<OrderLineItem> orderLineItems = new ArrayList<>();

            for (ShoppingCartItem cartItem : cart.getItems().values()) {

                OrderLineItem lineItem = new OrderLineItem();

                lineItem.setProduct(cartItem.getProduct());                          // Product being ordered
                lineItem.setQuantity(cartItem.getQuantity());                        // Quantity ordered
                lineItem.setSalesPrice(cartItem.getProduct().getPrice());            // Price per unit
                lineItem.setDiscount(cartItem.getDiscountPercent().doubleValue());   // Discount (if any)
                orderLineItems.add(lineItem);                                        // Add to order's list

            }

            // Attach line items to the order
            order.setItems(orderLineItems);

            // Step 6: Save the order to the DB
            Order createdOrder = orderDao.create(order);  // Saves and returns order with new ID

            // Step 7: Save each line item with the orderId
            for (OrderLineItem item : order.getItems()) {
                item.setOrderId(createdOrder.getOrderId());     // Assign orderId to each item

                orderLineItemDao.create(item);                  // Save item to DB

            }

            // Step 8: Clear the user's shopping cart after successful checkout
            shoppingCartDao.clearCart(userId);

            // Step 9: Return the created order to the client
            return createdOrder;

        } catch (Exception e) {
            throw new RuntimeException("Error during checkout: " + e.getMessage(), e);

        }

    }

}
