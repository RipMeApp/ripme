package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.VkRipper;

public class VkRipperTest extends RippersTest {
    
    public void testVkAlbum() throws IOException {
        if (!DOWNLOAD_CONTENT) {
            return;
        }
        List<URL> contentURLs = new ArrayList<URL>();
        contentURLs.add(new URL("https://vk.com/album45506334_172415053"));
        //contentURLs.add(new URL("https://vk.com/album45506334_0"));
        //contentURLs.add(new URL("https://vk.com/photos45506334"));
        for (URL url : contentURLs) {
            try {
                VkRipper ripper = new VkRipper(url);
                ripper.rip();
                assert(ripper.getWorkingDir().listFiles().length > 1);
                deleteDir(ripper.getWorkingDir());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Error while ripping URL " + url + ": " + e.getMessage());
            }
        }
    }

}
