package kz.practice.shop.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.practice.shop.dto.NameRequest;
import kz.practice.shop.entity.Category;
import kz.practice.shop.entity.Supplier;
import kz.practice.shop.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Catalog", description = "Санаттар мен жеткізушілер")
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return service.categories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody NameRequest request) {
        return service.createCategory(request);
    }

    @GetMapping("/suppliers")
    public List<Supplier> suppliers() {
        return service.suppliers();
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public Supplier createSupplier(@Valid @RequestBody NameRequest request) {
        return service.createSupplier(request);
    }
}
