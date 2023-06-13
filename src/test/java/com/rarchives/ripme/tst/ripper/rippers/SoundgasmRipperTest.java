package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.RedditRipper;
import com.rarchives.ripme.ripper.rippers.SoundgasmRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SoundgasmRipperTest extends RippersTest {

    @Test
    @Tag("flaky")
    public void testSoundgasmURLs() throws IOException, URISyntaxException {
        SoundgasmRipper ripper = new SoundgasmRipper(new URI("https://soundgasm.net/u/_Firefly_xoxo/Rambles-with-my-Lovense").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testRedditSoundgasmURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("https://www.reddit.com/r/gonewildaudio/comments/kn1bvj/f4m_mistress_controlled_my_lovense_while_i_tried/").toURL());
        testRipper(ripper);
    }
}
