package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BatoRipper;

<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class BatoRipperTest extends RippersTest {

    @Test
<<<<<<< HEAD
=======
    @Disabled("cloudlare? gets unavailable in test but works in browser")
>>>>>>> upstream/master
    public void testRip() throws IOException {
        BatoRipper ripper = new BatoRipper(new URL("https://bato.to/chapter/1207152"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://bato.to/chapter/1207152");
        BatoRipper ripper = new BatoRipper(url);
<<<<<<< HEAD
        assertEquals("1207152", ripper.getGID(url));
    }

    @Test
    public void testGetAlbumTitle() throws IOException {
        URL url = new URL("https://bato.to/chapter/1207152");
        BatoRipper ripper = new BatoRipper(url);
        assertEquals("bato_1207152_I_Messed_Up_by_Teaching_at_a_Black_Gyaru_School!_Ch.2", ripper.getAlbumTitle(url));
=======
        Assertions.assertEquals("1207152", ripper.getGID(url));
    }

    @Test
    @Disabled("cloudlare? gets unavailable in test but works in browser")
    public void testGetAlbumTitle() throws IOException {
        URL url = new URL("https://bato.to/chapter/1207152");
        BatoRipper ripper = new BatoRipper(url);
        Assertions.assertEquals("bato_1207152_I_Messed_Up_by_Teaching_at_a_Black_Gyaru_School!_Ch.2", ripper.getAlbumTitle(url));
>>>>>>> upstream/master
    }
}
