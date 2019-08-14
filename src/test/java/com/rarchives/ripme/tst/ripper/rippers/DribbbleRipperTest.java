package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.DribbbleRipper;
import org.junit.jupiter.api.Test;

public class DribbbleRipperTest extends RippersTest {
    @Test
    public void testDribbbleRip() throws IOException {
        DribbbleRipper ripper = new DribbbleRipper(new URL("https://dribbble.com/typogriff"));
        testRipper(ripper);
    }
}
