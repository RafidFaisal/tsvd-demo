# TSVD4J First-Iteration Experiment

This project uses the same shared `Vector` workload as the demo project, but enables the opt-in TSVD4J mode:

```xml
<tsvd4j.firstIterationOnly>true</tsvd4j.firstIterationOnly>
```

In this mode, TSVD4J allows at most one delay injection per worker thread. The benchmark reports elapsed time, average delay injections, and whether TSVD4J detected a conflict during that first delayed interleaving.

Build the independent optimized TSVD4J distribution first:

```bash
cd ../TSVD4J-first-iteration
mvn -q install -DskipTests
cd ../tsvd4j-first-iteration
```

The original demo continues to use the unoptimized distribution in `../TSVD4J`.
The optimized project uses distinct Maven coordinates and does not overwrite it.

Run one case:

```bash
mvn -q -Dbenchmark.iterations=10 \
  -Dtest=org.apache.log4j.helpers.AppenderAttachableTest \
  tsvd4j:tsvd4j
```

Run the experiment matrix:

```bash
./benchmark-first-iteration.sh
```
