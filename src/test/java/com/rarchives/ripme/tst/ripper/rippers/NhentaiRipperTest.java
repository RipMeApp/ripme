package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.NhentaiRipper;
import com.rarchives.ripme.utils.RipUtils;
<<<<<<< HEAD
=======
import org.junit.jupiter.api.Assertions;
>>>>>>> upstream/master
import org.junit.jupiter.api.Test;

public class NhentaiRipperTest extends RippersTest {
    public void testRip() throws IOException {
        NhentaiRipper ripper = new NhentaiRipper(new URL("https://nhentai.net/g/233295/"));
        testRipper(ripper);
    }

    public void testGetGID()  throws IOException {
        NhentaiRipper ripper = new NhentaiRipper(new URL("https://nhentai.net/g/233295/"));
<<<<<<< HEAD
        assertEquals("233295", ripper.getGID(new URL("https://nhentai.net/g/233295/")));
=======
        Assertions.assertEquals("233295", ripper.getGID(new URL("https://nhentai.net/g/233295/")));
>>>>>>> upstream/master
    }

    // Test the tag black listing
    @Test
    public void testTagBlackList()  throws IOException {
        URL url = new URL("https://nhentai.net/g/233295/");
        NhentaiRipper ripper = new NhentaiRipper(url);
        List<String> tagsOnPage = ripper.getTags(ripper.getFirstPage());
        // Test multiple blacklisted tags
        String[] tags = {"test", "one", "blowjob"};
        String blacklistedTag = RipUtils.checkTags(tags, tagsOnPage);
<<<<<<< HEAD
        assertEquals("blowjob", blacklistedTag);
=======
        Assertions.assertEquals("blowjob", blacklistedTag);
>>>>>>> upstream/master

        // test tags with spaces in them
        String[] tags2 = {"test", "one", "sole-female"};
        blacklistedTag = RipUtils.checkTags(tags2, tagsOnPage);
<<<<<<< HEAD
        assertEquals("sole-female", blacklistedTag);
=======
        Assertions.assertEquals("sole-female", blacklistedTag);
>>>>>>> upstream/master
    }
}
