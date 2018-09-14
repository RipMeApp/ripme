package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.EHentaiRipper;
import com.rarchives.ripme.utils.RipUtils;

public class EhentaiRipperTest extends RippersTest {
    public void testEHentaiAlbum() throws IOException {
        EHentaiRipper ripper = new EHentaiRipper(new URL("https://e-hentai.org/g/1144492/e823bdf9a5/"));
        testRipper(ripper);
    }

    // Test the tag black listing
    public void testTagBlackList()  throws IOException {
        URL url = new URL("https://e-hentai.org/g/1228503/1a2f455f96/");
        EHentaiRipper ripper = new EHentaiRipper(url);
        List<String> tagsOnPage = ripper.getTags(ripper.getFirstPage());
        // Test multiple blacklisted tags
        String[] tags = {"test", "one", "yuri"};
        String blacklistedTag = RipUtils.checkTags(tags, tagsOnPage);
        assertEquals("yuri", blacklistedTag);

        // test tags with spaces in them
        String[] tags2 = {"test", "one", "midnight on mars"};
        blacklistedTag = RipUtils.checkTags(tags2, tagsOnPage);
        assertEquals("midnight on mars", blacklistedTag);
    }
}