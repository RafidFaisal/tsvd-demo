package org.apache.log4j.helpers;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AppenderAttachableTest {

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        int iterations = Integer.getInteger("benchmark.iterations", 20);
        long timeoutSeconds = Long.getLong("benchmark.timeoutSeconds", 120L);
        AppenderAttachableImpl appenderImpl = new AppenderAttachableImpl();
        CountDownLatch finished = new CountDownLatch(2);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        // Seed initial elements
        for (int i = 0; i < 10; i++) {
            appenderImpl.addAppender(new AppenderAttachableImpl.Appender());
        }

        Thread t1 = new Thread(() -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    appenderImpl.removeAllAppenders();
                    Thread.sleep(2);
                }
            } catch (Throwable error) {
                failure.compareAndSet(null, error);
            } finally {
                finished.countDown();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    appenderImpl.addAppender(new AppenderAttachableImpl.Appender());
                    appenderImpl.removeAllAppenders();
                    Thread.sleep(2);
                }
            } catch (Throwable error) {
                failure.compareAndSet(null, error);
            } finally {
                finished.countDown();
            }
        });

        long startNanos = System.nanoTime();
        t1.start();
        t2.start();

        boolean completed = finished.await(timeoutSeconds, TimeUnit.SECONDS);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        System.out.println("BENCHMARK iterations=" + iterations
            + " elapsedMillis=" + elapsedMillis
            + " completed=" + completed);

        if (!completed) {
            t1.interrupt();
            t2.interrupt();
            throw new AssertionError("worker threads did not finish within " + timeoutSeconds + " seconds");
        }
        if (failure.get() != null) {
            throw new AssertionError("worker thread failed", failure.get());
        }
    }
}
