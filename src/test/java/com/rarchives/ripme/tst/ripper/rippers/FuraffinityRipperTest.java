package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FuraffinityRipper;

public class FuraffinityRipperTest extends RippersTest {
    public void testFuraffinityAlbum() throws IOException {
        FuraffinityRipper ripper = new FuraffinityRipper(new URL("https://www.furaffinity.net/gallery/mustardgas/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://www.furaffinity.net/gallery/mustardgas/");
        FuraffinityRipper ripper = new FuraffinityRipper(url);
        assertEquals("mustardgas", ripper.getGID(url));
    }

    public void testLogin() throws IOException {
        URL url = new URL("https://www.furaffinity.net/gallery/mustardgas/");
        FuraffinityRipper ripper = new FuraffinityRipper(url);
        // Check if the first page contain the username of ripmes shared account
        Boolean containsUsername = ripper.getFirstPage().html().contains("ripmethrowaway");
        assert containsUsername;
    }
}
