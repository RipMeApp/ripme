package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SmuttyRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class SmuttyRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException {
        SmuttyRipper ripper = new SmuttyRipper(new URL("https://smutty.com/user/QUIGON/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://smutty.com/user/QUIGON/");
        SmuttyRipper ripper = new SmuttyRipper(url);
        Assertions.assertEquals("QUIGON", ripper.getGID(url));
    }
}
