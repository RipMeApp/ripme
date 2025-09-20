package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.ripper.rippers.NsfwXxxRipper;

public class NsfwXxxRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testNsfwXxxUser() throws IOException, URISyntaxException {
        NsfwXxxRipper ripper = new NsfwXxxRipper(new URI("https://nsfw.xxx/user/smay3991").toURL());
        testRipper(ripper);
    }
}
