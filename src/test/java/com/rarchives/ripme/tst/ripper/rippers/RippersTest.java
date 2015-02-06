package com.rarchives.ripme.tst.ripper.rippers;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.utils.Utils;

/**
 * Contains helper methods for testing rippers.
 */
public class RippersTest extends TestCase {

    public final Logger logger = Logger.getLogger(RippersTest.class);

    /** Dummy test to make JUnit not complain */
    public void test() {
        assert(true);
    }

    protected void testRipper(AbstractRipper ripper) {
        try {
            Utils.setConfigInteger("page.timeout", 5 * 1000);
            ripper.setup();
            ripper.markAsTest();
            ripper.rip();
            for (File f : ripper.getWorkingDir().listFiles()) {
                System.err.println(f.toString());
            }
            assertTrue("Failed to download files from " + ripper.getURL(), ripper.getWorkingDir().listFiles().length >= 1);
        } catch (IOException e) {
            if (e.getMessage().contains("Ripping interrupted")) {
                // We expect some rips to get interrupted
            }
            else {
                e.printStackTrace();
                fail("Failed to rip " + ripper.getURL() + " : " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to rip " + ripper.getURL() + " : " + e.getMessage());
        }
        finally {
            deleteDir(ripper.getWorkingDir());
        }
    }

    /** File extensions that are safe to delete. */
    private static final String[] SAFE_EXTENSIONS =
        {"png", "jpg",  "jpeg", "gif",
         "mp4", "webm", "mov",  "mpg", "mpeg",
         "txt", "log", "php"};

    /** Recursively deletes a directory */
    protected void deleteDir(File dir) {
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

}
