package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.RedditRipper;
import org.junit.Test;
import com.rarchives.ripme.ripper.rippers.SoundgasmRipper;

import java.io.IOException;
import java.net.URL;

public class SoundgasmRipperTest extends RippersTest {

    @Test
    public void testSoundgasmURLs() throws IOException {
        SoundgasmRipper ripper = new SoundgasmRipper(new URL("https://soundgasm.net/u/_Firefly_xoxo/Rambles-with-my-Lovense"));
        testRipper(ripper);
    }

    @Test
    public void testRedditSoundgasmURL() throws IOException {
        RedditRipper ripper = new RedditRipper(new URL("https://www.reddit.com/r/gonewildaudio/comments/kn1bvj/f4m_mistress_controlled_my_lovense_while_i_tried/"));
        testRipper(ripper);
    }
}
