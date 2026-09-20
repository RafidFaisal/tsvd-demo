# TSVD4J First-Iteration Experiment

This project evaluates an optimized TSVD4J configuration that injects at most
one delay per worker thread. The goal is to measure whether a conflicting pair
is detected during the first delayed interleaving without accumulating delays
through every loop iteration.

## Build

Build and install the optimized TSVD4J distribution:

```bash
cd /home/rafid/tsvd-demo/TSVD4J-first-iteration
mvn -q install -DskipTests
```

The optimized experiment project uses separate Maven coordinates, so this does
not modify or overwrite another TSVD4J distribution.

## Run One Test

Run the workload for a specific iteration count:

```bash
cd /home/rafid/tsvd-demo/Test
mvn -q \
	-Dbenchmark.iterations=10 \
	-Dtest=org.apache.log4j.helpers.AppenderAttachableTest \
	tsvd4j:tsvd4j
```

Change `10` to another iteration count such as `100`, `1000`, or `10000`.
The output includes elapsed time, detected conflicting pairs, and the number
of delay injections.

## Run Benchmark Matrix

Run the repeated benchmark for all configured iteration counts:

```bash
cd /home/rafid/tsvd-demo/Test
chmod +x benchmark-first-iteration.sh
./benchmark-first-iteration.sh
```

The script runs each configuration five times by default and displays live
progress. Results are written to:

```text
Test/benchmark-results.csv
```

The CSV reports:

- iteration count
- requested and completed runs
- average elapsed time
- average delay injections
- whether a conflict was detected

To change the number of repetitions or iteration counts, edit `testNumber` or
the `iterations` array in `benchmark-first-iteration.sh`.
