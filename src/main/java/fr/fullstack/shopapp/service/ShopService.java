package fr.fullstack.shopapp.service;

import fr.fullstack.shopapp.model.OpeningHoursShop;
import fr.fullstack.shopapp.model.Shop;
import fr.fullstack.shopapp.repository.ShopRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ShopService {

    private static final String CREATED_AT_FIELD = "createdAt";

    private final ShopRepository shopRepository;
    private final EntityManager entityManager;

    public ShopService(ShopRepository shopRepository, EntityManager entityManager) {
        this.shopRepository = shopRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Shop createShop(Shop shop) {
        validateOpeningHours(shop);
        return shopRepository.save(shop);
    }

    @Transactional
    public void deleteShopById(long id) {
        if (!shopRepository.existsById(id)) {
            throw new IllegalArgumentException("Shop not found with id: " + id);
        }
        shopRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Shop getShopById(long id) {
        return shopRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Shop> getShopList(
        Optional<String> sortBy,
        Optional<Boolean> inVacations,
        Optional<String> createdAfter,
        Optional<String> createdBefore,
        Pageable pageable
    ) {
        if (sortBy.isPresent() && "nbProducts".equals(sortBy.get())) {
            return shopRepository.findByOrderByNbProductsDesc(pageable);
        }

        Page<Shop> filteredByDateAndVacation =
            filterByDateAndVacation(inVacations, createdAfter, createdBefore, pageable);
        if (filteredByDateAndVacation != null) {
            return filteredByDateAndVacation;
        }

        if (sortBy.isPresent()) {
            return sortShops(sortBy.get(), pageable);
        }

        return shopRepository.findAll(pageable);
    }

    private Page<Shop> filterByDateAndVacation(
        Optional<Boolean> inVacations,
        Optional<String> createdAfter,
        Optional<String> createdBefore,
        Pageable pageable
    ) {
        LocalDate afterDate = createdAfter.map(LocalDate::parse).orElse(null);
        LocalDate beforeDate = createdBefore.map(LocalDate::parse).orElse(null);

        if (inVacations.isPresent() && afterDate != null && beforeDate != null) {
            return shopRepository.findByInVacationsAndCreatedAtGreaterThanAndCreatedAtLessThan(
                inVacations.get(), afterDate, beforeDate, pageable);
        }

        if (inVacations.isPresent() && afterDate != null) {
            return shopRepository.findByInVacationsAndCreatedAtGreaterThan(
                inVacations.get(), afterDate, pageable);
        }

        if (inVacations.isPresent() && beforeDate != null) {
            return shopRepository.findByInVacationsAndCreatedAtLessThan(
                inVacations.get(), beforeDate, pageable);
        }

        if (inVacations.isPresent()) {
            return shopRepository.findByInVacations(inVacations.get(), pageable);
        }

        if (afterDate != null && beforeDate != null) {
            return shopRepository.findByCreatedAtBetween(afterDate, beforeDate, pageable);
        }

        if (afterDate != null) {
            return shopRepository.findByCreatedAtGreaterThan(afterDate, pageable);
        }

        if (beforeDate != null) {
            return shopRepository.findByCreatedAtLessThan(beforeDate, pageable);
        }

        return null;
    }

    private Page<Shop> sortShops(String sortBy, Pageable pageable) {
        if ("name".equals(sortBy)) {
            return shopRepository.findByOrderByNameAsc(pageable);
        }
        if (CREATED_AT_FIELD.equals(sortBy)) {
            return shopRepository.findByOrderByCreatedAtAsc(pageable);
        }
        if ("id".equals(sortBy)) {
            return shopRepository.findByOrderByIdAsc(pageable);
        }
        return shopRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Shop> searchShopsElasticsearch(
        Optional<String> text,
        Optional<Boolean> inVacations,
        Optional<String> createdAfter,
        Optional<String> createdBefore,
        Pageable pageable
    ) {
        SearchSession searchSession = Search.session(entityManager);

        var searchQueryBuilder = searchSession.search(Shop.class)
            .where(f -> {
                BooleanPredicateClausesStep<?> boolBuilder = f.bool();

                text.ifPresent(t ->
                    boolBuilder.must(
                        f.match()
                            .fields("name")
                            .matching(t)
                    )
                );

                inVacations.ifPresent(v ->
                    boolBuilder.must(
                        f.match()
                            .field("inVacations")
                            .matching(v)
                    )
                );

                createdAfter.ifPresent(s -> {
                    LocalDate d = LocalDate.parse(s);
                    boolBuilder.must(
                        f.range()
                            .field(CREATED_AT_FIELD)
                            .atLeast(d)
                    );
                });

                createdBefore.ifPresent(s -> {
                    LocalDate d = LocalDate.parse(s);
                    boolBuilder.must(
                        f.range()
                            .field(CREATED_AT_FIELD)
                            .atMost(d)
                    );
                });

                boolean hasFilter =
                    text.isPresent()
                        || inVacations.isPresent()
                        || createdAfter.isPresent()
                        || createdBefore.isPresent();

                if (!hasFilter) {
                    return f.matchAll();
                }

                return boolBuilder;
            });

        SearchResult<Shop> result = searchQueryBuilder
            .fetch((int) pageable.getOffset(), pageable.getPageSize());

        // Copier dans une liste modifiable avant de trier
        List<Shop> shops = new ArrayList<>(result.hits());
        long totalHits = result.total().hitCount();

        shops.sort(Comparator.comparing(Shop::getId));

        return new PageImpl<>(shops, pageable, totalHits);
    }

    @Transactional
    public Shop updateShop(Shop shop) {
        if (shop.getId() == null || !shopRepository.existsById(shop.getId())) {
            throw new IllegalArgumentException("Shop not found with id: " + shop.getId());
        }
        validateOpeningHours(shop);
        return shopRepository.save(shop);
    }

    private void validateOpeningHours(Shop shop) {
        if (shop.getOpeningHours() == null || shop.getOpeningHours().isEmpty()) {
            return;
        }

        List<OpeningHoursShop> openingHours = shop.getOpeningHours();

        for (int i = 0; i < openingHours.size(); i++) {
            for (int j = i + 1; j < openingHours.size(); j++) {
                OpeningHoursShop hour1 = openingHours.get(i);
                OpeningHoursShop hour2 = openingHours.get(j);

                if (hour1.getDayOfWeek().equals(hour2.getDayOfWeek())
                    && hour1.getOpeningTime().isBefore(hour2.getClosingTime())
                    && hour1.getClosingTime().isAfter(hour2.getOpeningTime())) {

                    throw new IllegalArgumentException(
                        String.format(
                            "Conflit d'horaires détecté pour le jour %s: %s-%s chevauche %s-%s",
                            hour1.getDayOfWeek(),
                            hour1.getOpeningTime(), hour1.getClosingTime(),
                            hour2.getOpeningTime(), hour2.getClosingTime()
                        )
                    );
                }
            }
        }
    }
}
