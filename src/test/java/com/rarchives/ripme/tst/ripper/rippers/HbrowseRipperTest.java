package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HbrowseRipper;

public class HbrowseRipperTest extends RippersTest {
    public void testPahealRipper() throws IOException {
        HbrowseRipper ripper = new HbrowseRipper(new URL("https://www.hbrowse.com/21013/c00001"));
        testRipper(ripper);
    }
}