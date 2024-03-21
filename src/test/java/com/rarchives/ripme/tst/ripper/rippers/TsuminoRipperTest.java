package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.TsuminoRipper;
import com.rarchives.ripme.utils.RipUtils;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class TsuminoRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testTsuminoRipper() throws IOException, URISyntaxException {
        TsuminoRipper ripper = new TsuminoRipper(new URI("http://www.tsumino.com/Book/Info/43528/sore-wa-kurokute-suketeita-what-s-tight-and-black-and-sheer-all-over-").toURL());
        testRipper(ripper);
    }
    @Test
    @Disabled("Broken ripper")
    public void testTagBlackList() throws IOException, URISyntaxException {
        TsuminoRipper ripper = new TsuminoRipper(new URI("http://www.tsumino.com/Book/Info/43528/sore-wa-kurokute-suketeita-what-s-tight-and-black-and-sheer-all-over-").toURL());
        Document doc = ripper.getFirstPage();
        List<String> tagsOnPage = ripper.getTags(doc);
        String[] tags1 = {"test", "one", "Smell"};
        String blacklistedTag = RipUtils.checkTags(tags1, tagsOnPage);
        Assertions.assertEquals("smell", blacklistedTag);

        // Test a tag with spaces
        String[] tags2 = {"test", "one", "Face sitting"};
        blacklistedTag = RipUtils.checkTags(tags2, tagsOnPage);
        Assertions.assertEquals("face sitting", blacklistedTag);

        // Test a album with no blacklisted tags
        String[] tags3 = {"nothing", "one", "null"};
        blacklistedTag = RipUtils.checkTags(tags3, tagsOnPage);
        Assertions.assertNull(blacklistedTag);

    }
}