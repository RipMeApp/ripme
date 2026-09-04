package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.HitomiRipper;

public class HitomiRipperTest extends RippersTest {

    @Test
    public void testGetGID() throws MalformedURLException, URISyntaxException, IOException {
        HitomiRipper ripper = new HitomiRipper(new URI("https://hitomi.la/galleries/975973.html").toURL());
        Assertions.assertEquals("975973", ripper.getGID(new URI("https://hitomi.la/galleries/975973.html").toURL()));
        Assertions.assertEquals("975973", ripper.getGID(new URI("https://hitomi.la/reader/975973.html").toURL()));
        Assertions.assertEquals("975973", ripper.getGID(new URI("https://hitomi.la/cg/test-title-975973.html").toURL()));
        Assertions.assertEquals("975973", ripper.getGID(new URI("https://hitomi.la/doujinshi/test-975973.html").toURL()));
        Assertions.assertEquals("975973", ripper.getGID(new URI("https://hitomi.la/manga/test-975973.html").toURL()));
        Assertions.assertEquals("975973", ripper.getGID(new URI("https://hitomi.la/gamecg/test-975973.html").toURL()));
    }

    @Test
    public void testGetGIDMalformed() throws IOException, URISyntaxException {
        HitomiRipper ripper = new HitomiRipper(new URI("https://hitomi.la/galleries/975973.html").toURL());
        Assertions.assertThrows(MalformedURLException.class, () -> {
            ripper.getGID(new URI("https://hitomi.la/index-all.html").toURL());
        });
        Assertions.assertThrows(MalformedURLException.class, () -> {
            ripper.getGID(new URI("https://hitomi.la/").toURL());
        });
    }

    @Test
    public void testGetURLsFromPageDeterministic() throws IOException, URISyntaxException {
        HitomiRipper ripper = new HitomiRipper(new URI("https://hitomi.la/galleries/975973.html").toURL());
        ripper.getGID(new URI("https://hitomi.la/galleries/975973.html").toURL());

        String jsonPayload = "var galleryinfo = {\"id\":975973,\"title\":\"Test Gallery\",\"files\":[" +
                "{\"name\":\"01.png\",\"hash\":\"2fdb01d761148ce291c4377387f1a31bd050b0c29313b94a057164c2bf266b77\",\"haswebp\":1,\"hasavif\":1}," +
                "{\"name\":\"02.jpg\",\"hash\":\"a219bae8232c709a808cf7bf14ace6485832ba469cfa627f4fab6b1e4299fe4b\",\"haswebp\":1,\"hasavif\":0}" +
                "]}";
        Document doc = Jsoup.parse(jsonPayload);
        List<String> urls = ripper.getURLsFromPage(doc);
        Assertions.assertEquals(2, urls.size());
        Assertions.assertTrue(urls.get(0).endsWith(".webp"));
        Assertions.assertTrue(urls.get(0).contains("gold-usergeneratedcontent.net"));
        Assertions.assertTrue(urls.get(1).endsWith(".webp"));
    }

    @Test
    public void testUrlFromHashDeterministic() throws IOException, URISyntaxException {
        HitomiRipper ripper = new HitomiRipper(new URI("https://hitomi.la/galleries/975973.html").toURL());

        JSONObject img1 = new JSONObject();
        img1.put("name", "01.png");
        img1.put("hash", "2fdb01d761148ce291c4377387f1a31bd050b0c29313b94a057164c2bf266b77");
        img1.put("haswebp", 1);
        img1.put("hasavif", 1);

        String url1 = ripper.urlFromHash(img1);
        Assertions.assertTrue(url1.startsWith("https://w"));
        Assertions.assertTrue(url1.contains(".gold-usergeneratedcontent.net/"));
        Assertions.assertTrue(url1.endsWith("/1975/2fdb01d761148ce291c4377387f1a31bd050b0c29313b94a057164c2bf266b77.webp"));

        JSONObject img2 = new JSONObject();
        img2.put("name", "02.jpg");
        img2.put("hash", "a219bae8232c709a808cf7bf14ace6485832ba469cfa627f4fab6b1e4299fe4b");
        img2.put("haswebp", 0);
        img2.put("hasavif", 1);

        String url2 = ripper.urlFromHash(img2);
        Assertions.assertTrue(url2.startsWith("https://a"));
        Assertions.assertTrue(url2.endsWith("/3044/a219bae8232c709a808cf7bf14ace6485832ba469cfa627f4fab6b1e4299fe4b.avif"));
    }

    @Test
    @Tag("flaky")
    public void testRipLive() throws IOException, URISyntaxException {
        HitomiRipper ripper = new HitomiRipper(new URI("https://hitomi.la/galleries/975973.html").toURL());
        testRipper(ripper);
        Assertions.assertEquals("975973", ripper.getGID(new URI("https://hitomi.la/galleries/975973.html").toURL()));
    }
}
