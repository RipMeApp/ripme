package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.NhentaiRipper;
import com.rarchives.ripme.utils.RipUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class NhentaiRipperTest extends RippersTest {
    public void testRip() throws IOException {
        NhentaiRipper ripper = new NhentaiRipper(new URL("https://nhentai.net/g/233295/"));
        testRipper(ripper);
    }

    public void testGetGID()  throws IOException {
        NhentaiRipper ripper = new NhentaiRipper(new URL("https://nhentai.net/g/233295/"));
        Assertions.assertEquals("233295", ripper.getGID(new URL("https://nhentai.net/g/233295/")));
    }

    // Test the tag black listing
    @Test
    @Tag("flaky")
    public void testTagBlackList()  throws IOException {
        URL url = new URL("https://nhentai.net/g/233295/");
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
