package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.TsuminoRipper;
import com.rarchives.ripme.utils.RipUtils;
import org.jsoup.nodes.Document;


public class TsuminoRipperTest extends RippersTest {
    public void testTsuminoRipper() throws IOException {
        TsuminoRipper ripper = new TsuminoRipper(new URL("http://www.tsumino.com/Book/Info/42882/chaldea-maid-"));
        testRipper(ripper);
    }

    public void testTagBlackList() throws IOException {
        TsuminoRipper ripper = new TsuminoRipper(new URL("http://www.tsumino.com/Book/Info/42882/chaldea-maid-"));
        Document doc = ripper.getFirstPage();
        List<String> tagsOnPage = ripper.getTags(doc);
        String[] tags1 = {"test", "one", "Blowjob"};
        String blacklistedTag = RipUtils.checkTags(tags1, tagsOnPage);
        assertEquals("blowjob", blacklistedTag);

        // Test a tag with spaces
        String[] tags2 = {"test", "one", "Full Color"};
        blacklistedTag = RipUtils.checkTags(tags2, tagsOnPage);
        assertEquals("full color", blacklistedTag);

        // Test a album with no blacklisted tags
        String[] tags3 = {"nothing", "one", "null"};
        blacklistedTag = RipUtils.checkTags(tags3, tagsOnPage);
        assertNull(blacklistedTag);

    }
}