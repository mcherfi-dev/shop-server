package fr.fullstack.shopapp.controller;

import fr.fullstack.shopapp.model.Product;
import fr.fullstack.shopapp.service.ProductService;
import fr.fullstack.shopapp.util.ErrorValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Tag(name = "Product Management", description = "API endpoints for managing products")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(
        summary = "Create a new product",
        description = "Creates a new product with validation. Price must be in cents (E_PRD_15)."
    )
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorValidation.getErrorValidationMessage(errors)
            );
        }

        try {
            Product created = service.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by its unique identifier")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable long id
    ) {
        try {
            service.deleteProductById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a single product by its identifier")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID", example = "1", required = true)
            @PathVariable long id
    ) {
        try {
            return ResponseEntity.ok().body(service.getProductById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(
        summary = "Get all products",
        description = "Retrieves a paginated list of products with optional filters by shop and category"
    )
    @GetMapping
    public ResponseEntity<Page<Product>> getProductsOfShop(
            @PageableDefault(size = 10, page = 0)
            @Parameter(
                description = "Pagination parameters (page, size, sort)",
                example = "page=0&size=10&sort=price,asc"
            )
            Pageable pageable,

            @Parameter(description = "Filter by shop ID", example = "1")
            @RequestParam(required = false) Optional<Long> shopId,

            @Parameter(description = "Filter by category ID", example = "2")
            @RequestParam(required = false) Optional<Long> categoryId
    ) {
        return ResponseEntity.ok(service.getShopProductList(shopId, categoryId, pageable));
    }

    @Operation(
        summary = "Update a product",
        description = "Updates an existing product with validation. Price must be in cents (E_PRD_15)."
    )
    @PutMapping
    public ResponseEntity<Product> updateProduct(@Valid @RequestBody Product product, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorValidation.getErrorValidationMessage(errors)
            );
        }

        try {
            return ResponseEntity.ok().body(service.updateProduct(product));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
