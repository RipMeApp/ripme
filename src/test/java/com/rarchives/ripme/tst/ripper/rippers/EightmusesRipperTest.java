package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.EightmusesRipper;

public class EightmusesRipperTest extends RippersTest {
    public void testEightmusesAlbum() throws IOException {
        // A simple image album
        EightmusesRipper ripper = new EightmusesRipper(new URL("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore"));
        testRipper(ripper);
        // Test pages with subalbums
        ripper = new EightmusesRipper(new URL("https://www.8muses.com/comix/album/Blacknwhitecomics_com-Comix/BlacknWhiteComics/The-Mayor"));
        testRipper(ripper);
    }
}