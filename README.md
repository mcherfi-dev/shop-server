# shop-server

API REST de gestion de boutiques et de produits, avec recherche full-text. Back end de
l'application, dont l'interface se trouve dans [shop-client](https://github.com/mcherfi-dev/shop-client).

**Contexte** : projet universitaire, Master Informatique, UFR Sciences et Techniques de Rouen, 2025.

## Le problème

Une liste de boutiques se pagine sans difficulté. Une recherche qui doit croiser plusieurs critères
sur des produits traduits en plusieurs langues, en restant rapide, n'est plus du ressort d'un
`LIKE %terme%` en SQL.

D'où la répartition retenue : PostgreSQL reste la source de vérité et garantit la cohérence des
données, Elasticsearch prend en charge la recherche. Hibernate Search fait le lien et maintient
l'index à jour au fil des écritures, ce qui évite d'avoir à synchroniser les deux moteurs à la main.

## Architecture

```
Controller      validation des entrées, codes HTTP, pagination
    |
    v
Service         règles métier, transactions, indexation
    |
    v
Repository      accès aux données (Spring Data JPA)
    |
    v
PostgreSQL  <-------- Hibernate Search --------> Elasticsearch
(source de vérité)                               (recherche)
```

Le modèle de domaine gère la traduction (`LocalizedProduct`, `Locale`), les horaires d'ouverture
(`OpeningHoursShop`) et les catégories. Les erreurs remontent via un `GlobalExceptionHandler`
unique, pour que l'API réponde de façon homogène plutôt que d'exposer des traces Java.

La validation s'appuie sur Bean Validation, avec une contrainte maison (`@StringEnumeration`) qui
vérifie qu'une chaîne appartient bien à une énumération donnée.

## Stack

| Couche | Technologie |
|---|---|
| Framework | Spring Boot 3.3, Java 17 |
| Persistance | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Recherche | Hibernate Search, backend Elasticsearch |
| Documentation | springdoc-openapi (Swagger UI) |
| Conteneurisation | Docker, image multi-étapes |

## Démarrage

Depuis la racine du projet principal, là où se trouve le `docker-compose.yml` :

```bash
docker compose up
```

L'API est disponible sur http://localhost:8080
La documentation Swagger, interactive, sur http://localhost:8080/swagger-ui.html

Le `Dockerfile` est en deux étapes : une image JDK compile le jar, une image JRE Alpine l'exécute.
L'image finale ne contient donc ni Maven ni les sources. Le point d'entrée attend qu'Elasticsearch
réponde sur `/_cluster/health` avant de démarrer Spring, sans quoi l'application échouerait au
premier lancement du `compose`.

### Configuration

Tout est piloté par variables d'environnement, avec des valeurs par défaut de développement local.
Aucun identifiant réel n'est versionné.

| Variable | Rôle | Défaut |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://db:5432/postgres` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe | à définir |
| `HIBERNATE_SEARCH_BACKEND_HOSTS` | Hôte Elasticsearch | `elasticsearch:9200` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Stratégie de schéma | `update` |

## Endpoints principaux

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/api/v1/shops` | Liste paginée des boutiques |
| `GET` | `/api/v1/shops/search` | Recherche Elasticsearch avec filtres |
| `POST` | `/api/v1/shops` | Créer une boutique |
| `PUT` | `/api/v1/shops/{id}` | Modifier une boutique |
| `DELETE` | `/api/v1/shops/{id}` | Supprimer une boutique |
| `GET` | `/api/v1/products` | Liste des produits |
| `GET` | `/api/v1/categories` | Liste des catégories |

La liste complète et les schémas de requête sont dans Swagger.

## Limites connues

- Pas d'authentification. La configuration de sécurité se limite au CORS, ouvert vers le front en
  développement. Une mise en production imposerait au minimum un filtre d'authentification.
- `ddl-auto=update` convient au développement, pas à la production. Il faudrait des migrations
  versionnées, avec Flyway ou Liquibase.
- Couverture de tests insuffisante.
