package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.FivehundredpxRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FivehundredpxRipperTest extends RippersTest {
    @Test @Disabled("Ripper is broken. See https://github.com/RipMeApp/ripme/issues/438")
    public void test500pxAlbum() throws IOException {
        FivehundredpxRipper ripper = new FivehundredpxRipper(new URL("https://marketplace.500px.com/alexander_hurman"));
        testRipper(ripper);
    }
}