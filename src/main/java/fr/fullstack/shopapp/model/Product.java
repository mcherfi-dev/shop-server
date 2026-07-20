package fr.fullstack.shopapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "products_seq")
    @SequenceGenerator(name = "products_seq", sequenceName = "products_seq", allocationSize = 50)
    private Long id;

    /**
     * Prix en centimes pour respecter E_PRD_15.
     * Exemple : 12,34 € => 1234.
     */
    @Column(name = "price_cents", nullable = false)
    @PositiveOrZero(message = "Price must be positive")
    @NotNull(message = "Price may not be null")
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonBackReference
    private Shop shop;

    @ManyToMany
    @JoinTable(
        name = "products_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @Size(min = 1, message = "At least one name and one description must be provided")
    private List<@Valid LocalizedProduct> localizedProduct = new ArrayList<>();

    // Getters et Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Integer getPrice() { 
        return price; 
    }

    public void setPrice(Integer price) { 
        this.price = price; 
    }

    public Shop getShop() { 
        return shop; 
    }

    public void setShop(Shop shop) { 
        this.shop = shop; 
    }

    public List<Category> getCategories() { 
        return categories; 
    }

    public void setCategories(List<Category> categories) { 
        this.categories = categories; 
    }

    public List<LocalizedProduct> getLocalizedProducts() { 
        return localizedProduct; 
    }

    public void setLocalizedProducts(List<LocalizedProduct> localizedProduct) { 
        this.localizedProduct = localizedProduct; 
    }
}
