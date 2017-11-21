package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.VkRipper;

public class VkRipperTest extends RippersTest {
    // https://github.com/RipMeApp/ripme/issues/252
    // Not supported (error): https://vk.com/helga_model (Profile Page)
    // Not supported (rips nothing): https://vk.com/albums45506334 (all albums under a Profile Page)

    // EXAMPLE: https://vk.com/photos45506334 (all photos for a model)
    // EXAMPLE: https://vk.com/album45506334_0 (a single album - profile pictures)
    // EXAMPLE: https://vk.com/album45506334_00?rev=1 (a single album - wall pictures)
    // EXAMPLE: https://vk.com/album45506334_101886701 (a single album - custom)
    public void testVkAlbumHttpRip() throws IOException {
        VkRipper ripper = new VkRipper(new URL("http://vk.com/album45506334_0"));
        testRipper(ripper);
    }
    public void testVkAlbumHttpsRip() throws IOException {
        VkRipper ripper = new VkRipper(new URL("https://vk.com/album45506334_0"));
        testRipper(ripper);
    }
    public void testVkPhotosRip() throws IOException {
        VkRipper ripper = new VkRipper(new URL("https://vk.com/photos45506334"));
        testRipper(ripper);
    }
}
