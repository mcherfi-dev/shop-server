package fr.fullstack.shopapp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shops")
@Indexed
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shops_seq")
    @SequenceGenerator(name = "shops_seq", sequenceName = "shops_seq", allocationSize = 50)
    private Long id;

    @NotBlank(message = "Shop name is required")
    @FullTextField
    private String name;

    @GenericField
    private LocalDate createdAt;

    @GenericField
    private Boolean inVacations;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<OpeningHoursShop> openingHours = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Product> products = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (inVacations == null) {
            inVacations = false;
        }
    }

    @Transient
    public int getNbProducts() {
        return products != null ? products.size() : 0;
    }

    @Transient
    public long getNbDistinctCategories() {
        if (products == null || products.isEmpty()) {
            return 0;
        }
        return products.stream()
            .flatMap(p -> p.getCategories().stream())
            .map(Category::getId)
            .distinct()
            .count();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getInVacations() {
        return inVacations;
    }

    public void setInVacations(Boolean inVacations) {
        this.inVacations = inVacations;
    }

    public List<OpeningHoursShop> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<OpeningHoursShop> openingHours) {
        this.openingHours = openingHours;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
