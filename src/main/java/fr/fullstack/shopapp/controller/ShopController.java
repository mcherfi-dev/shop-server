package fr.fullstack.shopapp.controller;

import fr.fullstack.shopapp.model.Shop;
import fr.fullstack.shopapp.service.ShopService;
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

@Tag(name = "Shop Management", description = "API endpoints for managing shops with Elasticsearch search")
@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {

    private final ShopService service;

    public ShopController(ShopService service) {
        this.service = service;
    }

    @Operation(summary = "Create a new shop", description = "Creates a new shop with validation (E_FIX_10)")
    @PostMapping
    public ResponseEntity<Shop> createShop(@Valid @RequestBody Shop shop, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorValidation.getErrorValidationMessage(errors)
            );
        }

        try {
            Shop created = service.createShop(shop);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete a shop")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable long id) {
        try {
            service.deleteShopById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get all shops", description = "Get paginated list with filters (E_BTQ_50)")
    @GetMapping
    public ResponseEntity<Page<Shop>> getAllShops(
        @PageableDefault(size = 10, page = 0) Pageable pageable,
        @Parameter(description = "Sort by field") @RequestParam(required = false) Optional<String> sortBy,
        @Parameter(description = "Filter by vacation status") @RequestParam(required = false) Optional<Boolean> inVacations,
        @Parameter(description = "Created after date (yyyy-MM-dd)") @RequestParam(required = false) Optional<String> createdAfter,
        @Parameter(description = "Created before date (yyyy-MM-dd)") @RequestParam(required = false) Optional<String> createdBefore
    ) {
        return ResponseEntity.ok(service.getShopList(sortBy, inVacations, createdAfter, createdBefore, pageable));
    }

    @Operation(summary = "Get shop by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Shop> getShopById(@PathVariable long id) {
        try {
            return ResponseEntity.ok(service.getShopById(id));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(
        summary = "Search shops with Elasticsearch (E_BTQ_65)",
        description = "Full-text search with filters using Elasticsearch"
    )
    @GetMapping("/search")
    public ResponseEntity<Page<Shop>> searchShops(
        @PageableDefault(size = 10, page = 0) Pageable pageable,
        @Parameter(description = "Text to search in shop name") @RequestParam(required = false) Optional<String> text,
        @Parameter(description = "Filter by vacation status") @RequestParam(required = false) Optional<Boolean> inVacations,
        @Parameter(description = "Created after date (yyyy-MM-dd)") @RequestParam(required = false) Optional<String> createdAfter,
        @Parameter(description = "Created before date (yyyy-MM-dd)") @RequestParam(required = false) Optional<String> createdBefore
    ) {
        return ResponseEntity.ok(service.searchShopsElasticsearch(text, inVacations, createdAfter, createdBefore, pageable));
    }

    @Operation(summary = "Update a shop", description = "Updates shop with validation (E_FIX_10)")
    @PutMapping
    public ResponseEntity<Shop> updateShop(@Valid @RequestBody Shop shop, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                ErrorValidation.getErrorValidationMessage(errors)
            );
        }

        try {
            return ResponseEntity.ok(service.updateShop(shop));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
