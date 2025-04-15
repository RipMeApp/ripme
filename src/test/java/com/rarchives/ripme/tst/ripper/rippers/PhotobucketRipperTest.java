package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PhotobucketRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PhotobucketRipperTest extends RippersTest {

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/229 : Disabled test (temporary) : BasicRippersTest#testPhotobucketRip (timing out)")
    public void testPhotobucketRip() throws IOException, URISyntaxException {
        PhotobucketRipper ripper = new PhotobucketRipper(
                new URI("http://s844.photobucket.com/user/SpazzySpizzy/library/Album%20Covers?sort=3&page=1").toURL());
        testRipper(ripper);
        deleteSubdirs(ripper.getWorkingDir());
        deleteDir(ripper.getWorkingDir());
    }

    @Test
    @Disabled("new test, still disabled out because of the issue above, since this test also involves network IO.")
    public void testGetNextPage() throws IOException, URISyntaxException {
        // this album should have more than enough sub-albums and pages
        // to serve as a pretty good iteration test (barring server or
        // network errors)
        String baseURL = "http://s1255.photobucket.com/user/mimajki/library/Movie%20gifs?sort=6&page=1";
        URL url = new URI(baseURL).toURL();
        PhotobucketRipper ripper = new PhotobucketRipper(url);
        org.jsoup.nodes.Document page = ripper.getFirstPage();
        // NOTE: number of pages remaining includes the subalbums
        // of the current album
        int numPagesRemaining = 38;
        for (int idx = 0; idx < numPagesRemaining; idx++) {
            page = ripper.getNextPage(page);
            System.out.println("URL: " + page.location());
        }
        try {
            page = ripper.getNextPage(page);
            Assertions.fail("Get next page did not throw an exception on the last page");
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "No more pages");
        }
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI(
                "http://s732.photobucket.com/user/doublesix66/library/Army%20Painter%20examples?sort=3&page=1").toURL();
        PhotobucketRipper ripper = new PhotobucketRipper(url);
        Assertions.assertEquals("doublesix66", ripper.getGID(url));
        url = new URI(
                "http://s732.photobucket.com/user/doublesix66/library/Army%20Painter%20examples/Painting%20examples?page=1&sort=3").toURL();
        Assertions.assertEquals("doublesix66", ripper.getGID(url));
        url = new URI("http://s844.photobucket.com/user/SpazzySpizzy/library/Album%20Covers").toURL();
        Assertions.assertEquals("SpazzySpizzy", ripper.getGID(url));
        url = new URI("http://s844.photobucket.com/user/SpazzySpizzy/library").toURL();
        Assertions.assertEquals("SpazzySpizzy", ripper.getGID(url));
    }
}