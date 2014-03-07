package com.rarchives.ripme.tst.ripper.rippers;

import java.io.File;

import junit.framework.TestCase;

public class RippersTest extends TestCase {

    // Flag for avoiding downloading content with every unit test
    public final boolean DOWNLOAD_CONTENT = false;

    public void testNothing() {
        // Avoid complaints about no test cases in this file.
        assert(true);
    }

    protected void deleteDir(File dir) {
        return;
        /*
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f);
            }
            f.delete();
        }
        dir.delete();
        //*/
    }

}
