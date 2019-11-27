package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.InstagramRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstagramRipperTest extends RippersTest {
    @Test
    public void testInstagramGID() throws IOException {
        Map<URL, String> testURLs = new HashMap<>();
        testURLs.put(new URL("http://instagram.com/Test_User"), "Test_User");
        testURLs.put(new URL("http://instagram.com/_test_user_"), "_test_user_");
        testURLs.put(new URL("http://instagram.com/_test_user_/?pinned"), "_test_user__pinned");
        testURLs.put(new URL("http://instagram.com/stories/_test_user_/"), "_test_user__stories");
        testURLs.put(new URL("http://instagram.com/_test_user_/tagged"), "_test_user__tagged");
        testURLs.put(new URL("http://instagram.com/_test_user_/channel"), "_test_user__igtv");
        testURLs.put(new URL("http://instagram.com/explore/tags/test_your_tag"), "tag_test_your_tag");
        testURLs.put(new URL("https://www.instagram.com/p/BZ4egP7njW5/?hl=en"), "post_BZ4egP7njW5");
        testURLs.put(new URL("https://www.instagram.com/p/BZ4egP7njW5"), "post_BZ4egP7njW5");
        testURLs.put(new URL("https://www.instagram.com/p/BaNPpaHn2zU/?taken-by=hilaryduff"), "post_BaNPpaHn2zU");
        testURLs.put(new URL("https://www.instagram.com/p/BaNPpaHn2zU/"), "post_BaNPpaHn2zU");
        for (URL url : testURLs.keySet()) {
            InstagramRipper ripper = new InstagramRipper(url);
            ripper.setup();
            assertEquals(testURLs.get(url), ripper.getGID(ripper.getURL()));
            deleteDir(ripper.getWorkingDir());
        }
    }

    @Test
    public void testInstagramAlbums() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        // This unit test is a bit flaky 
        //contentURLs.add(new URL("https://www.instagram.com/Test_User/"));
        contentURLs.add(new URL("https://www.instagram.com/p/BaNPpaHn2zU/?hl=en"));
        contentURLs.add(new URL("https://www.instagram.com/p/BaNPpaHn2zU/"));
        for (URL url : contentURLs) {
            InstagramRipper ripper = new InstagramRipper(url);
            testRipper(ripper);
        }
    }
}
