package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.EHentaiRipper;
import com.rarchives.ripme.utils.RipUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class EhentaiRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testEHentaiAlbum() throws IOException, URISyntaxException {
        EHentaiRipper ripper = new EHentaiRipper(new URI("https://e-hentai.org/g/1144492/e823bdf9a5/").toURL());
        testRipper(ripper);
    }

    // Test the tag black listing
    @Test
    public void testTagBlackList() throws IOException, URISyntaxException {
        URL url = new URI("https://e-hentai.org/g/1228503/1a2f455f96/").toURL();
        EHentaiRipper ripper = new EHentaiRipper(url);
        List<String> tagsOnPage = ripper.getTags(ripper.getFirstPage());
        // Test multiple blacklisted tags
        String[] tags = {"test", "one", "yuri"};
        String blacklistedTag = RipUtils.checkTags(tags, tagsOnPage);
        Assertions.assertEquals("yuri", blacklistedTag);

        // test tags with spaces in them
        String[] tags2 = {"test", "one", "midnight on mars"};
        blacklistedTag = RipUtils.checkTags(tags2, tagsOnPage);
        Assertions.assertEquals("midnight on mars", blacklistedTag);
    }
}
