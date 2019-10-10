package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.BaraagRipper;
import org.junit.jupiter.api.Test;

public class BaraagRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        BaraagRipper ripper = new BaraagRipper(new URL("https://baraag.net/@darkshadow777/media"));
        testRipper(ripper);
    }
}
