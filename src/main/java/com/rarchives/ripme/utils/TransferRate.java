package com.rarchives.ripme.utils;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TransferRate {
    private final Deque<ChunkStamp> transferQueue = new ConcurrentLinkedDeque<>();
    private int windowDurationMs = 10000;

    public void addChunk(long bytes) {
        long now = System.currentTimeMillis();
        transferQueue.addFirst(new ChunkStamp(now, bytes));
        removeOldChunks(now);
    }

    public double calculateBytesPerSecond() {
        if (transferQueue.isEmpty()) {
            return 0;
        }
        long totalBytes = 0;
        ChunkStamp oldest = transferQueue.getLast();
        long now = System.currentTimeMillis();
        removeOldChunks(now);
        if (transferQueue.isEmpty()) {
            return 0;
        }
        for (ChunkStamp chunkStamp : transferQueue) {
            totalBytes += chunkStamp.bytes;
        }
        long elapsedMs = now - oldest.timestampMs;
        double elapsedSeconds = (double) elapsedMs / 1000;
        if (elapsedSeconds <= 0) {
            return 0;
        }
        return totalBytes / elapsedSeconds;
    }

    public String formatHumanTransferRate() {
        double bps = calculateBytesPerSecond();
        int giB = 1024 * 1024 * 1024;
        int miB = 1024 * 1024;
        int kiB = 1024;
        if (bps >= giB) {
            return String.format("%.2f GiB/s", bps / giB);
        } else if (bps >= miB) {
            return String.format("%.2f MiB/s", bps / miB);
        } else if (bps >= kiB) {
            return String.format("%.2f KiB/s", bps / kiB);
        } else {
            return String.format("%.2f B/s", bps);
        }
    }

    public void setWindowDurationMs(int windowDurationMs) {
        this.windowDurationMs = windowDurationMs;
    }

    public int getWindowDurationMs() {
        return windowDurationMs;
    }

    private void removeOldChunks(long now) {
        while (!transferQueue.isEmpty() && transferQueue.getLast().timestampMs < now - windowDurationMs) {
            transferQueue.removeLast();
        }
    }

    private static class ChunkStamp {
        long bytes;
        long timestampMs; // millis

        ChunkStamp(long timestampMs, long bytes) {
            this.bytes = bytes;
            this.timestampMs = timestampMs;
        }
    }
}
