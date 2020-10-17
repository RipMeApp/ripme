package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.HypnohubRipper;
<<<<<<< HEAD

public class HypnohubRipperTest extends RippersTest {
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HypnohubRipperTest extends RippersTest {
    @Test
    @Disabled("wants a hunman")
>>>>>>> upstream/master
    public void testRip() throws IOException {
        URL poolURL = new URL("http://hypnohub.net/pool/show/2303");
        URL postURL = new URL("http://hypnohub.net/post/show/63464/black_hair-bracelet-collar-corruption-female_only-");
        HypnohubRipper ripper = new HypnohubRipper(poolURL);
        testRipper(ripper);
        ripper = new HypnohubRipper(postURL);
        testRipper(ripper);
    }
<<<<<<< HEAD
    public void testGetGID() throws IOException {
        URL poolURL = new URL("http://hypnohub.net/pool/show/2303");
        HypnohubRipper ripper = new HypnohubRipper(poolURL);
        assertEquals("2303", ripper.getGID(poolURL));

        URL postURL = new URL("http://hypnohub.net/post/show/63464/black_hair-bracelet-collar-corruption-female_only-");
        assertEquals("63464_black_hair-bracelet-collar-corruption-female_only-", ripper.getGID(postURL));
=======
    @Test
    public void testGetGID() throws IOException {
        URL poolURL = new URL("http://hypnohub.net/pool/show/2303");
        HypnohubRipper ripper = new HypnohubRipper(poolURL);
        Assertions.assertEquals("2303", ripper.getGID(poolURL));

        URL postURL = new URL("http://hypnohub.net/post/show/63464/black_hair-bracelet-collar-corruption-female_only-");
        Assertions.assertEquals("63464_black_hair-bracelet-collar-corruption-female_only-", ripper.getGID(postURL));
>>>>>>> upstream/master
    }
}
