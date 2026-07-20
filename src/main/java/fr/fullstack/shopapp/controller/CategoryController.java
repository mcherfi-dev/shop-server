package fr.fullstack.shopapp.controller;

import fr.fullstack.shopapp.model.Category;
import fr.fullstack.shopapp.service.CategoryService;
import fr.fullstack.shopapp.util.ErrorValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "Category Management", description = "API endpoints for managing product categories")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @Operation(
        summary = "Create a new category",
        description = "Creates a new product category with validation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid category data or validation error")
    })
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                ErrorValidation.getErrorValidationMessage(errors)
            );
        }

        try {
            return ResponseEntity.ok(service.createCategory(category));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete a category", description = "Deletes a category by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category successfully deleted"),
        @ApiResponse(responseCode = "400", description = "Invalid category ID or deletion failed")
    })
    @DeleteMapping("/{id}")
    public HttpStatus deleteCategory(@Parameter(description = "Category ID", example = "1") @PathVariable long id) {
        try {
            service.deleteCategoryById(id);
            return HttpStatus.NO_CONTENT;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get all categories", description = "Retrieves a paginated list of all categories")
    @GetMapping
    public ResponseEntity<Page<Category>> getAllCategories(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getCategoryList(pageable));
    }

    @Operation(summary = "Get category by ID", description = "Retrieves a single category by its unique identifier")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@Parameter(description = "Category ID", example = "1") @PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.getCategoryById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Update a category", description = "Updates an existing category with validation")
    @PutMapping
    public ResponseEntity<Category> updateCategory(@Valid @RequestBody Category category, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                ErrorValidation.getErrorValidationMessage(errors)
            );
        }

        try {
            return ResponseEntity.ok().body(service.updateCategory(category));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
