package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.AllporncomicRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class AllporncomicRipperTest extends RippersTest {
    @Test
    public void testAlbum() throws IOException {
        AllporncomicRipper ripper = new AllporncomicRipper(new URL("https://allporncomic.com/porncomic/dnd-pvp-dungeons-dragons-fred-perry/1-dnd-pvp"));
        testRipper(ripper);
    }

}
