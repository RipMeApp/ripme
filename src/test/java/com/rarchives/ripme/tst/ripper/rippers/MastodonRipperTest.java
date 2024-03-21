package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.MastodonRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MastodonRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        MastodonRipper ripper = new MastodonRipper(new URI("https://mastodon.social/@pythonhub/media").toURL());
        testRipper(ripper);
    }
}
