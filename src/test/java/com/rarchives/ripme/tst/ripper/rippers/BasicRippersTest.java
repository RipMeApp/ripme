package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.AbstractRipper;
import com.rarchives.ripme.ripper.rippers.DeviantartRipper;
import com.rarchives.ripme.ripper.rippers.EightmusesRipper;
import com.rarchives.ripme.ripper.rippers.FivehundredpxRipper;
import com.rarchives.ripme.ripper.rippers.FuraffinityRipper;
import com.rarchives.ripme.ripper.rippers.GifyoRipper;
import com.rarchives.ripme.ripper.rippers.GirlsOfDesireRipper;
import com.rarchives.ripme.ripper.rippers.HentaifoundryRipper;
import com.rarchives.ripme.ripper.rippers.ImagearnRipper;
import com.rarchives.ripme.ripper.rippers.ImagebamRipper;
import com.rarchives.ripme.ripper.rippers.ImagevenueRipper;
import com.rarchives.ripme.ripper.rippers.ImgboxRipper;
import com.rarchives.ripme.ripper.rippers.ModelmayhemRipper;
import com.rarchives.ripme.ripper.rippers.MotherlessRipper;
import com.rarchives.ripme.ripper.rippers.NfsfwRipper;
import com.rarchives.ripme.ripper.rippers.PhotobucketRipper;
import com.rarchives.ripme.ripper.rippers.PornhubRipper;
import com.rarchives.ripme.ripper.rippers.ShesFreakyRipper;
import com.rarchives.ripme.ripper.rippers.TapasticRipper;
import com.rarchives.ripme.ripper.rippers.TeenplanetRipper;
import com.rarchives.ripme.ripper.rippers.TumblrRipper;
import com.rarchives.ripme.ripper.rippers.TwitterRipper;
import com.rarchives.ripme.ripper.rippers.TwodgalleriesRipper;
import com.rarchives.ripme.ripper.rippers.VidbleRipper;
import com.rarchives.ripme.ripper.rippers.VineRipper;
import com.rarchives.ripme.ripper.rippers.VkRipper;
import com.rarchives.ripme.ripper.rippers.XhamsterRipper;

/**
 * Simple test cases for various rippers.
 * These tests only require a URL, no other special validation.
 */
public class BasicRippersTest extends RippersTest {

    public void testDeviantartAlbum() throws IOException {
        DeviantartRipper ripper = new DeviantartRipper(new URL("http://airgee.deviantart.com/gallery/"));
        testRipper(ripper);
    }
    public void testDeviantartNSFWAlbum() throws IOException {
        // NSFW gallery
        DeviantartRipper ripper = new DeviantartRipper(new URL("http://faterkcx.deviantart.com/gallery/"));
        testRipper(ripper);
    }

    public void testEightmusesAlbum() throws IOException {
        EightmusesRipper ripper = new EightmusesRipper(new URL("http://www.8muses.com/index/category/jab-hotassneighbor7"));
        testRipper(ripper);
        ripper = new EightmusesRipper(new URL("https://www.8muses.com/album/jab-comics/a-model-life"));
        testRipper(ripper);
    }

    /*
    public void testVineboxAlbum() throws IOException {
        FineboxRipper ripper = new FineboxRipper(new URL("http://vinebox.co/u/wi57hMjc2Ka"));
        testRipper(ripper);
    }
    */

    /*
    public void testFineboxAlbum() throws IOException {
        FineboxRipper ripper = new FineboxRipper(new URL("http://finebox.co/u/wi57hMjc2Ka"));
        testRipper(ripper);
    }
    */

    /*
    public void testRedditSubredditRip() throws IOException {
        RedditRipper ripper = new RedditRipper(new URL("http://www.reddit.com/r/nsfw_oc"));
        testRipper(ripper);
    }
    public void testRedditSubredditTopRip() throws IOException {
        RedditRipper ripper = new RedditRipper(new URL("http://www.reddit.com/r/nsfw_oc/top?t=all"));
        testRipper(ripper);
    }
    public void testRedditPostRip() throws IOException {
        RedditRipper ripper = new RedditRipper(new URL("http://www.reddit.com/r/UnrealGirls/comments/1ziuhl/in_class_veronique_popa/"));
        testRipper(ripper);
    }

    public void testTumblrFullRip() throws IOException {
        TumblrRipper ripper = new TumblrRipper(new URL("http://wrouinr.tumblr.com/archive"));
        testRipper(ripper);
    }
    public void testTumblrTagRip() throws IOException {
        TumblrRipper ripper = new TumblrRipper(new URL("http://topinstagirls.tumblr.com/tagged/berlinskaya"));
        testRipper(ripper);
    }
    public void testTumblrPostRip() throws IOException {
        TumblrRipper ripper = new TumblrRipper(new URL("http://sadbaffoon.tumblr.com/post/132045920789/what-a-hoe"));
        testRipper(ripper);
    }
    */

