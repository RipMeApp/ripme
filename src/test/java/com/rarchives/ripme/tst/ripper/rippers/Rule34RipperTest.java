package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.Rule34Ripper;
import com.rarchives.ripme.utils.Utils;

public class Rule34RipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRule34Rip() throws IOException, URISyntaxException {
        Rule34Ripper ripper = new Rule34Ripper(
                new URI("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo").toURL();
        Rule34Ripper ripper = new Rule34Ripper(url);
        Assertions.assertEquals("bimbo", ripper.getGID(url));
    }

    @Test
    public void testGetAPIUrlWithCredentials() throws IOException, URISyntaxException {
        Utils.setConfigString("rule34.api_key", "testapikey123");
        Utils.setConfigString("rule34.user_id", "12345");
        try {
            URL url = new URI("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo").toURL();
            Rule34Ripper ripper = new Rule34Ripper(url);

            // Trigger loadConfig() via getFirstPage(); HTTP call will fail in test env
            try {
                ripper.getFirstPage();
            } catch (IOException e) {
                // Expected in test environment
            }

            String apiUrlString = ripper.getAPIUrl().toExternalForm();
            Assertions.assertTrue(apiUrlString.contains("api_key=testapikey123"),
                    "API URL should contain api_key parameter");
            Assertions.assertTrue(apiUrlString.contains("user_id=12345"),
                    "API URL should contain user_id parameter");
        } finally {
            Utils.setConfigString("rule34.api_key", "");
            Utils.setConfigString("rule34.user_id", "");
        }
    }

    @Test
    public void testGetAPIUrlWithoutCredentials() throws IOException, URISyntaxException {
        Utils.setConfigString("rule34.api_key", "");
        Utils.setConfigString("rule34.user_id", "");
        try {
            URL url = new URI("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo").toURL();
            Rule34Ripper ripper = new Rule34Ripper(url);

            try {
                ripper.getFirstPage();
            } catch (IOException e) {
                // Expected in test environment
            }

            String apiUrlString = ripper.getAPIUrl().toExternalForm();
            Assertions.assertFalse(apiUrlString.contains("api_key="),
                    "API URL should not contain api_key when unconfigured");
            Assertions.assertFalse(apiUrlString.contains("user_id="),
                    "API URL should not contain user_id when unconfigured");
        } finally {
            Utils.setConfigString("rule34.api_key", "");
            Utils.setConfigString("rule34.user_id", "");
        }
    }
}
