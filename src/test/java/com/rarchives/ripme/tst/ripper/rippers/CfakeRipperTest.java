package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.CfakeRipper;

public class CfakeRipperTest extends RippersTest {
    public void testRip() throws IOException {
        CfakeRipper ripper = new CfakeRipper(new URL("http://cfake.com/picture/Zooey_Deschanel/1264"));
        testRipper(ripper);
    }
}
