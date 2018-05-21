package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ShesFreakyRipper;

public class ShesFreakyRipperTest extends RippersTest {
    // https://github.com/RipMeApp/ripme/issues/254
    /*
    public void testShesFreakyRip() throws IOException {
        ShesFreakyRipper ripper = new ShesFreakyRipper(new URL("http://www.shesfreaky.com/gallery/nicee-snow-bunny-579NbPjUcYa.html"));
        testRipper(ripper);
    }
    */

    public void testGetGID() throws IOException {
        URL url = new URL("http://www.shesfreaky.com/gallery/nicee-snow-bunny-579NbPjUcYa.html");
        ShesFreakyRipper ripper = new ShesFreakyRipper(url);
        assertEquals("nicee-snow-bunny-579NbPjUcYa", ripper.getGID(url));
    }
}
