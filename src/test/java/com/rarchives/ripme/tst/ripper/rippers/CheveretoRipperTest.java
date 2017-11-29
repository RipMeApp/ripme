package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.CheveretoRipper;

public class CheveretoRipperTest extends RippersTest {
    public void testHushpix() throws IOException {
        CheveretoRipper ripper = new CheveretoRipper(new URL("https://hushpix.com/album/gKcu"));
        testRipper(ripper);
    }

    public void testTagFox() throws IOException {
        CheveretoRipper ripper = new CheveretoRipper(new URL("http://tag-fox.com/album/Thjb"));
        testRipper(ripper);
    }
}
