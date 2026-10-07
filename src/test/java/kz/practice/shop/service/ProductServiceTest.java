package kz.practice.shop.service;

import kz.practice.shop.dto.ProductRequest;
import kz.practice.shop.dto.ProductResponse;
import kz.practice.shop.entity.Category;
import kz.practice.shop.entity.Product;
import kz.practice.shop.exception.ResourceNotFoundException;
import kz.practice.shop.repository.CategoryRepository;
import kz.practice.shop.repository.ProductRepository;
import kz.practice.shop.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock SupplierRepository supplierRepository;
    @InjectMocks ProductService service;

    private Product sampleProduct() {
        Category c = new Category("Электроника");
        c.setId(1L);
        Product p = new Product();
        p.setId(1L);
        p.setName("Phone X");
        p.setPrice(new BigDecimal("250000"));
        p.setCategory(c);
        return p;
    }

    @Test
    void findById_existing_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct()));

        ProductResponse response = service.findById(1L);

        assertEquals("Phone X", response.name());
        assertEquals("Электроника", response.categoryName());
    }

    @Test
    void findById_missing_throwsNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void create_withUnknownCategory_throwsAndDoesNotSave() {
        when(categoryRepository.findById(77L)).thenReturn(Optional.empty());
        ProductRequest request = new ProductRequest("Test", null, BigDecimal.TEN, 77L, null);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_missing_throwsNotFound() {
        when(productRepository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
        verify(productRepository, never()).deleteById(any());
    }
}
