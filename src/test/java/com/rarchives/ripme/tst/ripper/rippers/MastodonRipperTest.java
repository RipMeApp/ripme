package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MastodonRipper;
import org.junit.jupiter.api.Test;

public class MastodonRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        MastodonRipper ripper = new MastodonRipper(new URL("https://mastodon.social/@pythonhub/media"));
        testRipper(ripper);
    }
}
