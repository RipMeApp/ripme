package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ImxtoImageRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

class ImxtoImageRipperTest extends RippersTest {

    @Test
    public void getGID() throws IOException {
        URL url = new URL("https://imx.to/i/48q78k");
        ImxtoImageRipper imxtoImageRipper = new ImxtoImageRipper(url);

        Assertions.assertEquals("48q78k", imxtoImageRipper.getGID(url));
    }
}