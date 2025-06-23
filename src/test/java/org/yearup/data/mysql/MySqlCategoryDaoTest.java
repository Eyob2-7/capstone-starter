package org.yearup.data.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yearup.models.Category;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MySqlCategoryDaoTest extends BaseDaoTestClass {

    private MySqlCategoryDao dao;

    @BeforeEach
    void setup() {
        dao = new MySqlCategoryDao(dataSource);
    }

    @Test
    void getAllCategories_shouldReturnListOfCategories() {

        // Act
        List<Category> categories = dao.getAllCategories();

        // Assert
        assertNotNull(categories, "The list of categories should not be null");
        assertFalse(categories.isEmpty(), "The list should contain at least one category");
    }

    @Test
    void getById_shouldReturnCorrectCategory() {
        // Arrange
        int testId = 1;

        // Act
        Category category = dao.getById(testId);

        // Assert
        assertNotNull(category, "Returned category should not be null");
        assertEquals(testId, category.getCategoryId(), "Category ID should match the requested ID");
        assertNotNull(category.getName(), "Category name should not be null");
    }
}