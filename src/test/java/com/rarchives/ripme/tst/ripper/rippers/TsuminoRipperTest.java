package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.TsuminoRipper;

public class TsuminoRipperTest extends RippersTest {
    public void testTsuminoRipper() throws IOException {
        TsuminoRipper ripper = new TsuminoRipper(new URL("http://www.tsumino.com/Book/Info/42882/chaldea-maid-"));
        testRipper(ripper);
    }

    public void testTagBlackList() throws IOException {
        TsuminoRipper ripper = new TsuminoRipper(new URL("http://www.tsumino.com/Book/Info/42882/chaldea-maid-"));
        String[] tags1 = {"test", "one", "Blowjob"};
        String blacklistedTag = ripper.checkTags(ripper.getFirstPage(), tags1);
        assertEquals("blowjob", blacklistedTag);

        // Test a tag with spaces
        String[] tags2 = {"test", "one", "Full Color"};
        blacklistedTag = ripper.checkTags(ripper.getFirstPage(), tags2);
        assertEquals("full color", blacklistedTag);

    }
}