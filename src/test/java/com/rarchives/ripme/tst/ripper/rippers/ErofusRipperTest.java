package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ErofusRipper;

public class ErofusRipperTest extends RippersTest {
    public void testRip() throws IOException {
        ErofusRipper ripper = new ErofusRipper(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        ErofusRipper ripper = new ErofusRipper(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1"));
        assertEquals("be-story-club-comics", ripper.getGID(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1")));
    }
}
