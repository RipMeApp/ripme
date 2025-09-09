package com.rarchives.ripme.ripper;

import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.rarchives.ripme.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple wrapper around a FixedThreadPool.
 */
public class DownloadThreadPool {

    private static final Logger logger = LogManager.getLogger(DownloadThreadPool.class);
    private ThreadPoolExecutor threadPool = null;
    private final AtomicLong scheduledThreadCount = new AtomicLong(0);
    private final String name;

    public DownloadThreadPool(String threadPoolName) {
        int threads = Utils.getConfigInteger("threads.size", 10);
        logger.debug("Initializing " + threadPoolName + " thread pool with " + threads + " threads");
        this.name = threadPoolName;
        this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads, Thread.ofVirtual().factory());
    }

    /**
     * For adding threads to execution pool.
     * @param t
     *      Thread to be added.
     */
    public void addThread(Runnable t) {
        logger.trace("addThread called; name: {}, scheduledThreadCount: {}", name, scheduledThreadCount);
        scheduledThreadCount.incrementAndGet();
        threadPool.execute(t);
    }

    public void setThreadPoolSize(int threads) {
        logger.debug("Setting thread pool size to {}", threads);
        if (threads > threadPool.getMaximumPoolSize()) {
            threadPool.setMaximumPoolSize(threads);
            threadPool.setCorePoolSize(threads);
        } else {
            threadPool.setCorePoolSize(threads);
            threadPool.setMaximumPoolSize(threads);
        }
    }

    /**
     * Tries to shutdown threadpool.
     */
    public void waitForThreads(Supplier<Boolean> isFinishedQueueing, URL url) {
        logger.trace("waitForThreads called; name: {}; url: {}", name, url);
        while (!isFinishedQueueing.get()) {
            logger.trace("waiting for items to finish queueing; name: {}; url: {}", name, url);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.trace("sleep interrupted; name: {}; url: {}", name, url);
                break;
            }
        }

        logger.trace("about to shutdown thread pool. name: {}; url: {}", name, url);
        threadPool.shutdown();
        logger.trace("thread pool shutdown. name: {}; url: {}", name, url);
        try {
            threadPool.awaitTermination(3600, TimeUnit.SECONDS);
            logger.trace("thread pool terminated. name: {}; url: {}", name, url);
        } catch (InterruptedException e) {
            logger.error("[!] Interrupted while waiting for threads to finish: ", e);
        }
    }

    /**
     * Tries to shutdown threadpool.
     */
    public void waitForThreads(int expectedScheduledThreads, AtomicBoolean shouldStop, URL url) {
        logger.trace("waitForThreads called; name: {}; url: {}", name, url);
        while (getScheduledThreadCount() < expectedScheduledThreads && !shouldStop.get()) {
            logger.trace("waiting for scheduled threads to equal expected scheduled threads; name: {}; scheduled: {}; expected: {} url: {}", name, scheduledThreadCount, expectedScheduledThreads, url);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.trace("about to shutdown thread pool. name: {}; url: {}", name, url);
        threadPool.shutdown();
        logger.trace("thread pool shutdown. name: {}; url: {}", name, url);
        try {
            threadPool.awaitTermination(3600, TimeUnit.SECONDS);
            logger.trace("thread pool terminated. name: {}; url: {}", name, url);
        } catch (InterruptedException e) {
            logger.error("[!] Interrupted while waiting for threads to finish: ", e);
        }
    }

    public int getPendingThreadCount() {
        return threadPool.getQueue().size();
    }

    public int getActiveThreadCount() {
        return threadPool.getActiveCount();
    }

    public long getCompletedThreadCount() {
        return threadPool.getCompletedTaskCount();
    }

    public long getScheduledThreadCount() {
        //return threadPool.getTaskCount(); // approximate, bad
        return scheduledThreadCount.get();
    }
}
