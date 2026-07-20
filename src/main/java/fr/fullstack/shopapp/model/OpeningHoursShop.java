package fr.fullstack.shopapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "opening_hours_shop")
public class OpeningHoursShop {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opening_hours_shop_seq")
    @SequenceGenerator(name = "opening_hours_shop_seq", sequenceName = "opening_hours_shop_seq", allocationSize = 50)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonBackReference
    private Shop shop;

    // Constructeurs
    public OpeningHoursShop() {
    }

    public OpeningHoursShop(DayOfWeek dayOfWeek, LocalTime openingTime, LocalTime closingTime, Shop shop) {
        this.dayOfWeek = dayOfWeek;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.shop = shop;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    // Méthode utilitaire pour vérifier les chevauchements
    public boolean overlapsWith(OpeningHoursShop other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        return this.openingTime.isBefore(other.closingTime) && 
               this.closingTime.isAfter(other.openingTime);
    }

    @Override
    public String toString() {
        return "OpeningHoursShop{" +
                "id=" + id +
                ", dayOfWeek=" + dayOfWeek +
                ", openingTime=" + openingTime +
                ", closingTime=" + closingTime +
                '}';
    }
}
