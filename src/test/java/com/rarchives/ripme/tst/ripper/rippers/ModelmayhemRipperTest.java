package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ModelmayhemRipper;

public class ModelmayhemRipperTest extends RippersTest {
    public void testModelmayhemRip() throws IOException {
        ModelmayhemRipper ripper = new ModelmayhemRipper(new URL("https://www.modelmayhem.com/portfolio/520206/viewall"));
        testRipper(ripper);
    }
}
