# TSVD4J first-iteration stress study

This branch keeps the two tool variants separate and compares them on the same Java concurrency subjects.

## Layout

- [TSVD4J-original](TSVD4J-original): the unmodified TSVD4J distribution
- [TSVD4J-first-iteration](TSVD4J-first-iteration): the modified variant that limits delay injection to the first relevant access
- [benchmarks](benchmarks): small JaConTeBe-style concurrency subjects used to stress the detector

## Run one benchmark under the original tool

```bash
cd /home/rafid/tsvd-demo
mvn -q -f TSVD4J-original/pom.xml install
mvn -q -f benchmarks/collection-threshold test tsvd4j:tsvd4j
```

## Run the same benchmark under the first-iteration tool

```bash
cd /home/rafid/tsvd-demo
mvn -q -f TSVD4J-first-iteration/pom.xml install
mvn -q -f benchmarks/collection-threshold test tsvd4j:tsvd4j
```

## Run the whole suite

```bash
cd /home/rafid/tsvd-demo
./benchmarks/run-suite.sh
```

The purpose is to discover whether the first-iteration optimization misses real conflicts on late-state concurrency patterns.

