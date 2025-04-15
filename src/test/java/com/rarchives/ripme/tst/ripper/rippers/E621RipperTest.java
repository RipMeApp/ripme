package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.E621Ripper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class E621RipperTest extends RippersTest {
    public void testRip() throws IOException, URISyntaxException {
        E621Ripper ripper = new E621Ripper(new URI("https://e621.net/posts?tags=beach").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testFlashOrWebm() throws IOException, URISyntaxException {
        E621Ripper ripper = new E621Ripper(new URI("https://e621.net/posts?page=4&tags=gif+rating%3As+3d").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testGetNextPage() throws IOException, URISyntaxException {
        E621Ripper nextPageRipper = new E621Ripper(new URI("https://e621.net/posts?tags=cosmicminerals").toURL());
        try {
            nextPageRipper.getNextPage(nextPageRipper.getFirstPage());
            assert (true);
        } catch (IOException e) {
            throw e;
        }

        E621Ripper noNextPageRipper = new E621Ripper(new URI("https://e621.net/post/index/1/cosmicminerals").toURL());
        try {
            noNextPageRipper.getNextPage(noNextPageRipper.getFirstPage());
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "No more pages.");
        }
    }
    @Test
    @Tag("flaky")
    public void testOldRip() throws IOException, URISyntaxException {
        E621Ripper ripper = new E621Ripper(new URI("https://e621.net/post/index/1/beach").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testOldFlashOrWebm() throws IOException, URISyntaxException {
        E621Ripper ripper = new E621Ripper(new URI("https://e621.net/post/index/1/gif").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testOldGetNextPage() throws IOException, URISyntaxException {
        E621Ripper nextPageRipper = new E621Ripper(new URI("https://e621.net/post/index/1/cosmicminerals").toURL());
        try {
            nextPageRipper.getNextPage(nextPageRipper.getFirstPage());
            assert (true);
        } catch (IOException e) {
            throw e;
        }

        E621Ripper noNextPageRipper = new E621Ripper(new URI("https://e621.net/post/index/1/cosmicminerals").toURL());
        try {
            noNextPageRipper.getNextPage(noNextPageRipper.getFirstPage());
        } catch (IOException e) {
            Assertions.assertEquals(e.getMessage(), "No more pages.");
        }
    }
}
