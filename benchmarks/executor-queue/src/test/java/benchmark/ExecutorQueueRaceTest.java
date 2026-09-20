package benchmark;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ExecutorQueueRaceTest {

    private static final class SharedExecutorState {
        final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        void warmup() {
            for (int i = 0; i < 2_000; i++) {
                queue.offer(() -> { });
            }
            queue.clear();
        }

        void submitAndDrain(Runnable task) {
            queue.offer(task);
            if (queue.size() > 6) {
                while (!queue.isEmpty()) {
                    executor.submit(queue.poll());
                }
            }
        }
    }

    @Test
    public void raceAppearsAfterExecutorQueueFills() throws Exception {
        SharedExecutorState state = new SharedExecutorState();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService service = Executors.newFixedThreadPool(2);

        Runnable r1 = () -> {
            try {
                start.await();
                for (int i = 0; i < 25; i++) {
                    if (i < 5) {
                        state.warmup();
                    } else {
                        state.submitAndDrain(() -> { });
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
                    if (i < 5) {
                        state.warmup();
                    } else {
                        state.submitAndDrain(() -> { });
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
        state.executor.shutdownNow();
    }
}
