package benchmark;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapThresholdRaceTest {

    private static final class SharedMapState {
        final Map<Integer, Integer> values = new HashMap<>();

        void warmup() {
            for (int i = 0; i < 10_000; i++) {
                values.put(i, i);
            }
            values.clear();
        }

        void mutate(int key, int value) {
            values.put(key, value);
            if (values.size() > 16) {
                values.clear();
            }
        }
    }

    @Test
    public void raceAppearsAfterMapGrowthThreshold() throws Exception {
        SharedMapState state = new SharedMapState();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService service = Executors.newFixedThreadPool(2);

        Runnable r1 = () -> {
            try {
                start.await();
                for (int i = 0; i < 25; i++) {
                    if (i < 6) {
                        state.warmup();
                    } else {
                        state.mutate(i, 1);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable r2 = () -> {
            try {
                start.await();
                for (int i = 0; i < 25; i++) {
                    if (i < 6) {
                        state.warmup();
                    } else {
                        state.mutate(i, 2);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        service.submit(r1);
        service.submit(r2);
        start.countDown();
        service.shutdown();
        service.awaitTermination(10, TimeUnit.SECONDS);
    }
}
