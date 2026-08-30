# Java Code Assignment

This is a short code assignment that explores various aspects of software development, including API implementation, documentation, persistence layer handling, and testing.

## About the assignment

You will find the tasks of this assignment on [CODE_ASSIGNMENT](CODE_ASSIGNMENT.md) file

## About the code base

This is based on https://github.com/quarkusio/quarkus-quickstarts

### Requirements

To compile and run this demo you will need:

- JDK 17+

Tests and `quarkus:dev` use an in-memory H2 database (no Docker required). Production JVM mode still expects PostgreSQL (see below).

### Configuring JDK 17+

Make sure that `JAVA_HOME` environment variables has been set, and that a JDK 17+ `java` command is on the path.

## How to run locally

Work from this `java-assignment` directory.

1. Install **JDK 17+** and set `JAVA_HOME`. Confirm with `java -version`.
2. Make the Maven wrapper executable (once): `chmod +x mvnw`
3. Start the app in Quarkus dev mode (in-memory H2, no Docker):

```sh
./mvnw quarkus:dev
```

4. Open the demo UI: [http://localhost:8080/index.html](http://localhost:8080/index.html)

Seed data from `src/main/resources/import.sql` is loaded on startup (stores, products, warehouses).

### Tests

```sh
./mvnw test
```

Fails if instruction coverage is below 80% (generated OpenAPI types excluded). Report: `target/jacoco-report/index.html`.

### Package (optional)

```sh
./mvnw package
```

### JVM mode with PostgreSQL (optional)

Dev/test use H2. Packaged production profile expects PostgreSQL on `localhost:15432`:

```sh
docker run -it --rm=true --name quarkus_test \
  -e POSTGRES_USER=quarkus_test \
  -e POSTGRES_PASSWORD=quarkus_test \
  -e POSTGRES_DB=quarkus_test \
  -p 15432:5432 postgres:13.3
```

```sh
./mvnw package
java -jar ./target/quarkus-app/quarkus-run.jar
```

Datasource settings are in `src/main/resources/application.properties`.

## Useful API endpoints

Base URL: `http://localhost:8080`

### Stores (`/store`)

| Method | Path | Description |
| --- | --- | --- |
| GET | `/store` | List stores |
| GET | `/store/{id}` | Get store (seed ids: 1, 2, 3) |
| POST | `/store` | Create store |
| PUT | `/store/{id}` | Replace store |
| PATCH | `/store/{id}` | Partial update |
| DELETE | `/store/{id}` | Delete store |

```sh
curl http://localhost:8080/store
curl -s -X POST http://localhost:8080/store \
  -H 'Content-Type: application/json' \
  -d '{"name":"CITY-CENTRE","quantityProductsInStock":12}'
```

### Products (`/product`)

| Method | Path | Description |
| --- | --- | --- |
| GET | `/product` | List products |
| GET | `/product/{id}` | Get product (seed ids: 1, 2, 3) |
| POST | `/product` | Create product |
| PUT | `/product/{id}` | Update product |
| DELETE | `/product/{id}` | Delete product |

```sh
curl http://localhost:8080/product
```

### Warehouses (`/warehouse`)

| Method | Path | Description |
| --- | --- | --- |
| GET | `/warehouse` | List **active** warehouses |
| POST | `/warehouse` | Create warehouse (valid location, BU unique, location caps) |
| GET | `/warehouse/{id}` | Get by id (seed: `1` = MWH.001, `2` = MWH.012, `3` = MWH.023) |
| DELETE | `/warehouse/{id}` | Archive warehouse |
| POST | `/warehouse/{businessUnitCode}/replacement` | Archive current BU and create a replacement with the same code |

Known locations include `ZWOLLE-001`, `ZWOLLE-002`, `AMSTERDAM-001`, `AMSTERDAM-002`, `TILBURG-001`, `HELMOND-001`, `EINDHOVEN-001`, `VETSBY-001`.

```sh
curl http://localhost:8080/warehouse
curl http://localhost:8080/warehouse/2
curl -s -X POST http://localhost:8080/warehouse \
  -H 'Content-Type: application/json' \
  -d '{"businessUnitCode":"MWH.200","location":"EINDHOVEN-001","capacity":30,"stock":4}'
curl -s -X POST http://localhost:8080/warehouse/MWH.012/replacement \
  -H 'Content-Type: application/json' \
  -d '{"location":"AMSTERDAM-002","capacity":40,"stock":5}'
curl -X DELETE http://localhost:8080/warehouse/1
```

Generated create handlers return HTTP **200** (not 201) because the OpenAPI server stub returns the entity body.

### Fulfilment associations (`/store/{storeId}/fulfilment`)

Links a product + warehouse to a store. Constraints: max 2 warehouses per product per store, max 3 warehouses per store, max 5 product types per warehouse.

| Method | Path | Description |
| --- | --- | --- |
| GET | `/store/{storeId}/fulfilment` | List associations for a store |
| POST | `/store/{storeId}/fulfilment` | Create association |
| DELETE | `/store/{storeId}/fulfilment/{id}` | Remove association |

```sh
curl -s -X POST http://localhost:8080/store/2/fulfilment \
  -H 'Content-Type: application/json' \
  -d '{"productId":2,"warehouseBusinessUnitCode":"MWH.023"}'
curl http://localhost:8080/store/2/fulfilment
```

## Troubleshooting

Using **IntelliJ**, in case the generated code is not recognized and you have compilation failures, you may need to add `target/.../jaxrs` folder as "generated sources".