# JaConTeBe-style concurrency stress suite

This folder contains a small set of Java concurrency subjects designed to match the patterns used in JaConTeBe-style benchmark suites: late-stage shared-state races in collections, maps, queues, and task queues.

Each module is intentionally structured so that the shared resource is quiet for a warm-up phase and becomes dangerous only later. This is useful for stress-testing the first-iteration delay heuristic in the modified TSVD4J variant.

## Modules

- collection-threshold
- map-threshold
- queue-drain
- executor-queue

## Why these modules matter

These benchmarks are designed to stress the exact optimization under test: delaying only on the first relevant access instead of repeatedly delaying during later iterations of a loop.

The key idea is to avoid a trivial race that is visible immediately. Instead, each module starts in a quiet state, performs a warm-up phase, and only then enters the dangerous state where two threads mutate the same shared structure concurrently.

This pattern is important because the first-iteration optimization is only a heuristic. It works when the bug is already reachable on the first relevant shared access. It is much less reliable when the real conflict depends on later state transitions such as:

- a collection crossing a size threshold
- a map growing enough to trigger a resize or clearing phase
- a queue becoming non-empty and then draining
- a task queue reaching a point where producer/consumer overlap matters

That makes these modules closer to the kind of real-world concurrency patterns one sees in JaConTeBe-style suites, where the bug is not simply “two threads touch the same object immediately,” but “the shared object becomes dangerous only after a later lifecycle stage.”

### collection-threshold

Two threads share an `ArrayList`. The first few iterations are warm-up only. After that, both threads add values and clear the list once it exceeds a threshold. This models the common pattern where a collection is harmless until it crosses a size boundary.

### map-threshold

Two threads share a `HashMap`. Early iterations only warm up the object; later iterations `put` into the shared map and clear it after a growth threshold. This is meant to stress whether a first-only delay still catches a race that only becomes visible once the map has reached a meaningful size.

### queue-drain

Two threads share a `LinkedBlockingQueue`. The queue is quiet at first, then each thread begins offering values and draining the queue when it exceeds a threshold. This captures the producer/consumer style interleavings that appear in real concurrent programs.

### executor-queue

Two threads share a queue that feeds a single-thread executor. The queue is warm-up-only initially; later the threads begin submitting tasks and draining the queue when it fills. This approximates real executor and task-management races, where the conflict depends on the queue reaching a later run state.

The entire point of this suite is not to create a new “known-failing” detector, but to stress the existing first-iteration variant and discover whether it breaks on late-state, realistic concurrency patterns.

## Run a single module with the original tool

```bash
cd /home/rafid/tsvd-demo
mvn -q -f TSVD4J-original/pom.xml install
mvn -q -f benchmarks/collection-threshold test tsvd4j:tsvd4j
```

## Run the same module with the first-iteration tool

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

The suite records whether the original tool and the first-iteration variant both detect the conflict, or whether the first-iteration variant misses it.
