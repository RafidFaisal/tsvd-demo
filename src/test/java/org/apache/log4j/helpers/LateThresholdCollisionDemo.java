package org.apache.log4j.helpers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class LateThresholdCollisionDemo {

    public static final class ThresholdState {
        private final List<Integer> shared = new ArrayList<>();

        public void warmupOnly() {
            int sum = 0;
            for (int i = 0; i < 100_000; i++) {
                sum += i;
            }
            if (sum < 0) {
                throw new AssertionError("unreachable");
            }
        }

        public void accessShared(int value) {
            shared.add(value);
            if (shared.size() > 10) {
                shared.clear();
            }
        }
    }

    @Test
    public void firstIterationOnlyDelayMissesLateRace() throws Exception {
        final ThresholdState state = new ThresholdState();
        final CountDownLatch start = new CountDownLatch(1);
        final ExecutorService pool = Executors.newFixedThreadPool(2);

        Future<?> t1 = pool.submit(() -> {
            start.await();
            for (int i = 0; i < 20; i++) {
                if (i < 3) {
                    state.warmupOnly();
                    continue;
                }
                state.accessShared(1);
            }
            return null;
        });

        Future<?> t2 = pool.submit(() -> {
            start.await();
            for (int i = 0; i < 20; i++) {
                if (i < 3) {
                    state.warmupOnly();
                    continue;
                }
                state.accessShared(2);
            }
            return null;
        });

        start.countDown();
        t1.get(10, TimeUnit.SECONDS);
        t2.get(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        System.out.println("LateThresholdCollisionDemo: shared mutation starts only in iteration 3+.");
        System.out.println("A first-iteration-only delay cannot detect this race because the first three iterations are warm-up only.");
    }
}
