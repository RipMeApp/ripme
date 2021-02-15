package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.PorncomixDotOneRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PorncomixDotOneRipperTest extends RippersTest {
    @Test
    @Disabled("website down?")
    public void testPorncomixAlbum() throws IOException {
        PorncomixDotOneRipper ripper = new PorncomixDotOneRipper(new URL("https://www.porncomix.one/gallery/blacknwhite-make-america-great-again"));
        testRipper(ripper);
    }
}