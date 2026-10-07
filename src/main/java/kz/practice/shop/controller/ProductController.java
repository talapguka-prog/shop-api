package kz.practice.shop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.practice.shop.dto.ProductRequest;
import kz.practice.shop.dto.ProductResponse;
import kz.practice.shop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Тауарлар бойынша CRUD, іздеу, сүзгі, пагинация, сұрыптау")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Тауарлар тізімі (пагинация, сұрыптау, сүзгі). Мысал: ?page=0&size=10&sort=price,desc&minPrice=100")
    @GetMapping
    public Page<ProductResponse> getAll(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice,
                                        @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return service.findAll(name, categoryId, minPrice, maxPrice, pageable);
    }

    @Operation(summary = "Атау бойынша іздеу: /api/products/search?name=phone")
    @GetMapping("/search")
    public Page<ProductResponse> search(@RequestParam String name,
                                        @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return service.findAll(name, null, null, null, pageable);
    }

    @Operation(summary = "Тауарды id бойынша алу")
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Жаңа тауар жасау (201 Created)")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Тауарды толық жаңарту")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Тауарды жою (204 No Content)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
