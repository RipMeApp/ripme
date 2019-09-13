package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.TsuminoRipper;
import com.rarchives.ripme.utils.RipUtils;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;


public class TsuminoRipperTest extends RippersTest {
    @Test
    public void testTsuminoRipper() throws IOException {
        TsuminoRipper ripper = new TsuminoRipper(new URL("http://www.tsumino.com/Book/Info/43528/sore-wa-kurokute-suketeita-what-s-tight-and-black-and-sheer-all-over-"));
        testRipper(ripper);
    }
    @Test
    public void testTagBlackList() throws IOException {
        TsuminoRipper ripper = new TsuminoRipper(new URL("http://www.tsumino.com/Book/Info/43528/sore-wa-kurokute-suketeita-what-s-tight-and-black-and-sheer-all-over-"));
        Document doc = ripper.getFirstPage();
        List<String> tagsOnPage = ripper.getTags(doc);
        String[] tags1 = {"test", "one", "Smell"};
        String blacklistedTag = RipUtils.checkTags(tags1, tagsOnPage);
        assertEquals("smell", blacklistedTag);

        // Test a tag with spaces
        String[] tags2 = {"test", "one", "Face sitting"};
        blacklistedTag = RipUtils.checkTags(tags2, tagsOnPage);
        assertEquals("face sitting", blacklistedTag);

        // Test a album with no blacklisted tags
        String[] tags3 = {"nothing", "one", "null"};
        blacklistedTag = RipUtils.checkTags(tags3, tagsOnPage);
        assertNull(blacklistedTag);

    }
}