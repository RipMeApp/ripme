package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.VkRipper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class VkRipperTest extends RippersTest {
    // https://github.com/RipMeApp/ripme/issues/252
    // Not supported (error): https://vk.com/helga_model (Profile Page)
    // Not supported (rips nothing): https://vk.com/albums45506334 (all albums under a Profile Page)

    // EXAMPLE: https://vk.com/photos45506334 (all photos for a model)
    // EXAMPLE: https://vk.com/album45506334_0 (a single album - profile pictures)
    // EXAMPLE: https://vk.com/album45506334_00?rev=1 (a single album - wall pictures)
    // EXAMPLE: https://vk.com/album45506334_101886701 (a single album - custom)
    @Test
    @Tag("flaky")
    public void testVkAlbumHttpRip() throws IOException, URISyntaxException {
        VkRipper ripper = new VkRipper(new URI("https://vk.com/album45506334_0").toURL());
        testRipper(ripper);
    }
    @Test
    @Tag("flaky")
    public void testVkPhotosRip() throws IOException, URISyntaxException {
        VkRipper ripper = new VkRipper(new URI("https://vk.com/photos45506334").toURL());
        testRipper(ripper);
    }
    
    @Test
    @Tag("flaky")
    public void testFindJSONObjectContainingPhotoID() throws IOException, URISyntaxException {
        VkRipper ripper = new VkRipper(new URI("http://vk.com/album45506334_0").toURL());
        String json =
                "{\"payload\":[0,[\"album-45984105_268691406\",18,14,[{\"id\":\"-45984105_457345201\",\"base\":\"https://sun9-37.userapi.com/\",\"tagged\":[],\"likes\":0,\"shares\":0,\"o_src\":\"https://sun9-65.userapi.com/c857520/v857520962/10e24c/DPxygc3XW5E.jpg\",\"o_\":[\"https://sun9-65.userapi.com/c857520/v857520962/10e24c/DPxygc3XW5E\",130,98],\"z_src\":\"https://sun9-41.userapi.com/c857520/v857520962/10e24a/EsDDQA36qKI.jpg\",\"z_\":[\"https://sun9-41.userapi.com/c857520/v857520962/10e24a/EsDDQA36qKI\",1280,960],\"w_src\":\"https://sun9-60.userapi.com/c857520/v857520962/10e24b/6ETsA15rAdU.jpg\",\"w_\":[\"https://sun9-60.userapi.com/c857520/v857520962/10e24b/6ETsA15rAdU\",1405,1054]}]]],\"langVersion\":\"4298\"}";
        String responseJson =
                "{\"id\":\"-45984105_457345201\",\"base\":\"https://sun9-37.userapi.com/\",\"tagged\":[],\"likes\":0,\"shares\":0,\"o_src\":\"https://sun9-65.userapi.com/c857520/v857520962/10e24c/DPxygc3XW5E.jpg\",\"o_\":[\"https://sun9-65.userapi.com/c857520/v857520962/10e24c/DPxygc3XW5E\",130,98],\"z_src\":\"https://sun9-41.userapi.com/c857520/v857520962/10e24a/EsDDQA36qKI.jpg\",\"z_\":[\"https://sun9-41.userapi.com/c857520/v857520962/10e24a/EsDDQA36qKI\",1280,960],\"w_src\":\"https://sun9-60.userapi.com/c857520/v857520962/10e24b/6ETsA15rAdU.jpg\",\"w_\":[\"https://sun9-60.userapi.com/c857520/v857520962/10e24b/6ETsA15rAdU\",1405,1054]}";

        Assertions.assertTrue(
                ripper.findJSONObjectContainingPhotoId("-45984105_457345201", new JSONObject(json))
                        .similar(new JSONObject(responseJson)));
    }

    @Test
    public void testGetBestSourceUrl() throws IOException, URISyntaxException {
        VkRipper ripper = new VkRipper(new URI("http://vk.com/album45506334_0").toURL());
        String json =
                "{\"id\":\"-45984105_457345201\",\"base\":\"https://sun9-37.userapi.com/\",\"commcount\":0,\"date\":\"<span class=\\\"rel_date\\\">3 Dec at 1:14 am</span>\",\"tagged\":[],\"attached_tags\":{\"max_tags_per_object\":5},\"o_src\":\"https://sun9-65.userapi.com/c857520/v857520962/10e24c/DPxygc3XW5E.jpg\",\"o_\":[\"https://sun9-65.userapi.com/c857520/v857520962/10e24c/DPxygc3XW5E\",130,98],\"y_src\":\"https://sun9-9.userapi.com/c857520/v857520962/10e249/dUDeuY10s0A.jpg\",\"y_\":[\"https://sun9-9.userapi.com/c857520/v857520962/10e249/dUDeuY10s0A\",807,605],\"z_src\":\"https://sun9-41.userapi.com/c857520/v857520962/10e24a/EsDDQA36qKI.jpg\",\"z_\":[\"https://sun9-41.userapi.com/c857520/v857520962/10e24a/EsDDQA36qKI\",1280,960]}";
        Assertions.assertEquals("https://sun9-41.userapi.com/c857520/v857520962/10e24a/EsDDQA36qKI.jpg",
                ripper.getBestSourceUrl(new JSONObject(json)));
    }
}
