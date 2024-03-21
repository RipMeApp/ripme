package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.NsfwXxxRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class NsfwXxxRipperTest extends RippersTest {
    @Test
    public void testNsfwXxxUser() throws IOException, URISyntaxException {
        NsfwXxxRipper ripper = new NsfwXxxRipper(new URI("https://nsfw.xxx/user/smay3991").toURL());
        testRipper(ripper);
    }
}
