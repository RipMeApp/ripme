package com.rarchives.ripme.ui;

import java.nio.file.Path;

public class RipStatusComplete {
    Path dir = null;
    int count = 0;

    public RipStatusComplete(Path dir) {
        this.dir = dir;
        this.count = 1;
    }

    public RipStatusComplete(Path dir, int count) {
        this.dir = dir;
        this.count = count;
    }

    public String getDir() {
        return this.dir.toString();
    }
}
