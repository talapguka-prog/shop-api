package kz.practice.shop.service;

import kz.practice.shop.dto.NameRequest;
import kz.practice.shop.entity.Category;
import kz.practice.shop.entity.Supplier;
import kz.practice.shop.repository.CategoryRepository;
import kz.practice.shop.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Санаттар мен жеткізушілер бойынша қарапайым қызметтер. */
@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public CatalogService(CategoryRepository categoryRepository, SupplierRepository supplierRepository) {
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<Category> categories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category createCategory(NameRequest r) {
        return categoryRepository.save(new Category(r.name().trim()));
    }

    public List<Supplier> suppliers() {
        return supplierRepository.findAll();
    }

    @Transactional
    public Supplier createSupplier(NameRequest r) {
        return supplierRepository.save(new Supplier(r.name().trim(), r.phone()));
    }
}
