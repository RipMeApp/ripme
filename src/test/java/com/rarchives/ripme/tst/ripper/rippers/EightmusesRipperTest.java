package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.EightmusesRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class EightmusesRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testEightmusesAlbum() throws IOException, URISyntaxException {
        // A simple image album
        EightmusesRipper ripper = new EightmusesRipper(new URI("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore").toURL());
        testRipper(ripper);
        // Test the new url format
        ripper = new EightmusesRipper(new URI("https://www.8muses.com/comics/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore").toURL());
        testRipper(ripper);
        // Test pages with subalbums
        ripper = new EightmusesRipper(new URI("https://www.8muses.com/comix/album/Blacknwhitecomics_com-Comix/BlacknWhiteComics/The-Mayor").toURL());
        testRipper(ripper);
    }
    @Test
    public void testGID() throws IOException, URISyntaxException {
        EightmusesRipper ripper = new EightmusesRipper(new URI("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore").toURL());
        Assertions.assertEquals("Affect3D-Comics", ripper.getGID(new URI("https://www.8muses.com/comics/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore").toURL()));
    }
    @Test
    public void testGetSubdir() throws IOException, URISyntaxException {
        EightmusesRipper ripper = new EightmusesRipper(new URI("https://www.8muses.com/comix/album/Affect3D-Comics/TheDude3DX/Lust-Unleashed-The-Urge-To-Explore").toURL());
        Assertions.assertEquals("After-Party-Issue-1", ripper.getSubdir("After Party - Issue 1"));
    }
}