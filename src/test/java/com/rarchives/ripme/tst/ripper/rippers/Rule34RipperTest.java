package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.Rule34Ripper;

public class Rule34RipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testShesFreakyRip() throws IOException, URISyntaxException {
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

}
