package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rarchives.ripme.ripper.rippers.HentaiNexusRipper;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class HentainexusRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testHentaiNexusJson() throws IOException {
        List<URL> testURLs = new ArrayList<>();
        testURLs.add(new URL("https://hentainexus.com/view/9202"));
        testURLs.add(new URL("https://hentainexus.com/read/9202"));
        testURLs.add(new URL("https://hentainexus.com/view/9202#001"));
        testURLs.add(new URL("https://hentainexus.com/read/9202#001"));

        for (URL url : testURLs) {

            HentaiNexusRipper ripper = new HentaiNexusRipper(url);

            boolean testOK = false;
            try {

                String jsonEncodedString = ripper.getJsonEncodedStringFromPage();
                String jsonDecodedString = ripper.decodeJsonString(jsonEncodedString);
                JSONObject json = new JSONObject(jsonDecodedString);
                // Fail test if JSON empty
                testOK = !json.isEmpty();

            } catch (Exception e) {
                // Fail test if JSON invalid, not present or other errors
                testOK  = false;
            }

            Assert.assertEquals(true, testOK);
        }

    }
}
