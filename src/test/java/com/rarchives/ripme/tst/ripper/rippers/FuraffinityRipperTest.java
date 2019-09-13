package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FuraffinityRipper;
import org.junit.jupiter.api.Test;

public class FuraffinityRipperTest extends RippersTest {
    @Test
    public void testFuraffinityAlbum() throws IOException {
        FuraffinityRipper ripper = new FuraffinityRipper(new URL("https://www.furaffinity.net/gallery/spencerdragon/"));
        testRipper(ripper);
    }
    @Test
    public void testFuraffinityScrap() throws IOException {
        FuraffinityRipper ripper = new FuraffinityRipper(new URL("http://www.furaffinity.net/scraps/sssonic2/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://www.furaffinity.net/gallery/mustardgas/");
        FuraffinityRipper ripper = new FuraffinityRipper(url);
        assertEquals("mustardgas", ripper.getGID(url));
    }
    @Test
    public void testLogin() throws IOException {
        URL url = new URL("https://www.furaffinity.net/gallery/mustardgas/");
        FuraffinityRipper ripper = new FuraffinityRipper(url);
        // Check if the first page contain the username of ripmes shared account
        Boolean containsUsername = ripper.getFirstPage().html().contains("ripmethrowaway");
        assert containsUsername;
    }
}
