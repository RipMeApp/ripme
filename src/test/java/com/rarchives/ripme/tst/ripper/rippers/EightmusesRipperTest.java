package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EightmusesRipper;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals("Affect3D-Comics", ripper.getGID(new URL("https://www.8muses.com/comics/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore")));
    }
    @Test
    public void testGetSubdir() throws IOException {
        EightmusesRipper ripper = new EightmusesRipper(new URL("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore"));
        Assertions.assertEquals("After-Party-Issue-1", ripper.getSubdir("After Party - Issue 1"));
    }
}