package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BcfakesRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BcfakesRipperTest extends RippersTest {
    @Test
    @Disabled("21/06/2018 This test was disbaled as the site has experienced notable downtime")
    public void testRip() throws IOException {
        BcfakesRipper ripper = new BcfakesRipper(new URL("http://www.bcfakes.com/celebritylist/olivia-wilde/"));
        testRipper(ripper);
    }
}
