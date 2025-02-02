package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.HitomiRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HitomiRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testRip() throws IOException, URISyntaxException {
        HitomiRipper ripper = new HitomiRipper(new URI("https://hitomi.la/galleries/975973.html").toURL());
        testRipper(ripper);
        Assertions.assertTrue(ripper.getGID(new URI("https://hitomi.la/galleries/975973.html").toURL()).equals("975973"));
    }
}
