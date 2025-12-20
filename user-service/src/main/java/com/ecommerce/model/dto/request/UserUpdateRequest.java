package com.ecommerce.model.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 100, message = "Длина имени не может быть больше 100 символов")
    private String firstName;

    @Size(max = 100, message = "Длина фамилии не может быть больше 100 символов")
    private String lastName;

    @Pattern(regexp = "(^\\+7\\s\\(\\d{3}\\)\\s\\d{3}-\\d{2}-\\d{2}$)|(^\\+7\\d{10}$)|(^8\\d{10}$)|(\\d{10}$)",
            message = "Номер телефона должен соответствовать паттерну +7 (ХХХ) ХХХ-ХХ-ХХ или быть записан без дополнительных символов и пробелов")
    private String phoneNumber;
}
