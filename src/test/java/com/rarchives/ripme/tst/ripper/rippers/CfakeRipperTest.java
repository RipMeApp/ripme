package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.CfakeRipper;

public class CfakeRipperTest extends RippersTest {
    public void testRip() throws IOException, URISyntaxException {
        CfakeRipper ripper = new CfakeRipper(new URI("https://cfake.com/images/celebrity/Zooey_Deschanel/1264").toURL());
        testRipper(ripper);
    }
}
