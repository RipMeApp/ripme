package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BooruRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BooruRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        BooruRipper ripper = new BooruRipper(new URL("http://xbooru.com/index.php?page=post&s=list&tags=furry"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("http://xbooru.com/index.php?page=post&s=list&tags=furry");
        BooruRipper ripper = new BooruRipper(url);
        Assertions.assertEquals("furry", ripper.getGID(url));
    }
}