package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NhentaiRipper;

public class NhentaiRipperTest extends RippersTest {
    public void testRip() throws IOException {
        NhentaiRipper ripper = new NhentaiRipper(new URL("https://nhentai.net/g/233295/"));
        testRipper(ripper);
    }

    public void testGetGID()  throws IOException {
        NhentaiRipper ripper = new NhentaiRipper(new URL("https://nhentai.net/g/233295/"));
        assertEquals("233295", ripper.getGID(new URL("https://nhentai.net/g/233295/")));
    }

    // Test the tag black listing
    public void testTagBlackList()  throws IOException {
        URL url = new URL("https://nhentai.net/g/233295/");
        NhentaiRipper ripper = new NhentaiRipper(url);
        // Test multiple blacklisted tags
        String[] tags = {"test", "one", "blowjob"};
        String blacklistedTag = ripper.checkTags(ripper.getFirstPage(), tags);
        assertEquals("blowjob", blacklistedTag);

        // test tags with spaces in them
        String[] tags2 = {"test", "one", "sole female"};
        blacklistedTag = ripper.checkTags(ripper.getFirstPage(), tags2);
        assertEquals("sole female", blacklistedTag);
    }
}
