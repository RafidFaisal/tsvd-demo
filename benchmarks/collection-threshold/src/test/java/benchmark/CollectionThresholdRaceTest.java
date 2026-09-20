package benchmark;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CollectionThresholdRaceTest {

    private static final class SharedListState {
        final List<Integer> values = new ArrayList<>();

        void warmup() {
            for (int i = 0; i < 10_000; i++) {
                values.add(i);
            }
            values.clear();
        }

        void mutate(int value) {
            values.add(value);
            if (values.size() > 8) {
                values.clear();
            }
        }
    }

    @Test
    public void raceAppearsAfterWarmupThreshold() throws Exception {
        SharedListState state = new SharedListState();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService service = Executors.newFixedThreadPool(2);

        Runnable r1 = () -> {
            try {
                start.await();
                for (int i = 0; i < 30; i++) {
                    if (i < 5) {
                        state.warmup();
                    } else {
                        state.mutate(1);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable r2 = () -> {
            try {
                start.await();
                for (int i = 0; i < 30; i++) {
                    if (i < 5) {
                        state.warmup();
                    } else {
                        state.mutate(2);
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
