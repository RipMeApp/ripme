package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.BcfakesRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BcfakesRipperTest extends RippersTest {
    @Test
    @Disabled("21/06/2018 This test was disbaled as the site has experienced notable downtime")
    public void testRip() throws IOException, URISyntaxException {
        BcfakesRipper ripper = new BcfakesRipper(new URI("http://www.bcfakes.com/celebritylist/olivia-wilde/").toURL());
        testRipper(ripper);
    }
}
