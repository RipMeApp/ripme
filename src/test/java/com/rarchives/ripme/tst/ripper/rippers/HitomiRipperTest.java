package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HitomiRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HitomiRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testRip() throws IOException {
        HitomiRipper ripper = new HitomiRipper(new URL("https://hitomi.la/galleries/975973.html"));
        testRipper(ripper);
        assertTrue(ripper.getGID(new URL("https://hitomi.la/galleries/975973.html")).equals("975973"));
    }
}
