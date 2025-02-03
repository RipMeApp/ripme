package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ripper.rippers.FapwizRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class FapwizRipperTest extends RippersTest {
    @Test
    @Tag("flaky") // It seems like fetching the document within the test can be flaky.
    public void testGetNextPage_NoNextPage() throws IOException, URISyntaxException {
        URL url = new URI("https://fapwiz.com/alison-esha/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);

        Document firstPage = Http.url(url).userAgent(AbstractRipper.USER_AGENT).retries(1).get();
        try {
            ripper.getNextPage(firstPage);
            // If we don't throw, we failed the text because there *was* a next
            // page even though there shouldn't be.
            Assertions.fail();
        } catch (IOException exception) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Tag("flaky") // It seems like fetching the document within the test can be flaky.
    public void testGetNextPage_HasNextPage() throws IOException, URISyntaxException {
        URL url = new URI("https://fapwiz.com/miaipanema/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);

        Document firstPage = Http.url(url).userAgent(AbstractRipper.USER_AGENT).retries(1).get();
        try {
            Document doc = ripper.getNextPage(firstPage);
            Assertions.assertNotNull(doc);
        } catch (IOException exception) {
            // We should have found a next page but didn't.
            Assertions.fail();
        }
    }

    @Test
    public void testRipPost() throws IOException, URISyntaxException {
        URL url = new URI("https://fapwiz.com/petiteasiantravels/riding-at-9-months-pregnant/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        testRipper(ripper);
    }

    @Test
    public void testRipPostWithNumbersInUsername1() throws IOException, URISyntaxException {
        URL url = new URI("https://fapwiz.com/desperate_bug_7776/lets-be-friends-that-secretly-fuck-thanks/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        testRipper(ripper);
    }

    @Test
    public void testRipPostWithEmojiInShortUrl() throws IOException, URISyntaxException {
        URL url = new URI("https://fapwiz.com/miaipanema/my-grip-needs-a-name-%f0%9f%a4%ad%f0%9f%91%87%f0%9f%8f%bc/")
                .toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        testRipper(ripper);
    }

    @Test
    public void testRipPostWithEmojiInLongUrlAtEnd() throws IOException, URISyntaxException {
        URL url = new URI(
                "https://fapwiz.com/bimeat1998/just-imagine-youre-out-with-your-girl-and-your-buddies-and-then-she-makes-this-move-%f0%9f%98%8d/")
                .toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        testRipper(ripper);
    }

    @Test
    public void testRipPostWithEmojiInLongUrlInTheMiddle() throws IOException, URISyntaxException {
        URL url = new URI(
                "https://fapwiz.com/miaipanema/new-pov-couch-sex-with-perfect-cumshot-on-my-ass-%f0%9f%92%a6-you-know-where-to-get-it-%f0%9f%94%97%f0%9f%92%96/")
                .toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        testRipper(ripper);
    }

    // TODO Test rip user

    // TODO Test rip category

    @Test
    public void testPostGetGID1_Simple() throws IOException, URISyntaxException {
        URL url = new URI("https://fapwiz.com/petiteasiantravels/riding-at-9-months-pregnant/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        Assertions.assertEquals("post_petiteasiantravels_riding-at-9-months-pregnant", ripper.getGID(url));
    }

    // Test Post pages GetGID

    @Test
    public void testPostGetGID2_WithEmojiInLongUrlInTheMiddle() throws IOException, URISyntaxException {
        URL url = new URI(
                "https://fapwiz.com/miaipanema/new-pov-couch-sex-with-perfect-cumshot-on-my-ass-%f0%9f%92%a6-you-know-where-to-get-it-%f0%9f%94%97%f0%9f%92%96/")
                .toURL();
        FapwizRipper ripper = new FapwizRipper(url);

        // In this case the filesystem safe version of the GID is
        // "post_miaipanema_new-pov-couch-sex-with-perfect-cumshot-on-my-ass-f09f92a6-you-know-where-to-"
        // but the GID doesn't truncate and doesn't remove non-filesystem-safe
        // characters.
        String gid = ripper.getGID(url);
        Assertions.assertEquals(
                "post_miaipanema_new-pov-couch-sex-with-perfect-cumshot-on-my-ass-%f0%9f%92%a6-you-know-where-to-get-it-%f0%9f%94%97%f0%9f%92%96",
                gid);

        // Test directory name on disk (filesystem safe sanitized as the ripper will
        // do).
        String directoryName = Utils.filesystemSafe(ripper.getHost() + "_" + gid);
        Assertions.assertEquals(
                "fapwiz_post_miaipanema_new-pov-couch-sex-with-perfect-cumshot-on-my-ass-f09f92a6-you-know-where-to-",
                directoryName);
    }

    // Test User pages GetGID

    @Test
    public void testUserGetGID1_Simple() throws IOException, URISyntaxException {
        // Test a "simple" username that is all letters.
        URL url = new URI("https://fapwiz.com/petiteasiantravels/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        Assertions.assertEquals("user_petiteasiantravels", ripper.getGID(url));
    }

    @Test
    public void testUserGetGID2_Numbers() throws IOException, URISyntaxException {
        // Test a more complex username that contains numbers.
        URL url = new URI("https://fapwiz.com/bimeat1998/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        Assertions.assertEquals("user_bimeat1998", ripper.getGID(url));
    }

    @Test
    public void testUserGetGID3_HyphensAndNumbers() throws IOException, URISyntaxException {
        // Test a more complex username that contains hyphens and numbers.
        URL url = new URI("https://fapwiz.com/used-airport-4076/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        Assertions.assertEquals("user_used-airport-4076", ripper.getGID(url));
    }

    @Test
    public void testUserGetGID4_Underscores() throws IOException, URISyntaxException {
        // Test a more complex username that contains underscores.
        URL url = new URI("https://fapwiz.com/desperate_bug_7776/").toURL();
        FapwizRipper ripper = new FapwizRipper(url);
        Assertions.assertEquals("user_desperate_bug_7776", ripper.getGID(url));
    }
}
