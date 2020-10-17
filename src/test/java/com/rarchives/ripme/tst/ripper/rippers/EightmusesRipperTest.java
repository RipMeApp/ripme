package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EightmusesRipper;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class EightmusesRipperTest extends RippersTest {
    @Test
    public void testEightmusesAlbum() throws IOException {
        // A simple image album
        EightmusesRipper ripper = new EightmusesRipper(new URL("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore"));
        testRipper(ripper);
        // Test the new url format
        ripper = new EightmusesRipper(new URL("https://www.8muses.com/comics/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore"));
        testRipper(ripper);
        // Test pages with subalbums
        ripper = new EightmusesRipper(new URL("https://www.8muses.com/comix/album/Blacknwhitecomics_com-Comix/BlacknWhiteComics/The-Mayor"));
        testRipper(ripper);
    }
    @Test
    public void testGID() throws IOException {
        EightmusesRipper ripper = new EightmusesRipper(new URL("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore"));
<<<<<<< HEAD
        assertEquals("Affect3D-Comics", ripper.getGID(new URL("https://www.8muses.com/comics/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore")));
=======
        Assertions.assertEquals("Affect3D-Comics", ripper.getGID(new URL("https://www.8muses.com/comics/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore")));
>>>>>>> upstream/master
    }
    @Test
    public void testGetSubdir() throws IOException {
        EightmusesRipper ripper = new EightmusesRipper(new URL("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore"));
<<<<<<< HEAD
        assertEquals("After-Party-Issue-1", ripper.getSubdir("After Party - Issue 1"));
=======
        Assertions.assertEquals("After-Party-Issue-1", ripper.getSubdir("After Party - Issue 1"));
>>>>>>> upstream/master
    }
}