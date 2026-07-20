package fr.fullstack.shopapp.service;

import fr.fullstack.shopapp.model.LocalizedProduct;
import fr.fullstack.shopapp.model.Product;
import fr.fullstack.shopapp.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductService {

    public static class ProductException extends RuntimeException {
        public ProductException(String message) {
            super(message);
        }

        public ProductException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @PersistenceContext
    private EntityManager em;

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product createProduct(Product product) {
        checkLocalizedProducts(product);

        try {
            Product newProduct = productRepository.save(product);
            em.flush();
            em.refresh(newProduct);
            return newProduct;
        } catch (RuntimeException e) {
            throw new ProductException("Unable to create product", e);
        }
    }

    @Transactional
    public void deleteProductById(long id) {
        Product product = getProduct(id);
        try {
            productRepository.delete(product);
        } catch (RuntimeException e) {
            throw new ProductException("Unable to delete product with id " + id, e);
        }
    }

    @Transactional(readOnly = true)
    public Product getProductById(long id) {
        return getProduct(id);
    }

    @Transactional(readOnly = true)
    public Page<Product> getShopProductList(Optional<Long> shopId, Optional<Long> categoryId, Pageable pageable) {
        if (shopId.isPresent() && categoryId.isPresent()) {
            return productRepository.findByShopAndCategory(shopId.get(), categoryId.get(), pageable);
        }

        if (shopId.isPresent()) {
            return productRepository.findByShop(shopId.get(), pageable);
        }

        return productRepository.findByOrderByIdAsc(pageable);
    }

    @Transactional
    public Product updateProduct(Product product) {
        if (product.getId() == null) {
            throw new ProductException("Product id is required for update");
        }

        getProduct(product.getId());

        checkLocalizedProducts(product);

        try {
            Product updated = productRepository.save(product);
            em.flush();
            em.refresh(updated);
            return updated;
        } catch (RuntimeException e) {
            throw new ProductException("Unable to update product with id " + product.getId(), e);
        }
    }

    private void checkLocalizedProducts(Product product) {
        Optional<LocalizedProduct> localizedProductFr = product.getLocalizedProducts()
            .stream()
            .filter(o -> "FR".equalsIgnoreCase(o.getLocale()))
            .findFirst();

        if (localizedProductFr.isEmpty()) {
            throw new ProductException("A french localized product must be provided");
        }

        LocalizedProduct fr = localizedProductFr.get();

        if (fr.getName() == null || fr.getName().isBlank()) {
            throw new ProductException("French name is required");
        }

        if (fr.getDescription() == null || fr.getDescription().isBlank()) {
            throw new ProductException("French description is required");
        }
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductException("Product with id " + id + " not found"));
    }
}
