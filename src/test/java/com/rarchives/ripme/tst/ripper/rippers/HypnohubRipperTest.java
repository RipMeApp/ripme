package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HypnohubRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HypnohubRipperTest extends RippersTest {
    @Test
    @Disabled("wants a hunman")
    public void testRip() throws IOException, URISyntaxException {
        URL poolURL = new URI("http://hypnohub.net/pool/show/2303").toURL();
        URL postURL = new URI("http://hypnohub.net/post/show/63464/black_hair-bracelet-collar-corruption-female_only-").toURL();
        HypnohubRipper ripper = new HypnohubRipper(poolURL);
        testRipper(ripper);
        ripper = new HypnohubRipper(postURL);
        testRipper(ripper);
    }
    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL poolURL = new URI("http://hypnohub.net/pool/show/2303").toURL();
        HypnohubRipper ripper = new HypnohubRipper(poolURL);
        Assertions.assertEquals("2303", ripper.getGID(poolURL));

        URL postURL = new URI("http://hypnohub.net/post/show/63464/black_hair-bracelet-collar-corruption-female_only-").toURL();
        Assertions.assertEquals("63464_black_hair-bracelet-collar-corruption-female_only-", ripper.getGID(postURL));
    }
}