    public void testTwitterUserRip() throws IOException {
        TwitterRipper ripper = new TwitterRipper(new URL("https://twitter.com/danngamber01/media"));
        testRipper(ripper);
    }
    /*
    public void testTwitterSearchRip() throws IOException {
        TwitterRipper ripper = new TwitterRipper(new URL("https://twitter.com/search?q=from%3ADaisyfairymfc%20filter%3Aimages&src=typd"));
        testRipper(ripper);
    }
    */

    public void test500pxAlbum() throws IOException {
        FivehundredpxRipper ripper = new FivehundredpxRipper(new URL("https://marketplace.500px.com/alexander_hurman"));
        testRipper(ripper);
    }

    /*
    public void testFlickrAlbum() throws IOException {
        FlickrRipper ripper = new FlickrRipper(new URL("https://www.flickr.com/photos/leavingallbehind/sets/72157621895942720/"));
        testRipper(ripper);
    }
    */

    public void testFuraffinityAlbum() throws IOException {
        FuraffinityRipper ripper = new FuraffinityRipper(new URL("https://www.furaffinity.net/gallery/mustardgas/"));
        testRipper(ripper);
    }

    /*
    public void testFuskatorAlbum() throws IOException {
        FuskatorRipper ripper = new FuskatorRipper(new URL("http://fuskator.com/full/emJa1U6cqbi/index.html"));
        testRipper(ripper);
    }
    */

    public void testGifyoAlbum() throws IOException {
        GifyoRipper ripper = new GifyoRipper(new URL("http://gifyo.com/PieSecrets/"));
        testRipper(ripper);
    }

    public void testGirlsofdesireAlbum() throws IOException {
        GirlsOfDesireRipper ripper = new GirlsOfDesireRipper(new URL("http://www.girlsofdesire.org/galleries/krillia/"));
        testRipper(ripper);
    }

    public void testHentaifoundryRip() throws IOException {
        HentaifoundryRipper ripper = new HentaifoundryRipper(new URL("http://www.hentai-foundry.com/pictures/user/personalami"));
        testRipper(ripper);
    }

    public void testImagearnRip() throws IOException {
        AbstractRipper ripper = new ImagearnRipper(new URL("http://imagearn.com//gallery.php?id=578682"));
        testRipper(ripper);
    }

    public void testImagebamRip() throws IOException {
        AbstractRipper ripper = new ImagebamRipper(new URL("http://www.imagebam.com/gallery/488cc796sllyf7o5srds8kpaz1t4m78i"));
        testRipper(ripper);
    }

    /*
    public void testImagestashRip() throws IOException {
        AbstractRipper ripper = new ImagestashRipper(new URL("https://imagestash.org/tag/everydayuncensor"));
        testRipper(ripper);
    }
    */

    public void testImagevenueRip() throws IOException {
        AbstractRipper ripper = new ImagevenueRipper(new URL("http://img120.imagevenue.com/galshow.php?gal=gallery_1373818527696_191lo"));
        testRipper(ripper);
    }

    public void testImgboxRip() throws IOException {
        AbstractRipper ripper = new ImgboxRipper(new URL("http://imgbox.com/g/sEMHfsqx4w"));
        testRipper(ripper);
    }

