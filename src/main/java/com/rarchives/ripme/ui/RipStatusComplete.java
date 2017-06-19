package com.rarchives.ripme.ui;

import java.io.File;
import java.io.IOException;

public class RipStatusComplete {
    File dir = null;
    int count = 0;

    public RipStatusComplete(File dir) {
        this.dir = dir;
        this.count = 1;
    }

    public RipStatusComplete(File dir, int count) {
        this.dir = dir;
        this.count = count;
    }

    public String getDir() {
        String result;
        try {
            result = this.dir.getCanonicalPath();
        } catch (IOException e) {
            result = this.dir.toString();
        }
        return result;
    }
}
