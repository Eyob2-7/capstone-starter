package org.yearup.data.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class MySqlProductDaoTest extends BaseDaoTestClass {
    private MySqlProductDao dao;

    @BeforeEach
    public void setup() {
        dao = new MySqlProductDao(dataSource);
    }

    @Test
    public void getById_shouldReturn_theCorrectProduct() {
        // arrange
        int productId = 1;
        Product expected = new Product() {{
            setProductId(1);
            setName("Smartphone");
            setPrice(new BigDecimal("499.99"));
            setCategoryId(1);
            setDescription("A powerful and feature-rich smartphone for all your communication needs.");
            setColor("Black");
            setStock(50);
            setFeatured(false);
            setImageUrl("smartphone.jpg");
        }};

        // act
        var actual = dao.getById(productId);

        // assert
        assertEquals(expected.getPrice(), actual.getPrice(), "Because I tried to get product 1 from the database.");
    }

    @Test
    public void search_shouldReturnProductWithinPriceRange() {
        //Arrange
        BigDecimal min = new BigDecimal("25");
        BigDecimal max = new BigDecimal("200");

        // Act
        List<Product> result = dao.search(null, min, max, null, null);

        // Assert
        assertFalse(result.isEmpty(), "Expected products within the price range 25 to 200");
        assertTrue(result.stream().allMatch(p -> p.getPrice().compareTo(min) >= 0 && p.getPrice().compareTo(max) <= 0),
                "All product prices should fall within range.");
    }

    @Test
    public void search_shouldReturnProductsMatchingSearchTerm() {
        // arrange
        String searchTerm = "phone";
        // act
        List<Product> result = dao.search(null, null, null, null, searchTerm);
        // assert
        assertFalse(result.isEmpty(), "Expected products matching 'phone'.");
        assertTrue(result.stream().anyMatch(p ->
                        p.getName().toLowerCase().contains("phone") ||
                                p.getDescription().toLowerCase().contains("phone")),
                "Expected product name or description to contain 'phone'");
    }
}