package kz.practice.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        LocalDateTime createdAt,
        Long categoryId,
        String categoryName,
        List<String> suppliers
) {}
