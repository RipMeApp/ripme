package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.NfsfwRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NfsfwRipperTest extends RippersTest {

    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/291 -- nfsfw 'account suspended' error; disabled flaky test in CI")
    public void testNfsfwRip() throws IOException, URISyntaxException {
        NfsfwRipper ripper = new NfsfwRipper(new URI("http://nfsfw.com/gallery/v/Kitten/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://nfsfw.com/gallery/v/Kitten/").toURL();
        NfsfwRipper ripper = new NfsfwRipper(url);
        Assertions.assertEquals("Kitten", ripper.getGID(url));
        url = new URI("http://nfsfw.com/gallery/v/Kitten").toURL();
        Assertions.assertEquals("Kitten", ripper.getGID(url));
        url = new URI("http://nfsfw.com/gallery/v/Kitten/gif_001/").toURL();
        Assertions.assertEquals("Kitten__gif_001", ripper.getGID(url));
        url = new URI("http://nfsfw.com/gallery/v/Kitten/gif_001/").toURL();
        Assertions.assertEquals("Kitten__gif_001", ripper.getGID(url));
    }
}
