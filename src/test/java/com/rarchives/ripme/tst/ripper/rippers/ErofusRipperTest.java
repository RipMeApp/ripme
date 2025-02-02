package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.ErofusRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class ErofusRipperTest extends RippersTest {
    @Test
    @Tag("flaky")   // if url does not exist, erofusripper test ends in out of memory
    public void testRip() throws IOException, URISyntaxException {
        ErofusRipper ripper = new ErofusRipper(new URI("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testGetGID() throws IOException, URISyntaxException {
        ErofusRipper ripper = new ErofusRipper(new URI("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1").toURL());
        Assertions.assertEquals("be-story-club-comics", ripper.getGID(new URI("https://www.erofus.com/comics/be-story-club-comics/a-kiss/issue-1").toURL()));
    }
}
