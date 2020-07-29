package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.RulePornRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RulePornRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        RulePornRipper ripper = new RulePornRipper(new URL("https://ruleporn.com/tosh/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://ruleporn.com/tosh/");
        RulePornRipper ripper = new RulePornRipper(url);
        Assertions.assertEquals("tosh", ripper.getGID(url));
    }
}