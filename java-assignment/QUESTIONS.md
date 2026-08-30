# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes. I would not rewrite everything at once, but I would converge persistence toward one style per kind of module.

Today there are three approaches:

- Store extends PanacheEntity and is queried directly from the REST resource (Active Record).
- Product uses a PanacheRepository, which is already a better split, but the resource still owns all rules.
- Warehouse is closer to hexagonal architecture: domain model + WarehouseStore port + DbWarehouse adapter.

If I had to maintain this, I would:

1. Keep Panache, but stop mixing Active Record and repository in the same bounded context. Store should look like Product (repository), so entities are not the public API of the REST layer.
2. Keep the Warehouse port/adapter split for anything with real business rules (create/replace/archive, location caps, fulfilment constraints). Those rules should not live in JAX-RS resources.
3. Avoid leaking JPA entities over HTTP long term (Store/Product). A DTO/OpenAPI model reduces accidental coupling (lazy loads, persistence annotations, sequence ids in clients).

I would not force full hexagonal architecture onto Product CRUD. Simple register maintenance does not earn that ceremony. I would apply the richer model where invariants exist, and keep CRUD thin.

The main risk of leaving it mixed is that the next change (legacy sync, fulfilment, warehouse replace) has no obvious home, so logic keeps landing in resources and becomes hard to test without the database.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Contract-first (Warehouse OpenAPI yaml → generated resource/beans)

Pros:
- The HTTP contract is explicit, reviewable, and can be shared with other teams or mock servers before code exists.
- Breaking changes are visible in git (field rename, status codes).
- Generated models reduce drift between docs and implementation if generation runs in CI.

Cons:
- Generator and handwritten domain models diverge (we already map com.warehouse.api.beans.Warehouse ↔ domain Warehouse).
- Local iteration is slower; small handler tweaks require yaml + regenerate + implement.
- Generated code is awkward in the IDE unless build-helper/generated-sources is wired.

Code-first (Product/Store JAX-RS)

Pros:
- Fast for internal CRUD; one class is the source of truth.
- Easy to add a field and ship.
- No generator toolchain.

Cons:
- Documentation and client stubs lag unless you export OpenAPI from the running app and treat it as published.
- Easy to accidentally change a status code or payload shape.
- Inconsistent style next to Warehouse makes onboarding harder.

My choice: hybrid, with a rule.

- Public or cross-team APIs (Warehouse, fulfilment associations) stay contract-first. The yaml is the review artifact.
- Internal CRUD can stay code-first, but I would enable Quarkus SmallRye OpenAPI so /q/openapi is generated from annotations and published in CI.
- I would not generate server stubs for every resource; I would generate them where the contract is stable and the mapping cost is worth it.

I would not convert Product/Store to OpenAPI generation just for uniformity. I would convert them if another system starts depending on those payloads.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I would optimize for tests that protect business invariants, not for 100% line coverage of getters.

Priority order:

1. Pure unit tests for warehouse and fulfilment rules (duplicate BU, location caps, stock vs capacity, replace stock matching, archive, max 2 warehouses per product per store, max 3 warehouses per store, max 5 products per warehouse). These are cheap, fast, and are where production bugs will hide. In-memory fakes of WarehouseStore are enough.
2. A thin slice of @QuarkusTest REST tests: happy path + 400/404 for each resource. These catch mapping, transactions, and OpenAPI wiring. They are slower (Dev Services) so I would keep them few and independent of each other.
3. Integration tests (@QuarkusIntegrationTest) only for a couple of smoke flows (list + archive), because they run against the packaged app and duplicate @QuarkusTest cost.

I would not start with UI tests or native-image tests for this assignment.

To keep coverage useful over time:

- Gate CI on unit/integration coverage (JaCoCo, 80% instruction coverage on our code, excluding generated OpenAPI types). Coverage is a tripwire, not the goal.
- Review coverage reports for uncovered *branches in use cases*, not for missing tests on DTOs.
- When a production defect appears, add a failing unit test first, then fix.
- Prefer deterministic data (unique business unit codes) over depending on import.sql row ids across test classes.

If time is short, I would drop extra REST permutations before I drop use-case validation tests. The domain rules are the product.
```
