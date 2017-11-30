package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BcfakesRipper;

public class BcfakesRipperTest extends RippersTest {
    public void testRip() throws IOException {
        BcfakesRipper ripper = new BcfakesRipper(new URL("http://www.bcfakes.com/celebritylist/olivia-wilde/"));
        testRipper(ripper);
    }
}
