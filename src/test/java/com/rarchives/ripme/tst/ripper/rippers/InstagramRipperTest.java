package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.InstagramRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstagramRipperTest extends RippersTest {
    @Test
    public void testInstagramGID() throws IOException, URISyntaxException {
        Map<URL, String> testURLs = new HashMap<>();
        testURLs.put(new URI("http://instagram.com/Test_User").toURL(), "Test_User");
        testURLs.put(new URI("http://instagram.com/_test_user_").toURL(), "_test_user_");
        testURLs.put(new URI("http://instagram.com/_test_user_/?pinned").toURL(), "_test_user__pinned");
        testURLs.put(new URI("http://instagram.com/stories/_test_user_/").toURL(), "_test_user__stories");
        testURLs.put(new URI("http://instagram.com/_test_user_/tagged").toURL(), "_test_user__tagged");
        testURLs.put(new URI("http://instagram.com/_test_user_/channel").toURL(), "_test_user__igtv");
        testURLs.put(new URI("http://instagram.com/explore/tags/test_your_tag").toURL(), "tag_test_your_tag");
        testURLs.put(new URI("https://www.instagram.com/p/BZ4egP7njW5/?hl=en").toURL(), "post_BZ4egP7njW5");
        testURLs.put(new URI("https://www.instagram.com/p/BZ4egP7njW5").toURL(), "post_BZ4egP7njW5");
        testURLs.put(new URI("https://www.instagram.com/p/BaNPpaHn2zU/?taken-by=hilaryduff").toURL(), "post_BaNPpaHn2zU");
        testURLs.put(new URI("https://www.instagram.com/p/BaNPpaHn2zU/").toURL(), "post_BaNPpaHn2zU");
        for (URL url : testURLs.keySet()) {
            InstagramRipper ripper = new InstagramRipper(url);
            ripper.setup();
            Assertions.assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    @Test
    @Disabled("Ripper broken for single items")
    public void testInstagramSingle() throws IOException, URISyntaxException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URI("https://www.instagram.com/p/BaNPpaHn2zU/?hl=en").toURL());
        contentURLs.add(new URI("https://www.instagram.com/p/BaNPpaHn2zU/").toURL());
        for (URL url : contentURLs) {
            InstagramRipper ripper = new InstagramRipper(url);
            testRipper(ripper);
        }
    }

    @Test
    @Tag("flaky")
    public void testInstagramAlbums() throws IOException, URISyntaxException {
        // do not test, in case of rate limit 200/hr since 2021. see
        // https://github.com/ripmeapp2/ripme/issues/32
        URL url = new URI("https://www.instagram.com/Test_User/").toURL();
        InstagramRipper ripper = new InstagramRipper(url);
        testRipper(ripper);
    }
}
