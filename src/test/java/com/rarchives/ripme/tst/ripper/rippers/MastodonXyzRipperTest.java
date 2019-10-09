package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MastodonXyzRipper;
import org.junit.jupiter.api.Test;

public class MastodonXyzRipperTest extends RippersTest {
    @Test
    public void testRip() throws IOException {
        MastodonXyzRipper ripper = new MastodonXyzRipper(new URL("https://mastodon.xyz/@artwo/media"));
        testRipper(ripper);
    }
}
