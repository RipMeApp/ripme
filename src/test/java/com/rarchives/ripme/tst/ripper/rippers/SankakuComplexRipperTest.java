package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SankakuComplexRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SankakuComplexRipperTest extends RippersTest {
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/257")
    public void testSankakuChanRip() throws IOException, URISyntaxException {
        SankakuComplexRipper ripper = new SankakuComplexRipper(
                new URI("https://chan.sankakucomplex.com/?tags=cleavage").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/257")
    public void testSankakuIdolRip() throws IOException, URISyntaxException {
        SankakuComplexRipper ripper = new SankakuComplexRipper(
                new URI("https://idol.sankakucomplex.com/?tags=meme_%28me%21me%21me%21%29_%28cosplay%29").toURL());
        testRipper(ripper);
    }

    @Test
    public void testgetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://idol.sankakucomplex.com/?tags=meme_%28me%21me%21me%21%29_%28cosplay%29").toURL();
        SankakuComplexRipper ripper = new SankakuComplexRipper(url);
        Assertions.assertEquals("idol._meme_(me!me!me!)_(cosplay)", ripper.getGID(url));
    }

    @Test
    public void testgetSubDomain() throws IOException, URISyntaxException {
        URL url = new URI("https://idol.sankakucomplex.com/?tags=meme_%28me%21me%21me%21%29_%28cosplay%29").toURL();
        SankakuComplexRipper ripper = new SankakuComplexRipper(url);
        Assertions.assertEquals("idol.", ripper.getSubDomain(url));
    }
}
