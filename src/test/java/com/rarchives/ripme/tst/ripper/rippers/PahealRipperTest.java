package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PahealRipper;
import org.junit.jupiter.api.Test;

public class PahealRipperTest extends RippersTest {
    @Test
    public void testPahealRipper() throws IOException {
        // a photo set
        PahealRipper ripper = new PahealRipper(new URL("http://rule34.paheal.net/post/list/bimbo/1"));
        testRipper(ripper);
    }
}