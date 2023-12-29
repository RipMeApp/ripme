package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.HentaidudeRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HentaidudeRipperTest extends RippersTest{

    @Test
    public void testRip() throws IOException, URISyntaxException {
        HentaidudeRipper ripper = new HentaidudeRipper(new URI("https://hentaidude.com/girlfriends-4ever-dlc-2/").toURL());
        testRipper(ripper);

    }



}