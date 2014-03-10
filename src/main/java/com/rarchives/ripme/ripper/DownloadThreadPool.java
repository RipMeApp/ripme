package com.rarchives.ripme.ripper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rarchives.ripme.utils.Utils;

/**
 * Simple wrapper around a FixedThreadPool.
 */
public class DownloadThreadPool {

    private static final Logger logger = Logger.getLogger(DownloadThreadPool.class);
    private ExecutorService threadPool = null;

    public DownloadThreadPool() {
        initialize("Main");
    }
    
    public DownloadThreadPool(String threadPoolName) {
        initialize(threadPoolName);
    }

    private void initialize(String threadPoolName) {
        int threads = Utils.getConfigInteger("threads.size", 10);
        logger.debug("Initializing " + threadPoolName + " thread pool with " + threads + " threads");
        threadPool = Executors.newFixedThreadPool(threads);
    }

    public void addThread(Thread t) {
        threadPool.execute(t);
    }

    public void waitForThreads() {
        threadPool.shutdown();
        try {
            // XXX What if some rips take longer than 120 seconds to complete?
            threadPool.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("[!] Interrupted while waiting for threads to finish: ", e);
        }
    }
}
