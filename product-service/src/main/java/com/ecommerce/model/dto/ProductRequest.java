package com.ecommerce.model.dto;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {

    @NotBlank(message = "Название товара не может быть пустым")
    @Size(max = 52, message = "Название товара не может превышать 52 символов")
    String name;

    @Size(min = 1, max = 322, message = "Описание должно быть от 1 до 322 символов")
    String description;

    @NotNull
    @DecimalMin(value = "0.1", message = "Цена должна быть больше 0.1")
    BigDecimal price;

    @NotNull
    Long categoryId;

    @Size(min = 1, max = 35, message = "Название бренда товара должно быть от 1 до 35 символов")
    String brand;

    @PositiveOrZero(message = "Количество должно быть больше или равно 0")
    Integer stockQuantity;

    @URL(message = "URL изображения должен быть валидным")
    String imageUrl;
}
