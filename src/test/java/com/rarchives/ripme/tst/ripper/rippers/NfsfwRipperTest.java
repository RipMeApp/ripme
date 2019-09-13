package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NfsfwRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NfsfwRipperTest extends RippersTest {

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/291 -- nfsfw 'account suspended' error; disabled flaky test in CI")
    public void testNfsfwRip() throws IOException {
        NfsfwRipper ripper = new NfsfwRipper(new URL("http://nfsfw.com/gallery/v/Kitten/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("http://nfsfw.com/gallery/v/Kitten/");
        NfsfwRipper ripper = new NfsfwRipper(url);
        assertEquals("Kitten", ripper.getGID(url));
        url = new URL("http://nfsfw.com/gallery/v/Kitten");
        assertEquals("Kitten", ripper.getGID(url));
        url = new URL("http://nfsfw.com/gallery/v/Kitten/gif_001/");
        assertEquals("Kitten__gif_001", ripper.getGID(url));
        url = new URL("http://nfsfw.com/gallery/v/Kitten/gif_001/");
        assertEquals("Kitten__gif_001", ripper.getGID(url));
    }
}
