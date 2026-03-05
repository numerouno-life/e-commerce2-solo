package com.ecommerce.exception;

public class CategoryHasProductsException extends RuntimeException{
    public CategoryHasProductsException(String message) {
        super(message);
    }
}