    /*
    public void testMinusUserRip() throws IOException {
        AbstractRipper ripper = new MinusRipper(new URL("http://vampyr3.minus.com/"));
        testRipper(ripper);
        deleteSubdirs(ripper.getWorkingDir());
        deleteDir(ripper.getWorkingDir());
    }
    public void testMinusUserAlbumRip() throws IOException {
        AbstractRipper ripper = new MinusRipper(new URL("http://vampyr3.minus.com/mw7ztQ6xzP7ae"));
        testRipper(ripper);
    }
    public void testMinusUserUploadsRip() throws IOException {
        AbstractRipper ripper = new MinusRipper(new URL("http://vampyr3.minus.com/uploads"));
        testRipper(ripper);
    }
    public void testMinusAlbumRip() throws IOException {
        AbstractRipper ripper = new MinusRipper(new URL("http://minus.com/mw7ztQ6xzP7ae"));
        testRipper(ripper);
    }
    */

    public void testModelmayhemRip() throws IOException {
        AbstractRipper ripper = new ModelmayhemRipper(new URL("http://www.modelmayhem.com/portfolio/520206/viewall"));
        testRipper(ripper);
    }

    public void testMotherlessAlbumRip() throws IOException {
        MotherlessRipper ripper = new MotherlessRipper(new URL("http://motherless.com/G4DAA18D"));
        testRipper(ripper);
    }

    public void testNfsfwRip() throws IOException {
        AbstractRipper ripper = new NfsfwRipper(new URL("http://nfsfw.com/gallery/v/Kitten/"));
        testRipper(ripper);
    }

    public void testPhotobucketRip() throws IOException {
        AbstractRipper ripper = new PhotobucketRipper(new URL("http://s844.photobucket.com/user/SpazzySpizzy/library/Album%20Covers?sort=3&page=1"));
        testRipper(ripper);
        deleteSubdirs(ripper.getWorkingDir());
        deleteDir(ripper.getWorkingDir());
    }

    public void testPornhubRip() throws IOException {
        AbstractRipper ripper = new PornhubRipper(new URL("http://www.pornhub.com/album/428351"));
        testRipper(ripper);
    }

    /*
    public void testSankakuChanRip() throws IOException {
        AbstractRipper ripper = new SankakuComplexRipper(new URL("https://chan.sankakucomplex.com/?tags=cleavage"));
        testRipper(ripper);
    }
    public void testSankakuIdolRip() throws IOException {
        AbstractRipper ripper = new SankakuComplexRipper(new URL("https://idol.sankakucomplex.com/?tags=meme_%28me%21me%21me%21%29_%28cosplay%29"));
        testRipper(ripper);
    }
    */

    public void testShesFreakyRip() throws IOException {
        AbstractRipper ripper = new ShesFreakyRipper(new URL("http://www.shesfreaky.com/gallery/nicee-snow-bunny-579NbPjUcYa.html"));
        testRipper(ripper);
    }

    public void testTapasticRip() throws IOException {
        AbstractRipper ripper = new TapasticRipper(new URL("http://tapastic.com/episode/2139"));
        testRipper(ripper);
    }

    public void testTeenplanetRip() throws IOException {
        AbstractRipper ripper = new TeenplanetRipper(new URL("http://teenplanet.org/galleries/the-perfect-side-of-me-6588.html"));
        testRipper(ripper);
    }

    public void testTwodgalleriesRip() throws IOException {
        AbstractRipper ripper = new TwodgalleriesRipper(new URL("http://www.2dgalleries.com/artist/regis-loisel-6477"));
        testRipper(ripper);
    }

    public void testVidbleRip() throws IOException {
        AbstractRipper ripper = new VidbleRipper(new URL("http://www.vidble.com/album/y1oyh3zd"));
        testRipper(ripper);
    }

    public void testVineRip() throws IOException {
        AbstractRipper ripper = new VineRipper(new URL("https://vine.co/u/954440445776334848"));
        testRipper(ripper);
    }

    public void testVkSubalbumRip() throws IOException {
        VkRipper ripper = new VkRipper(new URL("http://vk.com/album45506334_0"));
        testRipper(ripper);
    }
    public void testVkRootAlbumRip() throws IOException {
        VkRipper ripper = new VkRipper(new URL("https://vk.com/album45506334_0"));
        testRipper(ripper);
    }
    public void testVkPhotosRip() throws IOException {
        VkRipper ripper = new VkRipper(new URL("https://vk.com/photos45506334"));
        testRipper(ripper);
    }

    public void testXhamsterAlbums() throws IOException {
        XhamsterRipper ripper = new XhamsterRipper(new URL("http://xhamster.com/photos/gallery/1462237/alyssa_gadson.html"));
        testRipper(ripper);
    }
}
