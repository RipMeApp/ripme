package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PicstatioRipper;
import org.junit.jupiter.api.Test;

public class PicstatioRipperTest extends RippersTest {

    public void testRip() throws IOException {
        PicstatioRipper ripper = new PicstatioRipper(new URL("https://www.picstatio.com/aerial-view-wallpapers"));
        testRipper(ripper);
    }
    @Test
    public void testGID() throws IOException {
        PicstatioRipper ripper = new PicstatioRipper(new URL("https://www.picstatio.com/aerial-view-wallpapers"));
        assertEquals("aerial-view-wallpapers", ripper.getGID(new URL("https://www.picstatio.com/aerial-view-wallpapers")));
    }
}