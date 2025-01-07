package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.AllporncomicRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AllporncomicRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testAlbum1() throws IOException, URISyntaxException {
        AllporncomicRipper ripper = new AllporncomicRipper(
                new URI("https://allporncomic.com/porncomic/dnd-pvp-dungeons-dragons-fred-perry/1-dnd-pvp").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testAlbum2() throws IOException, URISyntaxException {
        AllporncomicRipper ripper = new AllporncomicRipper(
                new URI("https://allporncomic.com/porncomic/hinatas-addiction-boruto-burgersnshakesa/3-hinatas-addiction/")
                        .toURL());
        testRipper(ripper);
    }

}
