package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.PahealRipper;
import org.junit.jupiter.api.Test;

public class PahealRipperTest extends RippersTest {
    @Test
    public void testPahealRipper() throws IOException, URISyntaxException {
        // a photo set
        PahealRipper ripper = new PahealRipper(new URI("http://rule34.paheal.net/post/list/bimbo/1").toURL());
        testRipper(ripper);
    }
}