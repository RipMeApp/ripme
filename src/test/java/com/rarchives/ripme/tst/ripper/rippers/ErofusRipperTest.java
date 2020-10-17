package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ErofusRipper;
<<<<<<< HEAD

public class ErofusRipperTest extends RippersTest {
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ErofusRipperTest extends RippersTest {
    @Test
>>>>>>> upstream/master
    public void testRip() throws IOException {
        ErofusRipper ripper = new ErofusRipper(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1"));
        testRipper(ripper);
    }

<<<<<<< HEAD
    public void testGetGID() throws IOException {
        ErofusRipper ripper = new ErofusRipper(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1"));
        assertEquals("be-story-club-comics", ripper.getGID(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1")));
=======
    @Test
    public void testGetGID() throws IOException {
        ErofusRipper ripper = new ErofusRipper(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1"));
        Assertions.assertEquals("be-story-club-comics", ripper.getGID(new URL("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1")));
>>>>>>> upstream/master
    }
}
