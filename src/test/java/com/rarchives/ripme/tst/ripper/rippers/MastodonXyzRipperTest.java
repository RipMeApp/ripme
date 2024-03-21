package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.MastodonXyzRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MastodonXyzRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        MastodonXyzRipper ripper = new MastodonXyzRipper(new URI("https://mastodon.xyz/@artwo/media").toURL());
        testRipper(ripper);
    }
}
