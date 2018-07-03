package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.VideoRipper;
import com.rarchives.ripme.ripper.rippers.video.PornhubRipper;
import com.rarchives.ripme.ripper.rippers.video.XhamsterRipper;
import com.rarchives.ripme.ripper.rippers.XvideosRipper;
import com.rarchives.ripme.ripper.rippers.video.YoupornRipper;
import com.rarchives.ripme.ripper.rippers.video.YuvutuRipper;

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


//    Test disbaled. See https://github.com/RipMeApp/ripme/issues/574
    
//    public void testTwitchVideoRipper() throws IOException {
//        List<URL> contentURLs = new ArrayList<>();
//        contentURLs.add(new URL("https://clips.twitch.tv/FaithfulIncredulousPotTBCheesePull"));
//        for (URL url : contentURLs) {
//            TwitchVideoRipper ripper = new TwitchVideoRipper(url);
//            videoTestHelper(ripper);
//        }
//    }
    
    public void testXhamsterRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL("https://xhamster.com/videos/brazzers-busty-big-booty-milf-lisa-ann-fucks-her-masseur-1492828"));
        for (URL url : contentURLs) {
            XhamsterRipper ripper = new XhamsterRipper(url);
            videoTestHelper(ripper);
        }
    }
    
    public void testPornhubRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL("https://www.pornhub.com/view_video.php?viewkey=ph5a329fa707269"));
        for (URL url : contentURLs) {
            PornhubRipper ripper = new PornhubRipper(url);
            videoTestHelper(ripper);
        }
    }

    public void testYoupornRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL("http://www.youporn.com/watch/7669155/mrs-li-amateur-69-orgasm/?from=categ"));
        for (URL url : contentURLs) {
            YoupornRipper ripper = new YoupornRipper(url);
            videoTestHelper(ripper);
        }
    }
    
    public void testYuvutuRipper() throws IOException {
        List<URL> contentURLs = new ArrayList<>();
        contentURLs.add(new URL("http://www.yuvutu.com/video/828499/female-reader-armpit-job/"));
        for (URL url : contentURLs) {
            YuvutuRipper ripper = new YuvutuRipper(url);
            videoTestHelper(ripper);
        }
    }

}
