package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.NewgroundsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class NewgroundsRipperTest extends RippersTest {
    @Test
    public void testNewgroundsRip() throws IOException {
        NewgroundsRipper ripper = new NewgroundsRipper(new URL("https://zone-sama.newgrounds.com/art"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://zone-sama.newgrounds.com/art");
        NewgroundsRipper ripper = new NewgroundsRipper(url);
        Assertions.assertEquals("zone-sama", ripper.getGID(url));
    }


}
