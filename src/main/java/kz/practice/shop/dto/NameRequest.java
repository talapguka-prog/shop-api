package kz.practice.shop.dto;

import jakarta.validation.constraints.NotBlank;

public record NameRequest(@NotBlank(message = "Атау бос болмауы керек") String name, String phone) {}
