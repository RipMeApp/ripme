package com.rarchives.ripme.ripper;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.rarchives.ripme.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple wrapper around a FixedThreadPool.
 */
public class DownloadThreadPool {

    private static final Logger logger = LogManager.getLogger(DownloadThreadPool.class);
    private ThreadPoolExecutor threadPool = null;

    public DownloadThreadPool(String threadPoolName) {
        initialize(threadPoolName);
    }
    
    /**
     * Initializes the threadpool.
     * @param threadPoolName Name of the threadpool.
     */
    private void initialize(String threadPoolName) {
        int threads = Utils.getConfigInteger("threads.size", 10);
        logger.debug("Initializing " + threadPoolName + " thread pool with " + threads + " threads");
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
    }
    /**
     * For adding threads to execution pool.
     * @param t
     *      Thread to be added.
     */
    public void addThread(Runnable t) {
        threadPool.execute(t);
    }

    /**
     * Tries to shutdown threadpool.
     */
    public void waitForThreads() {
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(3600, TimeUnit.SECONDS);
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
}
