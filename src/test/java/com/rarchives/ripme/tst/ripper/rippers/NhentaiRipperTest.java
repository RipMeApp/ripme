package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.NhentaiRipper;
import com.rarchives.ripme.utils.RipUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class NhentaiRipperTest extends RippersTest {
    public void testRip() throws IOException, URISyntaxException {
        NhentaiRipper ripper = new NhentaiRipper(new URI("https://nhentai.net/g/233295/").toURL());
        testRipper(ripper);
    }

    public void testGetGID() throws IOException, URISyntaxException {
        NhentaiRipper ripper = new NhentaiRipper(new URI("https://nhentai.net/g/233295/").toURL());
        Assertions.assertEquals("233295", ripper.getGID(new URI("https://nhentai.net/g/233295/").toURL()));
    }

    // Test the tag black listing
    @Test
    @Tag("flaky")
    public void testTagBlackList() throws IOException, URISyntaxException {
        URL url = new URI("https://nhentai.net/g/233295/").toURL();
        NhentaiRipper ripper = new NhentaiRipper(url);
        List<String> tagsOnPage = ripper.getTags(ripper.getFirstPage());
        // Test multiple blacklisted tags
        String[] tags = {"test", "one", "blowjob"};
        String blacklistedTag = RipUtils.checkTags(tags, tagsOnPage);
        Assertions.assertEquals("blowjob", blacklistedTag);

        // test tags with spaces in them
        String[] tags2 = {"test", "one", "sole-female"};
        blacklistedTag = RipUtils.checkTags(tags2, tagsOnPage);
        Assertions.assertEquals("sole-female", blacklistedTag);
    }
}
