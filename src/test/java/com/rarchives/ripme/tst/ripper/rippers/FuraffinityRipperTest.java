package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FuraffinityRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class FuraffinityRipperTest extends RippersTest {
    @Test
    @Tag("slow")
    public void testFuraffinityAlbum() throws IOException, URISyntaxException {
        FuraffinityRipper ripper = new FuraffinityRipper(new URI("https://www.furaffinity.net/gallery/spencerdragon/").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("slow")
    public void testFuraffinityScrap() throws IOException, URISyntaxException {
        FuraffinityRipper ripper = new FuraffinityRipper(new URI("http://www.furaffinity.net/scraps/sssonic2/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://www.furaffinity.net/gallery/mustardgas/").toURL();
        FuraffinityRipper ripper = new FuraffinityRipper(url);
        Assertions.assertEquals("mustardgas", ripper.getGID(url));
    }
    @Test
    @Tag("flaky")
    public void testLogin() throws IOException, URISyntaxException {
        URL url = new URI("https://www.furaffinity.net/gallery/mustardgas/").toURL();
        FuraffinityRipper ripper = new FuraffinityRipper(url);
        // Check if the first page contain the username of ripmes shared account
        boolean containsUsername = ripper.getFirstPage().html().contains("ripmethrowaway");
        assert containsUsername;
    }
}
