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

        for (int i = 0; i < 10; i++) {
            appenderImpl.addAppender(new AppenderAttachableImpl.Appender());
        }

        Thread remover = new Thread(() -> runRemover(appenderImpl, iterations, finished, failure), "remover");
        Thread modifier = new Thread(() -> runModifier(appenderImpl, iterations, finished, failure), "modifier");

        long startNanos = System.nanoTime();
        remover.start();
        modifier.start();

        boolean completed = finished.await(timeoutSeconds, TimeUnit.SECONDS);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        System.out.println("BENCHMARK iterations=" + iterations
            + " elapsedMillis=" + elapsedMillis
            + " completed=" + completed);

        if (!completed) {
            remover.interrupt();
            modifier.interrupt();
            throw new AssertionError("worker threads did not finish within " + timeoutSeconds + " seconds");
        }
        if (failure.get() != null) {
            throw new AssertionError("worker thread failed", failure.get());
        }
    }

    private static void runRemover(AppenderAttachableImpl appenderImpl, int iterations,
            CountDownLatch finished, AtomicReference<Throwable> failure) {
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
    }

    private static void runModifier(AppenderAttachableImpl appenderImpl, int iterations,
            CountDownLatch finished, AtomicReference<Throwable> failure) {
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
    }
}
