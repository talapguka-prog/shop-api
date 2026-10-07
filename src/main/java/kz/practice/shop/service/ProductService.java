package kz.practice.shop.service;

import jakarta.persistence.criteria.Predicate;
import kz.practice.shop.dto.ProductRequest;
import kz.practice.shop.dto.ProductResponse;
import kz.practice.shop.entity.Category;
import kz.practice.shop.entity.Product;
import kz.practice.shop.entity.Supplier;
import kz.practice.shop.exception.ResourceNotFoundException;
import kz.practice.shop.repository.CategoryRepository;
import kz.practice.shop.repository.ProductRepository;
import kz.practice.shop.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    public Page<ProductResponse> findAll(String name, Long categoryId,
                                         BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findAll(buildSpec(name, categoryId, minPrice, maxPrice), pageable)
                .map(this::toResponse);
    }

    public ProductResponse findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Тауар табылмады: id=" + id);
        }
        productRepository.deleteById(id);
    }

    // ---------- көмекші әдістер
    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Тауар табылмады: id=" + id));
    }

    private void apply(Product product, ProductRequest r) {
        Category category = categoryRepository.findById(r.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Санат табылмады: id=" + r.categoryId()));

        Set<Supplier> suppliers = new HashSet<>();
        if (r.supplierIds() != null && !r.supplierIds().isEmpty()) {
            suppliers.addAll(supplierRepository.findAllById(r.supplierIds()));
            if (suppliers.size() != r.supplierIds().size()) {
                throw new ResourceNotFoundException("Жеткізушілердің бірі табылмады: " + r.supplierIds());
            }
        }
        product.setName(r.name().trim());
        product.setDescription(r.description());
        product.setPrice(r.price());
        product.setCategory(category);
        product.setSuppliers(suppliers);
    }

    private Specification<Product> buildSpec(String name, Long categoryId, BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                ps.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (min != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), min));
            }
            if (max != null) {
                ps.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), max));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private ProductResponse toResponse(Product p) {
        List<String> supplierNames = p.getSuppliers().stream().map(Supplier::getName).sorted().toList();
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCreatedAt(),
                p.getCategory().getId(), p.getCategory().getName(), supplierNames);
    }
}
