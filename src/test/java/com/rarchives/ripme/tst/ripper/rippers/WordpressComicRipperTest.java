package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.WordpressComicRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class WordpressComicRipperTest extends RippersTest {
    // Test links (see also WordpressComicRipper.java)
    // http://www.totempole666.com/comic/first-time-for-everything-00-cover/
    // http://buttsmithy.com/archives/comic/p1
    // http://themonsterunderthebed.net/?comic=test-post
    // http://prismblush.com/comic/hella-trap-pg-01/
    // http://www.konradokonski.com/sawdust/comic/get-up/
    // http://www.konradokonski.com/wiory/comic/08182008/
    // http://freeadultcomix.com/finders-feepaid-in-full-sparrow/
    // http://thisis.delvecomic.com/NewWP/comic/in-too-deep/
    // http://tnbtu.com/comic/01-00/
    // http://shipinbottle.pepsaga.com/?p=281

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_totempole666() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://www.totempole666.com/comic/first-time-for-everything-00-cover/").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_buttsmithy() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(new URI("http://buttsmithy.com/archives/comic/p1").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_themonsterunderthebed() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://themonsterunderthebed.net/?comic=test-post").toURL());
        testRipper(ripper);
    }
    @Test
    public void test_prismblush() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://prismblush.com/comic/hella-trap-pg-01/").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void test_konradokonski_1() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://www.konradokonski.com/sawdust/comic/get-up/").toURL());
        testRipper(ripper);

    }
    @Test
    @Tag("flaky")
    public void test_konradokonski_2() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://www.konradokonski.com/wiory/comic/08182008/").toURL());
        testRipper(ripper);
    }
    @Test
    public void test_konradokonski_getAlbumTitle() throws IOException, URISyntaxException {
        URL url = new URI("http://www.konradokonski.com/sawdust/comic/get-up/").toURL();
        WordpressComicRipper ripper = new WordpressComicRipper(url);
        Assertions.assertEquals("konradokonski.com_sawdust", ripper.getAlbumTitle(url));

    }

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_freeadultcomix() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://freeadultcomix.com/finders-feepaid-in-full-sparrow/").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void test_delvecomic() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://thisis.delvecomic.com/NewWP/comic/in-too-deep/").toURL());
        testRipper(ripper);
    }
    @Test
    public void test_Eightmuses_download() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("https://8muses.download/lustomic-playkittens-josh-samuel-porn-comics-8-muses/").toURL());
        testRipper(ripper);
    }
    @Test
    public void test_Eightmuses_getAlbumTitle() throws IOException, URISyntaxException {
        URL url = new URI("https://8muses.download/lustomic-playkittens-josh-samuel-porn-comics-8-muses/").toURL();
        WordpressComicRipper ripper = new WordpressComicRipper(url);
        Assertions.assertEquals("8muses.download_lustomic-playkittens-josh-samuel-porn-comics-8-muses", ripper.getAlbumTitle(url));
    }
    @Test
    @Tag("flaky")
    public void test_spyingwithlana_download() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(
                new URI("http://spyingwithlana.com/comic/the-big-hookup/").toURL());
        testRipper(ripper);
    }
    @Test
    public void test_spyingwithlana_getAlbumTitle() throws IOException, URISyntaxException {
        URL url = new URI("http://spyingwithlana.com/comic/the-big-hookup/").toURL();
        WordpressComicRipper ripper = new WordpressComicRipper(url);
        Assertions.assertEquals("spyingwithlana_the-big-hookup", ripper.getAlbumTitle(url));
    }

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/269 - Disabled test - WordpressRipperTest: various domains flaky in CI
    public void test_pepsaga() throws IOException, URISyntaxException {
        WordpressComicRipper ripper = new WordpressComicRipper(new URI("http://shipinbottle.pepsaga.com/?p=281").toURL());
        testRipper(ripper);
    }
}
