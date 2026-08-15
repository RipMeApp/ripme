
package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.rarchives.ripme.ripper.rippers.RedditRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.container.mp4.MovieCreator;

public class RedditRipperTest extends RippersTest {

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/253
    public void testRedditSubredditRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("http://www.reddit.com/r/nsfw_oc").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky") // https://github.com/RipMeApp/ripme/issues/253
    public void testRedditSubredditTopRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("http://www.reddit.com/r/nsfw_oc/top?t=all").toURL());
        testRipper(ripper);
    }

    @Test
    @Disabled
    public void testRedditPostRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("http://www.reddit.com/r/UnrealGirls/comments/1ziuhl/in_class_veronique_popa/").toURL());
        testRipper(ripper);
    }

    /**testRedditSubredditRip:19
     * GFYCAT TEST Tests a good GfycatURL (no "/gifs/detail")
     * 
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedditGfyGoodURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/bottesting/comments/7msozf/good_link/").toURL());
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testSelfPostRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/gonewildstories/comments/oz7d97/f_18_finally_having_a_normal_sex_life/").toURL()
        );
        testRipper(ripper);
    }

    @Test
    @Tag("flaky")
    public void testSelfPostAuthorRip() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(new URI("https://www.reddit.com/user/ickybabie_").toURL());
        testRipper(ripper);
    }

    /**
     * GFYCAT TEST Tests a Bad URL with the "/gifs/detail" inside.
     * 
     * @throws IOException
     */
    @Test
    @Tag("flaky")
    public void testRedditGfyBadURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/bottesting/comments/7msmhi/bad_link/").toURL());
        testRipper(ripper);
    }

    /**
     * GFYCAT TEST Tests a gfycat URL with the gifdeliverynetwork/redgifs hosted video
     *
     * @throws IOException
     */
    @Test
    public void testRedditGfycatRedirectURL() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/r/NSFW_GIF/comments/ennwsa/gorgeous_tits/").toURL());
    }

    @Test
    @Tag("flaky")
    public void testRedditGallery() throws IOException, URISyntaxException {
        RedditRipper ripper = new RedditRipper(
                new URI("https://www.reddit.com/gallery/hrrh23").toURL());
        testRipper(ripper);
    }

    /**
     * Regression test for #219 (ripmeapp2/ripme): v.redd.it videos with a
     * separate audio track were downloaded silently (no audio muxed in).
     * Drives the actual (private) manifest-parsing and download/mux methods
     * via reflection against a real, known-to-have-audio v.redd.it post, and
     * verifies the muxed output actually contains both a video and an audio
     * track.
     */
    @Test
    @Tag("flaky")
    public void testRedditVideoAudioMux() throws Exception {
        RedditRipper ripper = new RedditRipper(new URI("https://www.reddit.com/r/test/").toURL());
        ripper.setup();

        Class<?> manifestClass = Class.forName("com.rarchives.ripme.ripper.rippers.RedditRipper$RedditVideoManifest");

        Method parseMethod = RedditRipper.class.getDeclaredMethod("parseRedditVideoMPD", String.class);
        parseMethod.setAccessible(true);
        Object manifest = parseMethod.invoke(ripper, "https://v.redd.it/ygd513q4brhe1");
        Assertions.assertNotNull(manifest, "Failed to parse DASH manifest");

        Field audioField = manifestClass.getDeclaredField("audioURL");
        audioField.setAccessible(true);
        Assertions.assertNotNull(audioField.get(manifest), "Expected an audio track in the manifest");

        Path outputPath = Files.createTempFile("ripme-test-reddit-video-", ".mp4");
        try {
            Method downloadMethod = RedditRipper.class.getDeclaredMethod("downloadRedditVideo", manifestClass, Path.class);
            downloadMethod.setAccessible(true);
            downloadMethod.invoke(ripper, manifest, outputPath);

            Assertions.assertTrue(Files.exists(outputPath) && Files.size(outputPath) > 0,
                    "Muxed output file was not created");

            Movie movie = MovieCreator.build(outputPath.toAbsolutePath().toString());
            boolean hasVideo = movie.getTracks().stream().anyMatch(t -> "vide".equals(t.getHandler()));
            boolean hasAudio = movie.getTracks().stream().anyMatch(t -> "soun".equals(t.getHandler()));
            Assertions.assertTrue(hasVideo, "Muxed file is missing a video track");
            Assertions.assertTrue(hasAudio, "Muxed file is missing an audio track");
        } finally {
            Files.deleteIfExists(outputPath);
        }
    }
}
