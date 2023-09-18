package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ImxtoRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

class ImxtoRipperTest extends RippersTest {

    @Test
    public void getGID() throws IOException {
        URL url = new URL("https://imx.to/i/48q78k");
        ImxtoRipper imxtoImageRipper = new ImxtoRipper(url);

        Assertions.assertEquals("48q78k", imxtoImageRipper.getGID(url));
    }
}