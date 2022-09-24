package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.NsfwXxxRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class NsfwXxxRipperTest extends RippersTest {
    @Test
    public void testNsfwXxxUser() throws IOException {
        NsfwXxxRipper ripper = new NsfwXxxRipper(new URL("https://nsfw.xxx/user/smay3991"));
        testRipper(ripper);
    }
}
