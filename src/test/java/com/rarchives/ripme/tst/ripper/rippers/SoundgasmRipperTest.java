package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.RedditRipper;
import com.rarchives.ripme.ripper.rippers.SoundgasmRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class SoundgasmRipperTest extends RippersTest {

    @Test
    @Tag("flaky")
    public void testSoundgasmURLs() throws IOException, URISyntaxException {
        SoundgasmRipper ripper = new SoundgasmRipper(new URI("https://soundgasm.net/u/HTMLExamples/Making-Text-into-a-Soundgasm-Audio-Link").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testRedditSoundgasmURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("https://www.reddit.com/user/Mistress_Minerva/").toURL());
        testRipper(ripper);
    }
}
