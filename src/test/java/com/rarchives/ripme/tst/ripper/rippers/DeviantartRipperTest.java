package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.DeviantartRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;

public class DeviantartRipperTest extends RippersTest {
    public void testDeviantartAlbum() throws IOException {
        DeviantartRipper ripper = new DeviantartRipper(new URL("https://www.deviantart.com/airgee/gallery/"));
        testRipper(ripper);
    }

    public void testDeviantartNSFWAlbum() throws IOException {
        // NSFW gallery
        DeviantartRipper ripper = new DeviantartRipper(new URL("https://www.deviantart.com/faterkcx/gallery/"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://www.deviantart.com/airgee/gallery/");
        DeviantartRipper ripper = new DeviantartRipper(url);
        assertEquals("airgee", ripper.getGID(url));
    }

    public void testGetGalleryIDAndUsername() throws IOException {
        URL url = new URL("https://www.deviantart.com/airgee/gallery/");
        DeviantartRipper ripper = new DeviantartRipper(url);
        Document doc = Http.url(url).get();
        assertEquals("airgee", ripper.getUsername(doc));
        assertEquals("714589", ripper.getGalleryID(doc));
    }

    public void testSanitizeURL() throws IOException {
        List<URL> urls = new ArrayList<URL>();
        urls.add(new URL("https://www.deviantart.com/airgee/"));
        urls.add(new URL("https://www.deviantart.com/airgee"));
        urls.add(new URL("https://www.deviantart.com/airgee/gallery/"));

        for (URL url : urls) {
            DeviantartRipper ripper = new DeviantartRipper(url);
            assertEquals("https://www.deviantart.com/airgee/gallery/", ripper.sanitizeURL(url).toExternalForm());
        }
    }
}
