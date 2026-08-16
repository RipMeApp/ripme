package com.rarchives.ripme.android;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exercises the generated {@link RipperRegistry} against a handful of real ripper URLs, offline
 * and deterministically. Every URL below was picked and checked by hand against the real ripper
 * source (see android/core/build.gradle.kts's generateRipperRegistry task) so that:
 *
 * <ul>
 *   <li>canRip() and the matching ripper's constructor never touch the network - both are pure
 *       string/regex checks (constructors here run as plain "super(url)"), matching
 *       AbstractRipper's own contract that canRip() must be safe to call speculatively.</li>
 *   <li>exactly one ripper class claims each domain used below. This is not automatic: e.g.
 *       vk.com, pornhub.com and yuvutu.com are each claimed by *two* different ripper classes -
 *       a top-level album ripper (com.rarchives.ripme.ripper.rippers.VkRipper /
 *       .PornhubRipper / .YuvutuRipper) and an unrelated same-named-or-not video ripper
 *       under .video - so those domains were deliberately avoided here in favour of ones with a
 *       single owner.</li>
 * </ul>
 */
public class RipperRegistryTest {

    private static URL url(String s) throws Exception {
        return new URI(s).toURL();
    }

    @Test
    public void resolvesAlbumRippersByDomain() throws Exception {
        Assertions.assertEquals(
                "com.rarchives.ripme.ripper.rippers.ImgurRipper",
                RipperRegistry.getRipper(url("https://imgur.com/a/abc1234")).getClass().getName());
        Assertions.assertEquals(
                "com.rarchives.ripme.ripper.rippers.RedditRipper",
                RipperRegistry.getRipper(url("https://www.reddit.com/r/pics")).getClass().getName());
        Assertions.assertEquals(
                "com.rarchives.ripme.ripper.rippers.E621Ripper",
                RipperRegistry.getRipper(url("https://e621.net/posts?tags=cat")).getClass().getName());
        Assertions.assertEquals(
                "com.rarchives.ripme.ripper.rippers.DanbooruRipper",
                RipperRegistry.getRipper(url("https://danbooru.donmai.us/posts?tags=cat")).getClass().getName());
        Assertions.assertEquals(
                "com.rarchives.ripme.ripper.rippers.EightmusesRipper",
                RipperRegistry.getRipper(url("https://www.8muses.com/comics/album/Test")).getClass().getName());
    }

    @Test
    public void fallsThroughToVideoRippersOnceAlbumRippersAreExhausted() throws Exception {
        // clips.twitch.tv is claimed only by the video ripper (unlike vk.com/pornhub.com/
        // yuvutu.com above), so this genuinely exercises the second loop in getRipper(URL) -
        // see AbstractRipper.java:794-816, which RipperRegistry.getRipper mirrors.
        Assertions.assertEquals(
                "com.rarchives.ripme.ripper.rippers.video.TwitchVideoRipper",
                RipperRegistry.getRipper(url("https://clips.twitch.tv/SomeClipName")).getClass().getName());
    }

    @Test
    public void throwsWhenNoRipperMatches() {
        // No ripper claims instagram.com once InstagramRipper is dropped (confirmed: no other
        // ripper's canRip/getDomain mentions "instagram" anywhere in the source tree).
        Assertions.assertThrows(Exception.class,
                () -> RipperRegistry.getRipper(url("https://www.instagram.com/p/abc123def/")));
    }

    @Test
    public void instagramRipperIsAbsentFromTheRegistry() {
        // Assert directly on the generated lists, not just the URL-throws check above, so this
        // fails loudly (rather than silently passing for the wrong reason) if some *other*
        // ripper ever starts claiming instagram.com.
        Assertions.assertFalse(RipperRegistry.ALBUM_RIPPERS.contains(
                "com.rarchives.ripme.ripper.rippers.InstagramRipper"));
        Assertions.assertFalse(RipperRegistry.VIDEO_RIPPERS.contains(
                "com.rarchives.ripme.ripper.rippers.InstagramRipper"));
    }

    @Test
    public void registryListsAreSortedAndRoughlyComplete() {
        // Sanity check on the generator itself, independent of any single URL: at the time this
        // test was written there were 112 top-level rippers (111 after dropping Instagram) and 8
        // video rippers. Checked with >=, not ==, so this doesn't need updating every time
        // upstream adds a ripper - it only guards against the generator silently producing an
        // empty or badly truncated list.
        Assertions.assertTrue(RipperRegistry.ALBUM_RIPPERS.size() >= 100);
        Assertions.assertTrue(RipperRegistry.VIDEO_RIPPERS.size() >= 5);

        List<String> sortedAlbumRippers = new ArrayList<>(RipperRegistry.ALBUM_RIPPERS);
        Collections.sort(sortedAlbumRippers);
        Assertions.assertEquals(sortedAlbumRippers, RipperRegistry.ALBUM_RIPPERS);

        List<String> sortedVideoRippers = new ArrayList<>(RipperRegistry.VIDEO_RIPPERS);
        Collections.sort(sortedVideoRippers);
        Assertions.assertEquals(sortedVideoRippers, RipperRegistry.VIDEO_RIPPERS);
    }
}
