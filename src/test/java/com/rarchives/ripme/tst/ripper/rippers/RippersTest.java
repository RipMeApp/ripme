package com.rarchives.ripme.tst.ripper.rippers;

import java.io.File;

import junit.framework.TestCase;

public class RippersTest extends TestCase {

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
