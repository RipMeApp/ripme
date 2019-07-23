package com.rarchives.ripme.tst.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.ChanRipper;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Utils;

/**
 * Contains helper methods for testing rippers.
 */
public class RippersTest {

    private final Logger logger = Logger.getLogger(RippersTest.class);

    public void testStub() {
        assertTrue("RippersTest must contain at lease one test.", true);
    }

    void testRipper(AbstractRipper ripper) {
        try {
            // Turn on Debug logging
            ((ConsoleAppender) Logger.getRootLogger().getAppender("stdout")).setThreshold(Level.DEBUG);

            // Decrease timeout
            Utils.setConfigInteger("page.timeout", 20 * 1000);

            ripper.setup();
            ripper.markAsTest();
            ripper.rip();
            assertTrue("Failed to download a single file from " + ripper.getURL(),
                    ripper.getWorkingDir().listFiles().length >= 1);
        } catch (IOException e) {
            if (e.getMessage().contains("Ripping interrupted")) {
                // We expect some rips to get interrupted
            } else {
                e.printStackTrace();
                fail("Failed to rip " + ripper.getURL() + " : " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to rip " + ripper.getURL() + " : " + e.getMessage());
        } finally {
            deleteDir(ripper.getWorkingDir());
        }
    }

    // We have a special test for chan rippers because we can't assume that content
    // will be downloadable, as content
    // is often removed within mere hours of it being posted. So instead of trying
    // to download any content we just check
    // that we found links to it
    void testChanRipper(ChanRipper ripper) {
        try {
            // Turn on Debug logging
            ((ConsoleAppender) Logger.getRootLogger().getAppender("stdout")).setThreshold(Level.DEBUG);

            // Decrease timeout
            Utils.setConfigInteger("page.timeout", 20 * 1000);

            ripper.setup();
            ripper.markAsTest();
            List<String> foundUrls = ripper.getURLsFromPage(ripper.getFirstPage());
            assertTrue("Failed to find single url on page " + ripper.getURL(), foundUrls.size() >= 1);
        } catch (IOException e) {
            if (e.getMessage().contains("Ripping interrupted")) {
                // We expect some rips to get interrupted
            } else {
                e.printStackTrace();
                fail("Failed to rip " + ripper.getURL() + " : " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to rip " + ripper.getURL() + " : " + e.getMessage());
        } finally {
            deleteDir(ripper.getWorkingDir());
        }
    }

    /** File extensions that are safe to delete. */
    private static final String[] SAFE_EXTENSIONS = { "png", "jpg", "jpeg", "gif", "mp4", "webm", "mov", "mpg", "mpeg",
            "txt", "log", "php" };

    /** Recursively deletes a directory */
    void deleteDir(File dir) {
        if (!dir.getName().contains("_")) {
            // All ripped albums contain an underscore
            // Don't delete an album if it doesn't have an underscore
            return;
        }
        for (File f : dir.listFiles()) {
            boolean safe = false;
            for (String ext : SAFE_EXTENSIONS) {
                safe |= f.getAbsolutePath().toLowerCase().endsWith("." + ext);
            }
            if (!safe) {
                // Found a file we shouldn't delete! Stop deleting immediately.
                return;
            }
            if (f.isDirectory()) {
                deleteDir(f);
            }
            f.delete();
        }
        dir.delete();
    }

    void deleteSubdirs(File workingDir) {
        for (File f : workingDir.listFiles()) {
            if (f.isDirectory()) {
                for (File sf : f.listFiles()) {
                    logger.debug("Deleting " + sf);
                    sf.delete();
                }
                logger.debug("Deleting " + f);
                f.delete();
            }
        }
    }

    @Deprecated
    void assertEquals(String expected, String actual) {
        Assertions.assertEquals(expected, actual);
    }

    @Deprecated
    void assertEquals(String message, String expected, String actual) {
        Assertions.assertEquals(expected, actual, message);
    }

    @Deprecated
    void assertEquals(Object expected, Object actual) {
        Assertions.assertEquals(expected, actual);
    }

    @Deprecated
    void fail(String message) {
        Assertions.fail(message);
    }

    @Deprecated
    void assertTrue(boolean condition) {
        Assertions.assertTrue(condition);
    }

    @Deprecated
    void assertTrue(String failMessage, boolean condition) {
        Assertions.assertTrue(condition, failMessage);
    }

    @Deprecated
    void assertFalse(String message, boolean condition) {
        Assertions.assertFalse(condition, message);
    }

    @Deprecated
    void assertNull(Object actual) {
        Assertions.assertNull(actual);
    }

    @Deprecated
    void assertNotNull(String message, Object actual) {
        Assertions.assertNotNull(actual, message);
    }

}
