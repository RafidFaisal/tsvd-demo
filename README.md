# Legacy TSVD4J Benchmark Archive

This directory preserves the original loop benchmark used to study TSVD4J's
delay accumulation. It uses the shared `Vector` workload in `src/main/java`
and the concurrent test in `src/test/java`.

The legacy workload is intended to
exercise repeated delay injection across the loop. Its main result is whether
TSVD4J detects a conflicting pair after the complete workload.

## Run One Test

From this directory, run a single configuration:

```bash
cd /home/rafid/tsvd-demo/Test
mvn -q \
  -Dbenchmark.iterations=10 \
  -Dtest=org.apache.log4j.helpers.AppenderAttachableTest \
  tsvd4j:tsvd4j
```

Replace `10` with another iteration count such as `100`, `1000`, or `10000`.
The test prints elapsed time and TSVD4J writes detected conflicts under
`.tsvd4j/`.

## Run Benchmark Matrix

The archived runner executes each configured iteration count five times and
calculates average elapsed time, delay injections, and conflict detection:

```bash
cd /home/rafid/tsvd-demo
chmod +x benchmark-tsvd4j.sh
./benchmark-tsvd4j.sh
```

Results are written to:

```text
/home/rafid/tsvd-demo/benchmark-results.csv
```

The runner currently uses these iteration counts:

```bash
iterations=(10 100 1000 10000)
```

Change `testNumber` to change the number of repeated runs.

## Archived Results

The checked-in `benchmark-results.csv` records the legacy run results:

```text
iterations,mode,runs,completed_runs,avg_elapsed_millis,avg_delay_injections,conflict_detected
10,baseline,5,5,22,0,false
10,tsvd4j,5,5,1028,20,true
100,baseline,5,3,212,0,false
100,tsvd4j,5,5,10260,110,true
1000,baseline,5,0,,0,false
1000,tsvd4j,5,5,2212,13,true
10000,baseline,5,0,,0,false
10000,tsvd4j,5,5,82076,721,true

```

