package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.ripper.rippers.ListalRipper;

public class ListalRipperTest extends RippersTest {

    /**
     * Test for list type url.
     * @throws IOException
     */
    public void testRipListType() throws IOException {
        ListalRipper ripper =
                new ListalRipper(new URL("https://www.listal.com/list/evolution-emma-stone"));
        testRipper(ripper);
    }

    /**
     * Test for folder type url.
     * @throws IOException
     */
    public void testRipFolderType() throws IOException {
        ListalRipper ripper =
                new ListalRipper(new URL("https://www.listal.com/chet-atkins/pictures"));
        testRipper(ripper);
    }

}
