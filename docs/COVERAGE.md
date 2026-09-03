# Test coverage

The Maven build enforces both instruction and line coverage above 80%. The
minimum for each counter is 81%, so a regression fails the build before it can
be merged.

## Latest verified baseline

Measured on 3 September 2026 with `./mvnw clean test`:

- Tests: 45 passed, 0 failed, 0 skipped
- Instruction coverage: 84.6% (1,576 of 1,863)
- Line coverage: 85.6% (397 of 464)

Generated OpenAPI server classes under `com.warehouse.api` are excluded because
they are generated from `warehouse-openapi.yaml`, not maintained source code.

## Generate the report

From `java-assignment`:

```sh
./mvnw clean test
open target/jacoco-report/index.html
```

CI runs the same coverage gate for every push and pull request. It uploads the
complete HTML report as the `jacoco-coverage-report` workflow artifact and
retains it for 30 days.
