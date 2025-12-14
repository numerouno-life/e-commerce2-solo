package com.ecommerce.model.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class UserRegistrationRequest {

    @NotBlank(message = "Ник пользователя не может быть пустым")
    @Size(min = 3, max = 50, message = "Длина ника пользователя не может быть меньше 3 и больше 50 символов")
    private String username;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, message = "Длина пароля не может быть меньше 8 символов")
    private String password;

    @Size(max = 100, message = "Длина имени не может быть больше 100 символов")
    private String firstName;

    @Size(max = 100, message = "Длина фамилии не может быть больше 100 символов")
    private String lastName;

    /*
    Пояснения какому паттерну должен соответствовать номер телефона не было,
    поэтому я сделал несколько вариантов:
    1) +7 (ХХХ) ХХХ-ХХ-ХХ в regexp - (^\+7\s\(\d{3}\)\s\d{3}-\d{2}-\d{2}$)
    2) 8XXXXXXXXXX - (^\+7\d{10}$)
    3) +7XXXXXXXXXX - (^8\d{10}$)
    4) ХХХХХХХХХХ - (\d{10}$)
    X - Любая цифра.
    Знак |(ИЛИ) - разделяет разные паттерны.
     */
    @Pattern(regexp = "(^\\+7\\s\\(\\d{3}\\)\\s\\d{3}-\\d{2}-\\d{2}$)|(^\\+7\\d{10}$)|(^8\\d{10}$)|(\\d{10}$)",
            message = "Номер телефона должен соответствовать паттерну +7 (ХХХ) ХХХ-ХХ-ХХ или быть записан без дополнительных символов и пробелов")
    private String phoneNumber;
}