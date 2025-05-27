package com.example.techgear;

import junit.framework.TestCase;
public class ProductTest extends TestCase {
    private Product product;

    @Override
    protected void setUp() {
        // Initialize a test product before each test
        product = new Product("Test Product", 99.99, 10);
    }

    public void testDefaultConstructor() {
        Product emptyProduct = new Product();
        assertNull(emptyProduct.getId());
        assertNull(emptyProduct.getName());
        assertEquals(0.0, emptyProduct.getPrice());
        assertEquals(0, emptyProduct.getStock());
    }

    public void testParameterizedConstructor() {
        assertEquals("Test Product", product.getName());
        assertEquals(99.99, product.getPrice());
        assertEquals(10, product.getStock());
    }

    public void testSetAndGetId() {
        String testId = "test123";
        product.setId(testId);
        assertEquals(testId, product.getId());
    }

    public void testSetAndGetName() {
        String newName = "Updated Product";
        product.setName(newName);
        assertEquals(newName, product.getName());
    }

    public void testSetAndGetPrice() {
        double newPrice = 149.99;
        product.setPrice(newPrice);
        assertEquals(newPrice, product.getPrice());
    }

    public void testSetAndGetStock() {
        int newStock = 20;
        product.setStock(newStock);
        assertEquals(newStock, product.getStock());
    }
} 