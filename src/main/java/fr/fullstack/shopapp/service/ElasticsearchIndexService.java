package fr.fullstack.shopapp.service;

import fr.fullstack.shopapp.model.Shop;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElasticsearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchIndexService.class);
    private final EntityManager entityManager;

    public ElasticsearchIndexService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void reindexAllOnStartup() {
        try {
            log.info("Démarrage de l'indexation Elasticsearch...");
            
            SearchSession searchSession = Search.session(entityManager);
            MassIndexer indexer = searchSession.massIndexer(Shop.class)
                    .threadsToLoadObjects(5)
                    .batchSizeToLoadObjects(25)
                    .idFetchSize(150)
                    .transactionTimeout(1800);
            
            indexer.startAndWait();
            
            log.info("Elasticsearch: Indexation initiale terminée avec succès");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erreur lors de l'indexation Elasticsearch: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'indexation Elasticsearch", e);
        }
    }
}
