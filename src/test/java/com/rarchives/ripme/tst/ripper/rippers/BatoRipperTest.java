package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BatoRipper;

public class BatoRipperTest extends RippersTest {
    public void testRip() throws IOException {
        BatoRipper ripper = new BatoRipper(new URL("https://bato.to/chapter/1207152"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://bato.to/chapter/1207152");
        BatoRipper ripper = new BatoRipper(url);
        assertEquals("1207152", ripper.getGID(url));
    }

    public void testGetAlbumTitle() throws IOException {
        URL url = new URL("https://bato.to/chapter/1207152");
        BatoRipper ripper = new BatoRipper(url);
        assertEquals("bato_1207152_I_Messed_Up_by_Teaching_at_a_Black_Gyaru_School!_Ch.2", ripper.getAlbumTitle(url));
    }
}
