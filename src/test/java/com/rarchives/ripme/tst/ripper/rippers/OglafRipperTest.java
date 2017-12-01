package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.OglafRipper;

public class OglafRipperTest extends RippersTest {
    public void testRip() throws IOException {
        OglafRipper ripper = new OglafRipper(new URL("http://oglaf.com/plumes/"));
        testRipper(ripper);
    }
}