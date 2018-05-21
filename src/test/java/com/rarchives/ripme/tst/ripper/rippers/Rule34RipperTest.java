package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.Rule34Ripper;

public class Rule34RipperTest extends RippersTest {
    public void testShesFreakyRip() throws IOException {
        Rule34Ripper ripper = new Rule34Ripper(new URL("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo"));
        testRipper(ripper);
    }

    public void testGetGID() throws IOException {
        URL url = new URL("https://rule34.xxx/index.php?page=post&s=list&tags=bimbo");
        Rule34Ripper ripper = new Rule34Ripper(url);
        assertEquals("bimbo", ripper.getGID(url));
    }

}
