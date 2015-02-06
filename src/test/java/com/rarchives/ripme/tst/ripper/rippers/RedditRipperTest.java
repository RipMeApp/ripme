package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.RedditRipper;

public class RedditRipperTest extends RippersTest {

    public void testRedditAlbums() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.reddit.com/r/nsfw_oc"));
        contentURLs.add(new URL("http://www.reddit.com/r/nsfw_oc/top?t=all"));
        contentURLs.add(new URL("http://www.reddit.com/r/UnrealGirls/comments/1ziuhl/in_class_veronique_popa/"));
        for (URL url : contentURLs) {
            RedditRipper ripper = new RedditRipper(url);
            testRipper(ripper);
        }
    }

}
