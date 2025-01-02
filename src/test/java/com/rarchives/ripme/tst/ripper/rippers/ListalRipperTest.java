package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.ListalRipper;
import org.junit.jupiter.api.*;

public class ListalRipperTest extends RippersTest {

    /**
     * Test for list type url.
     */
    @Test
    @Tag("flaky")
    public void testPictures() throws IOException, URISyntaxException {
        ListalRipper ripper =
                new ListalRipper(new URI("https://www.listal.com/emma-stone_iii/pictures").toURL());
        testRipper(ripper);
    }

    /**
     * Test for list type url.
     */
    @Test
    @Tag("flaky")
    public void testRipListType() throws IOException, URISyntaxException {
        ListalRipper ripper =
                new ListalRipper(new URI("https://www.listal.com/list/evolution-emma-stone").toURL());
        testRipper(ripper);
    }

    /**
     * Test for folder type url.
     */
    @Test
    public void testRipFolderType() throws IOException, URISyntaxException {
        ListalRipper ripper =
                new ListalRipper(new URI("https://www.listal.com/chet-atkins/pictures").toURL());
        testRipper(ripper);
    }

}
