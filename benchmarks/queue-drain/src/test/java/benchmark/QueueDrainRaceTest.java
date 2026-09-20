package benchmark;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueDrainRaceTest {

    private static final class SharedQueueState {
        final LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

        void warmup() {
            for (int i = 0; i < 10_000; i++) {
                queue.offer(i);
            }
            queue.clear();
        }

        void addAndDrain(int value) {
            queue.offer(value);
            if (queue.size() > 8) {
                while (!queue.isEmpty()) {
                    queue.poll();
                }
            }
        }
    }

    @Test
    public void raceAppearsAfterDrainThreshold() throws Exception {
        SharedQueueState state = new SharedQueueState();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService service = Executors.newFixedThreadPool(2);

        Runnable r1 = () -> {
            try {
                start.await();
                for (int i = 0; i < 30; i++) {
                    if (i < 5) {
                        state.warmup();
                    } else {
                        state.addAndDrain(1);
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
                        state.addAndDrain(2);
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
