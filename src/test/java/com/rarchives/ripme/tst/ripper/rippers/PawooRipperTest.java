package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PawooRipper;
import org.junit.jupiter.api.Test;

public class PawooRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        PawooRipper ripper = new PawooRipper(new URL("https://pawoo.net/@halki/media"));
        testRipper(ripper);
    }
}
