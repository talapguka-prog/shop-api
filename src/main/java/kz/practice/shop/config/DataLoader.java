package kz.practice.shop.config;

import kz.practice.shop.dto.ProductRequest;
import kz.practice.shop.entity.Category;
import kz.practice.shop.entity.Supplier;
import kz.practice.shop.repository.CategoryRepository;
import kz.practice.shop.repository.SupplierRepository;
import kz.practice.shop.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/** Іске қосқанда бастапқы тест деректерін жүктейді. */
@Component
public class DataLoader implements CommandLineRunner {
    private final CategoryRepository categories;
    private final SupplierRepository suppliers;
    private final ProductService products;

    public DataLoader(CategoryRepository categories, SupplierRepository suppliers, ProductService products) {
        this.categories = categories;
        this.suppliers = suppliers;
        this.products = products;
    }

    @Override
    public void run(String... args) {
        if (categories.count() > 0) return;
        Category electronics = categories.save(new Category("Электроника"));
        categories.save(new Category("Киім"));
        categories.save(new Category("Кітаптар"));
        Supplier s1 = suppliers.save(new Supplier("Alma Trade", "+7 701 000 00 01"));
        Supplier s2 = suppliers.save(new Supplier("Steppe Supply", "+7 701 000 00 02"));

        products.create(new ProductRequest("Phone X", "Смартфон 128 ГБ", new BigDecimal("250000"),
                electronics.getId(), Set.of(s1.getId())));
        products.create(new ProductRequest("Laptop Pro", "Ноутбук 16 ГБ RAM", new BigDecimal("650000"),
                electronics.getId(), Set.of(s1.getId(), s2.getId())));
    }
}
