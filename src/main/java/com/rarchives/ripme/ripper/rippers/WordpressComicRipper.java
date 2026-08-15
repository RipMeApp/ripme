package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class WordpressComicRipper extends AbstractHTMLRipper {
    private String pageTitle = "";

    public WordpressComicRipper(URL url) throws IOException {
        super(url);
    }

    // Test links (see also WordpressComicRipperTest.java)
    // http://www.totempole666.com/comic/first-time-for-everything-00-cover/
    // http://buttsmithy.com/archives/comic/p1
    // http://themonsterunderthebed.net/?comic=test-post
    // http://prismblush.com/comic/hella-trap-pg-01/
    // http://www.konradokonski.com/sawdust/comic/get-up/
    // http://www.konradokonski.com/wiory/comic/08182008/
    // http://freeadultcomix.com/finders-feepaid-in-full-sparrow/
    // http://thisis.delvecomic.com/NewWP/comic/in-too-deep/
    // http://shipinbottle.pepsaga.com/?p=281

    private static List<String> explicit_domains = Arrays.asList(
        "www.totempole666.com",
        "buttsmithy.com",
        "incase.buttsmithy.com",
        "themonsterunderthebed.net",
        "prismblush.com",
        "www.konradokonski.com",
        "freeadultcomix.com",
        "thisis.delvecomic.com",
        "shipinbottle.pepsaga.com",
        "8muses.download",
        "spyingwithlana.com",
        "comixfap.net",
            "manytoon.me",
            "manhwahentai.me"
    );

    private static List<String> theme1 = Arrays.asList(
            "www.totempole666.com",
            "buttsmithy.com",
            "themonsterunderthebed.net",
            "prismblush.com",
            "www.konradokonski.com",
            "thisis.delvecomic.com",
            "spyingwithlana.com"
    );

    private static List<String> webtoonTheme = Arrays.asList(
            "manhwahentai.me",
            "manytoon.me"
    );

    @Override
    public String getHost() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public String getDomain() {
        return url.toExternalForm().split("/")[2];
    }

    @Override
    public boolean canRip(URL url) {
        String url_name = url.toExternalForm();
        if (explicit_domains.contains(url_name.split("/")[2])) {

            Pattern totempole666Pat = Pattern.compile("https?://www\\.totempole666.com/comic/([a-zA-Z0-9_-]*)/?$");
            Matcher totempole666Mat = totempole666Pat.matcher(url.toExternalForm());
            if (totempole666Mat.matches()) {
                return true;
            }

            Pattern konradokonskiPat = Pattern.compile("https?://www.konradokonski.com/([a-zA-Z0-9_-]*)/comic/([a-zA-Z0-9_-]*)/?$");
            Matcher konradokonskiMat = konradokonskiPat.matcher(url.toExternalForm());
            if (konradokonskiMat.matches()) {
                return true;
            }

            // This is hardcoded because it starts on the first page, unlike all the other
            // konradokonski which start on the last page
            konradokonskiPat = Pattern.compile("https?://www.konradokonski.com/aquartzbead/?$");
            konradokonskiMat = konradokonskiPat.matcher(url.toExternalForm());
            if (konradokonskiMat.matches()) {
                return true;
            }

            Pattern buttsmithyPat = Pattern.compile("https?://buttsmithy.com/archives/comic/([a-zA-Z0-9_-]*)/?$");
            Matcher buttsmithyMat = buttsmithyPat.matcher(url.toExternalForm());
            if (buttsmithyMat.matches()) {
                return true;
            }

            Pattern buttsmithyIncasePat = Pattern.compile("https?://incase.buttsmithy.com/comic/([a-zA-Z0-9_-]*)/?$");
            Matcher buttsmithyIncaseMat = buttsmithyIncasePat.matcher(url.toExternalForm());
            if (buttsmithyIncaseMat.matches()) {
                return true;
            }

            Pattern theMonsterUnderTheBedPat = Pattern.compile("https?://themonsterunderthebed.net/\\?comic=([a-zA-Z0-9_-]*)/?$");
            Matcher theMonsterUnderTheBedMat = theMonsterUnderTheBedPat.matcher(url.toExternalForm());
            if (theMonsterUnderTheBedMat.matches()) {
                return true;
            }

            Pattern prismblushPat = Pattern.compile("https?://prismblush.com/comic/([a-zA-Z0-9_-]*)/?$");
            Matcher prismblushMat = prismblushPat.matcher(url.toExternalForm());
            if (prismblushMat.matches()) {
                return true;
            }

            Pattern freeadultcomixPat = Pattern.compile("https?://freeadultcomix.com/([a-zA-Z0-9_\\-]*)/?$");
            Matcher freeadultcomixMat = freeadultcomixPat.matcher(url.toExternalForm());
            if (freeadultcomixMat.matches()) {
                return true;
            }

            Pattern thisisDelvecomicPat = Pattern.compile("https?://thisis.delvecomic.com/NewWP/comic/([a-zA-Z0-9_\\-]*)/?$");
            Matcher thisisDelvecomicMat = thisisDelvecomicPat.matcher(url.toExternalForm());
            if (thisisDelvecomicMat.matches()) {
                return true;
            }

            Pattern comicsxxxPat = Pattern.compile("https?://comics-xxx.com/([a-zA-Z0-9_\\-]*)/?$");
            Matcher comicsxxxMat = comicsxxxPat.matcher(url.toExternalForm());
            if (comicsxxxMat.matches()) {
                return true;
            }

            Pattern shipinbottlePat = Pattern.compile("https?://shipinbottle.pepsaga.com/\\?p=([0-9]*)/?$");
            Matcher shipinbottleMat =shipinbottlePat.matcher(url.toExternalForm());
            if (shipinbottleMat.matches()) {
                return true;
            }

            Pattern eight_musesPat = Pattern.compile("https?://8muses.download/([a-zA-Z0-9_-]+)/?$");
            Matcher eight_musesMat = eight_musesPat.matcher(url.toExternalForm());
            if (eight_musesMat.matches()) {
                return true;
            }

            Pattern spyingwithlanaPat = Pattern.compile("https?://spyingwithlana.com/comic/([a-zA-Z0-9_-]+)/?$");
            Matcher spyingwithlanaMat = spyingwithlanaPat.matcher(url.toExternalForm());
            if (spyingwithlanaMat.matches()) {
                return true;
            }

            Pattern pa = Pattern.compile("^https?://8muses.download/\\?s=([a-zA-Z0-9-]*)");
            Matcher ma = pa.matcher(url.toExternalForm());
            if (ma.matches()) {
                return true;
            }

            Pattern pat = Pattern.compile("https?://8muses.download/page/\\d+/\\?s=([a-zA-Z0-9-]*)");
            Matcher mat = pat.matcher(url.toExternalForm());
            if (mat.matches()) {
                return true;
            }

            pat = Pattern.compile("https://8muses.download/category/([a-zA-Z0-9-]*)/?");
            mat = pat.matcher(url.toExternalForm());
            if (mat.matches()) {
                return true;
            }

            pat = Pattern.compile("https?://comixfap.net/([a-zA-Z0-9-]*)/?");
            mat = pat.matcher(url.toExternalForm());
            if (mat.matches()) {
                return true;
            }

            pat = Pattern.compile("https?://manytoon.me/manhwa/([a-zA-Z0-9_-]+)/chapter-\\d+/?$");
            mat = pat.matcher(url.toExternalForm());
            if (mat.matches()) {
                return true;
            }

            pat = Pattern.compile("https://manhwahentai.me/webtoon/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-])+/?");
            mat = pat.matcher(url.toExternalForm());
            if (mat.matches()) {
                return true;
            }
        }


        return false;
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        Pattern pa = Pattern.compile("^https?://8muses.download/\\?s=([a-zA-Z0-9-]*)");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            return true;
        }

        Pattern pat = Pattern.compile("https?://8muses.download/page/\\d+/\\?s=([a-zA-Z0-9-]*)");
        Matcher mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return true;
        }

        pat = Pattern.compile("https://8muses.download/category/([a-zA-Z0-9-]*)/?");
        mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return true;
        }

        return false;
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select("#post_masonry > article > div > figure > a")) {
            urlsToAddToQueue.add(elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        Pattern totempole666Pat = Pattern.compile("(?:https?://)?(?:www\\.)?totempole666.com/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher totempole666Mat = totempole666Pat.matcher(url.toExternalForm());
        if (totempole666Mat.matches()) {
            return "totempole666.com" + "_" + "The_cummoner";
        }

        Pattern buttsmithyPat = Pattern.compile("https?://buttsmithy.com/archives/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher buttsmithyMat = buttsmithyPat.matcher(url.toExternalForm());
        if (buttsmithyMat.matches()) {
            return "buttsmithy.com" + "_" + "Alfie";
        }

        Pattern konradokonskiPat = Pattern.compile("http://www.konradokonski.com/([a-zA-Z]+)/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher konradokonskiMat = konradokonskiPat.matcher(url.toExternalForm());
        if (konradokonskiMat.matches()) {
            return "konradokonski.com_" + konradokonskiMat.group(1);
        }

        konradokonskiPat = Pattern.compile("https?://www.konradokonski.com/aquartzbead/?$");
        konradokonskiMat = konradokonskiPat.matcher(url.toExternalForm());
        if (konradokonskiMat.matches()) {
            return "konradokonski.com_aquartzbead";
        }

        Pattern freeadultcomixPat = Pattern.compile("https?://freeadultcomix.com/([a-zA-Z0-9_\\-]*)/?$");
        Matcher freeadultcomixMat = freeadultcomixPat.matcher(url.toExternalForm());
        if (freeadultcomixMat.matches()) {
            return getHost() + "_" + freeadultcomixMat.group(1);
        }

        Pattern thisisDelvecomicPat = Pattern.compile("https?://thisis.delvecomic.com/NewWP/comic/([a-zA-Z0-9_\\-]*)/?$");
        Matcher thisisDelvecomicMat = thisisDelvecomicPat.matcher(url.toExternalForm());
        if (thisisDelvecomicMat.matches()) {
            return getHost() + "_" + "Delve";
        }

        Pattern prismblushPat = Pattern.compile("https?://prismblush.com/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher prismblushMat = prismblushPat.matcher(url.toExternalForm());
        if (prismblushMat.matches()) {
            return getHost() + "_" + prismblushMat.group(1);
        }

        Pattern buttsmithyIncasePat = Pattern.compile("https?://incase.buttsmithy.com/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher buttsmithyIncaseMat = buttsmithyIncasePat.matcher(url.toExternalForm());
        if (buttsmithyIncaseMat.matches()) {
            return getHost() + "_" + buttsmithyIncaseMat.group(1).replaceAll("-page-\\d", "").replaceAll("-pg-\\d", "");
        }

        Pattern comicsxxxPat = Pattern.compile("https?://comics-xxx.com/([a-zA-Z0-9_\\-]*)/?$");
        Matcher comicsxxxMat = comicsxxxPat.matcher(url.toExternalForm());
        if (comicsxxxMat.matches()) {
            return getHost() + "_" + comicsxxxMat.group(1);
        }

        Pattern shipinbottlePat = Pattern.compile("https?://shipinbottle.pepsaga.com/\\?p=([0-9]*)/?$");
        Matcher shipinbottleMat =shipinbottlePat.matcher(url.toExternalForm());
        if (shipinbottleMat.matches()) {
            return getHost() + "_" + "Ship_in_bottle";
        }

        Pattern eight_musesPat = Pattern.compile("https?://8muses.download/([a-zA-Z0-9_-]+)/?$");
        Matcher eight_musesMat = eight_musesPat.matcher(url.toExternalForm());
        if (eight_musesMat.matches()) {
            return getHost() + "_" + eight_musesMat.group(1);
        }

        Pattern spyingwithlanaPat = Pattern.compile("https?://spyingwithlana.com/comic/([a-zA-Z0-9_-]+)/?$");
        Matcher spyingwithlanaMat = spyingwithlanaPat.matcher(url.toExternalForm());
        if (spyingwithlanaMat.matches()) {
            return "spyingwithlana_" + spyingwithlanaMat.group(1).replaceAll("-page-\\d", "");
        }

        Pattern pat = Pattern.compile("https?://comixfap.net/([a-zA-Z0-9-]*)/?");
        Matcher mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return "comixfap_" + mat.group(1);
        }

        pat = Pattern.compile("https?://manytoon.me/manhwa/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-])+/?");
        mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return "manytoon.me_" + mat.group(1) + "_" + mat.group(2);
        }

        pat = Pattern.compile("https://manhwahentai.me/webtoon/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-])+/?");
        mat = pat.matcher(url.toExternalForm());
        if (mat.matches()) {
            return "manhwahentai.me_" + mat.group(1) + "_" + mat.group(2);
        }


        return super.getAlbumTitle(url);
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        String url_name = url.toExternalForm();
        // We shouldn't need to return any GID
        if (explicit_domains.contains(url_name.split("/")[2])) {
            return "";
        }
        throw new MalformedURLException("You should never see this error message");
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        String nextPage = "";
        Element elem = null;
        if (theme1.contains(getHost())) {
            elem = doc.select("a.comic-nav-next").first();
            if (elem == null) {
                throw new IOException("No more pages");
            }
            nextPage = elem.attr("href");
        } else if (getHost().contains("shipinbottle.pepsaga.com")) {
            elem = doc.select("td.comic_navi_right > a.navi-next").first();
            if (elem == null) {
                throw new IOException("No more pages");
            }
            nextPage = elem.attr("href");
        }

        if (nextPage.equals("")) {
            throw new IOException("No more pages");
        } else {
            return Http.url(nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        if (theme1.contains(getHost())) {
            Element elem = doc.select("div.comic-table > div#comic > a > img").first();
            // If doc is the last page in the comic then elem.attr("src") returns null
            // because there is no link <a> to the next page
            if (elem == null) {
                elem = doc.select("div.comic-table > div#comic > img").first();
            }
            // Check if this is a site where we can get the page number from the title
            if (url.toExternalForm().contains("buttsmithy.com")) {
                // Set the page title
                pageTitle = doc.select("meta[property=og:title]").attr("content");
                pageTitle = pageTitle.replace(" ", "");
                pageTitle = pageTitle.replace("P", "p");
            }
            if (url.toExternalForm().contains("www.totempole666.com")) {
                String postDate = doc.select("span.post-date").first().text().replaceAll("/", "_");
                String postTitle = doc.select("h2.post-title").first().text().replaceAll("#", "");
                pageTitle = postDate + "_" + postTitle;
            }
            if (url.toExternalForm().contains("themonsterunderthebed.net")) {
                pageTitle = doc.select("title").first().text().replaceAll("#", "");
                pageTitle = pageTitle.replace("“", "");
                pageTitle = pageTitle.replace("”", "");
                pageTitle = pageTitle.replace("The Monster Under the Bed", "");
                pageTitle = pageTitle.replace("–", "");
                pageTitle = pageTitle.replace(",", "");
                pageTitle = pageTitle.replace(" ", "");
            }

            result.add(elem.attr("src"));
        } else if (webtoonTheme.contains(getHost())) {
            for (Element el : doc.select("img.wp-manga-chapter-img")) {
                LOGGER.info(el.toString());
                result.add(getWebtoonImageUrl(el));
            }
        }

        // freeadultcomix gets it own if because it needs to add http://freeadultcomix.com to the start of each link
        // TODO review the above comment which no longer applies -- see if there's a refactoring we should do here.
        if (url.toExternalForm().contains("freeadultcomix.com")) {
            for (Element elem : doc.select("div.post-texto > p > noscript > img[class*=aligncenter]")) {
                result.add(elem.attr("src"));
            }
        } else if (url.toExternalForm().contains("comics-xxx.com")) {
            for (Element elem : doc.select("div.single-post > center > p > img")) {
                result.add(elem.attr("src"));
            }
        } else if (url.toExternalForm().contains("shipinbottle.pepsaga.com")) {
            for (Element elem : doc.select("div#comic > a > img")) {
                result.add(elem.attr("src"));
            }
        } else if (url.toExternalForm().contains("8muses.download")) {
            for (Element elem : doc.select("div.popup-gallery > figure > a")) {
                result.add(elem.attr("href"));
            }
        } else if (url.toExternalForm().contains("http://comixfap.net")) {
            // Some pages on comixfap do use unite-gallery and others don't, so we have a loop for each
            for (Element elem : doc.select("div.entry-content > div.dgwt-jg-gallery > figure > a")) {
                result.add(elem.attr("href"));
            }
            for (Element elem : doc.select(".unite-gallery > img")) {
                result.add(elem.attr("src"));
            }
        }



        return result;
    }

    private String getWebtoonImageUrl(Element el) {
        if (el.attr("src").contains("http")) {
            return el.attr("src");
        } else {
            return el.attr("data-src");
        }
    }

    @Override
    public void downloadURL(URL url, int index) {
        // Download the url with the page title as the prefix
        // so we can download them in any order (And don't have to rerip the whole site to update the local copy)
        if (getHost().contains("buttsmithy.com")
                || getHost().contains("www.totempole666.com")
                || getHost().contains("themonsterunderthebed.net")) {
            addURLToDownload(url, pageTitle + "_");
        }

        // If we're ripping a site where we can't get the page number/title we just rip normally
        addURLToDownload(url, getPrefix(index));


    }
}
