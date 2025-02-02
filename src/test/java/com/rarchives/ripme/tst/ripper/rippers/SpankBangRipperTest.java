package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.SpankbangRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class SpankBangRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testSpankBangVideo() throws IOException, URISyntaxException {
        SpankbangRipper ripper = new SpankbangRipper(new URI("https://spankbang.com/2a7fh/video/mdb901").toURL());  //most popular video of all time on site; should stay up
        testRipper(ripper);
    }

}
