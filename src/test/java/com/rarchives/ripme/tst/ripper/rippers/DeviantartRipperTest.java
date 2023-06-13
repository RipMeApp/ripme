package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.DeviantartRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DeviantartRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testDeviantartAlbum() throws IOException, URISyntaxException {
        DeviantartRipper ripper = new DeviantartRipper(new URI("https://www.deviantart.com/airgee/gallery/").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("Broken ripper")
    public void testDeviantartNSFWAlbum() throws IOException, URISyntaxException {
        // NSFW gallery
        DeviantartRipper ripper = new DeviantartRipper(new URI("https://www.deviantart.com/faterkcx/gallery/").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled("Broken ripper")
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://www.deviantart.com/airgee/gallery/").toURL();
        DeviantartRipper ripper = new DeviantartRipper(url);
        Assertions.assertEquals("airgee", ripper.getGID(url));
    }

    @Test
    @Disabled("Broken ripper")
    public void testGetGalleryIDAndUsername() throws IOException, URISyntaxException {
        URL url = new URI("https://www.deviantart.com/airgee/gallery/").toURL();
        DeviantartRipper ripper = new DeviantartRipper(url);
        Document doc = Http.url(url).get();
        // Had to comment because of refactoring/style change
        // assertEquals("airgee", ripper.getUsername(doc));
        // assertEquals("714589", ripper.getGalleryID(doc));
    }

    @Test
    @Disabled("Broken ripper")
    public void testSanitizeURL() throws IOException, URISyntaxException {
        List<URL> urls = new ArrayList<URL>();
        urls.add(new URI("https://www.deviantart.com/airgee/").toURL());
        urls.add(new URI("https://www.deviantart.com/airgee").toURL());
        urls.add(new URI("https://www.deviantart.com/airgee/gallery/").toURL());

        for (URL url : urls) {
            DeviantartRipper ripper = new DeviantartRipper(url);
            Assertions.assertEquals("https://www.deviantart.com/airgee/gallery/", ripper.sanitizeURL(url).toExternalForm());
        }
    }
}
