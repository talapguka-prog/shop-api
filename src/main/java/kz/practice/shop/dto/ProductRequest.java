package kz.practice.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record ProductRequest(
        @NotBlank(message = "Тауар атауы бос болмауы керек")
        @Size(max = 200, message = "Атау 200 символдан аспауы керек")
        String name,

        @Size(max = 1000, message = "Сипаттама 1000 символдан аспауы керек")
        String description,

        @NotNull(message = "Бағасы көрсетілуі керек")
        @Positive(message = "Баға нөлден үлкен болуы керек")
        BigDecimal price,

        @NotNull(message = "categoryId көрсетілуі керек")
        Long categoryId,

        Set<Long> supplierIds
) {}
