package org.apache.log4j.helpers;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class LateResizeCollisionDemo {

    public static final class ResizeState {
        private final Map<Integer, Integer> shared = new HashMap<>();

        public void warmupOnly() {
            int local = 0;
            for (int i = 0; i < 50_000; i++) {
                local += i;
            }
            if (local < 0) {
                throw new AssertionError("unreachable");
            }
        }

        public void accessShared(int key, int value) {
            shared.put(key, value);
            if (shared.size() > 16) {
                shared.clear();
            }
        }
    }

    @Test
    public void firstIterationOnlyDelayMissesResizeRace() throws Exception {
        final ResizeState state = new ResizeState();
        final CountDownLatch start = new CountDownLatch(1);
        final ExecutorService pool = Executors.newFixedThreadPool(2);

        Future<?> t1 = pool.submit(() -> {
            start.await();
            for (int i = 0; i < 25; i++) {
                if (i < 5) {
                    state.warmupOnly();
                    continue;
                }
                state.accessShared(i, 1);
            }
            return null;
        });

        Future<?> t2 = pool.submit(() -> {
            start.await();
            for (int i = 0; i < 25; i++) {
                if (i < 5) {
                    state.warmupOnly();
                    continue;
                }
                state.accessShared(i, 2);
            }
            return null;
        });

        start.countDown();
        t1.get(10, TimeUnit.SECONDS);
        t2.get(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        System.out.println("LateResizeCollisionDemo: shared map mutation starts only in iteration 5+.");
        System.out.println("A first-iteration-only delay misses this race because the map is not used until after warm-up.");
    }
}
