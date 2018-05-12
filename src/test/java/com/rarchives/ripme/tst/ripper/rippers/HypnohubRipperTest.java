package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HypnohubRipper;

public class HypnohubRipperTest extends RippersTest {
    public void testRip() throws IOException {
        URL poolURL = new URL("http://hypnohub.net/pool/show/2303");
        URL postURL = new URL("http://hypnohub.net/post/show/63464/black_hair-bracelet-collar-corruption-female_only-");
        HypnohubRipper ripper = new HypnohubRipper(poolURL);
        testRipper(ripper);
        ripper = new HypnohubRipper(postURL);
        testRipper(ripper);
    }
    public void testGetGID() throws IOException {
        URL poolURL = new URL("http://hypnohub.net/pool/show/2303");
        HypnohubRipper ripper = new HypnohubRipper(poolURL);
        assertEquals("2303", ripper.getGID(poolURL));

        URL postURL = new URL("http://hypnohub.net/post/show/63464/black_hair-bracelet-collar-corruption-female_only-");
        assertEquals("63464_black_hair-bracelet-collar-corruption-female_only-", ripper.getGID(postURL));
    }
}
