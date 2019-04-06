package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ErofusRipper;

public class CfakeRipperTest extends RippersTest {
    public void testRip() throws IOException {
        ErofusRipper ripper = new ErofusRipper(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1"));
        testRipper(ripper);
    }
}
