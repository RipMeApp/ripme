package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.ripper.rippers.video.PornhubRipper;
import com.rarchives.ripme.ripper.rippers.video.VineRipper;
import com.rarchives.ripme.ripper.rippers.video.XvideosRipper;
import com.rarchives.ripme.ripper.rippers.video.YoupornRipper;

public class VideoRippersTest extends RippersTest {

    /**
     * Helper method for testing a video ripper
     * @param ripper The video ripper
     */
    private void videoTestHelper(VideoRipper ripper) {
        URL oldURL = ripper.getURL();
        try {
            ripper.setup();
            ripper.markAsTest();
            ripper.rip();
            // Video ripper testing is... weird.
            // If the ripper finds the URL to download the video, and it's a test,
            // then the ripper sets the download URL as the ripper's URL.
            assertFalse("Failed to find download url for " + oldURL, oldURL.equals(ripper.getURL()));
        } catch (Exception e) {
            fail("Error while ripping " + ripper.getURL() + " : " + e);
            e.printStackTrace();
        } finally {
            deleteDir(ripper.getWorkingDir());
        }
    }

    public void testXvideosRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.xvideos.com/video1428195/stephanie_first_time_anal"));
        contentURLs.add(new URL("http://www.xvideos.com/video7136868/vid-20140205-wa0011"));
        for (URL url : contentURLs) {
            XvideosRipper ripper = new XvideosRipper(url);
            videoTestHelper(ripper);
        }
    }

    public void testPornhubRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.pornhub.com/view_video.php?viewkey=993166542"));
        for (URL url : contentURLs) {
            PornhubRipper ripper = new PornhubRipper(url);
            videoTestHelper(ripper);
        }
    }

    public void testVineRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("https://vine.co/v/hiqQrP0eUZx"));
        for (URL url : contentURLs) {
            VineRipper ripper = new VineRipper(url);
            videoTestHelper(ripper);
        }
    }

    public void testYoupornRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("http://www.youporn.com/watch/7669155/mrs-li-amateur-69-orgasm/?from=categ"));
        for (URL url : contentURLs) {
            YoupornRipper ripper = new YoupornRipper(url);
            videoTestHelper(ripper);
        }
    }

}
