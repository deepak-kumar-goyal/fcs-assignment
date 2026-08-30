# About the assignment

You will find the tasks of this assignment in the [CODE_ASSIGNMENT](java-assignment/CODE_ASSIGNMENT.md) file.

Written questions: [java-assignment/QUESTIONS.md](java-assignment/QUESTIONS.md)

Case study: [case-study/CASE_STUDY.md](case-study/CASE_STUDY.md)

## About the code base

Some of this code here is based on https://github.com/quarkusio/quarkus-quickstarts

## How to run locally

Requires **JDK 17+**. Dev mode uses in-memory H2 (no Docker).

```sh
cd java-assignment
chmod +x mvnw
./mvnw quarkus:dev
```

UI: [http://localhost:8080/index.html](http://localhost:8080/index.html)

Tests and 80% coverage gate:

```sh
cd java-assignment && ./mvnw test
```

More detail (JVM/PostgreSQL, troubleshooting): [java-assignment/README.md](java-assignment/README.md)

## Useful API endpoints

Base URL: `http://localhost:8080`

| Area | Method | Path |
| --- | --- | --- |
| Stores | GET, POST | `/store` |
| Stores | GET, PUT, PATCH, DELETE | `/store/{id}` |
| Products | GET, POST | `/product` |
| Products | GET, PUT, DELETE | `/product/{id}` |
| Warehouses | GET, POST | `/warehouse` |
| Warehouses | GET, DELETE | `/warehouse/{id}` |
| Warehouses | POST | `/warehouse/{businessUnitCode}/replacement` |
| Fulfilment | GET, POST | `/store/{storeId}/fulfilment` |
| Fulfilment | DELETE | `/store/{storeId}/fulfilment/{id}` |

Examples:

```sh
curl http://localhost:8080/store
curl http://localhost:8080/product
curl http://localhost:8080/warehouse
curl -s -X POST http://localhost:8080/warehouse \
  -H 'Content-Type: application/json' \
  -d '{"businessUnitCode":"MWH.200","location":"EINDHOVEN-001","capacity":30,"stock":4}'
curl -s -X POST http://localhost:8080/store/2/fulfilment \
  -H 'Content-Type: application/json' \
  -d '{"productId":2,"warehouseBusinessUnitCode":"MWH.023"}'
```
