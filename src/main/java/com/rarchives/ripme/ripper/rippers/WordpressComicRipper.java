package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
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
    String pageTitle = "";

    public WordpressComicRipper(URL url) throws IOException {
        super(url);
    }

    // Test links
    // http://www.totempole666.com/comic/first-time-for-everything-00-cover/
    // http://buttsmithy.com/archives/comic/p1
    // http://themonsterunderthebed.net/?comic=test-post
    // http://prismblush.com/comic/hella-trap-pg-01/
    // http://www.konradokonski.com/sawdust/
    // http://www.konradokonski.com/wiory/
    // http://freeadultcomix.com/finders-feepaid-in-full-sparrow/
    // http://comics-xxx.com/republic-rendezvous-palcomix-star-wars-xxx/

    public static List<String> explicit_domains = Arrays.asList("www.totempole666.com",
        "buttsmithy.com", "themonsterunderthebed.net", "prismblush.com", "www.konradokonski.com", "freeadultcomix.com",
        "thisis.delvecomic.com", "comics-xxx.com");

    @Override
    public String getHost() {
        String host = url.toExternalForm().split("/")[2];
        return host;
    }

    @Override
    public String getDomain() {
        String host = url.toExternalForm().split("/")[2];
        return host;
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

            Pattern konradokonskiPat = Pattern.compile("https?://www.konradokonski.com/sawdust/comic/([a-zA-Z0-9_-]*)/?$");
            Matcher konradokonskiMat = konradokonskiPat.matcher(url.toExternalForm());
            if (konradokonskiMat.matches()) {
                return true;
            }

            Pattern buttsmithyPat = Pattern.compile("https?://buttsmithy.com/archives/comic/([a-zA-Z0-9_-]*)/?$");
            Matcher buttsmithyMat = buttsmithyPat.matcher(url.toExternalForm());
            if (buttsmithyMat.matches()) {
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
        }

        return false;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException {
        Pattern totempole666Pat = Pattern.compile("(?:https?://)?(?:www\\.)?totempole666.com\\/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher totempole666Mat = totempole666Pat.matcher(url.toExternalForm());
        if (totempole666Mat.matches()) {
            return "totempole666.com" + "_" + "The_cummoner";
        }

        Pattern buttsmithyPat = Pattern.compile("https?://buttsmithy.com/archives/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher buttsmithyMat = buttsmithyPat.matcher(url.toExternalForm());
        if (buttsmithyMat.matches()) {
            return "buttsmithy.com" + "_" + "Alfie";
        }

        Pattern konradokonskiSawdustPat = Pattern.compile("http://www.konradokonski.com/sawdust/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher konradokonskiSawdustMat = konradokonskiSawdustPat.matcher(url.toExternalForm());
        if (konradokonskiSawdustMat.matches()) {
            return "konradokonski.com_sawdust";
        }

        Pattern konradokonskiWioryPat = Pattern.compile("http://www.konradokonski.com/wiory/comic/([a-zA-Z0-9_-]*)/?$");
        Matcher konradokonskiWioryMat = konradokonskiWioryPat.matcher(url.toExternalForm());
        if (konradokonskiWioryMat.matches()) {
            return "konradokonski.com_wiory";
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

        Pattern comicsxxxPat = Pattern.compile("https?://comics-xxx.com/([a-zA-Z0-9_\\-]*)/?$");
        Matcher comicsxxxMat = comicsxxxPat.matcher(url.toExternalForm());
        if (comicsxxxMat.matches()) {
            return getHost() + "_" + comicsxxxMat.group(1);
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
        if (getHost().contains("www.totempole666.com")
                || getHost().contains("buttsmithy.com")
                || getHost().contains("themonsterunderthebed.net")
                || getHost().contains("prismblush.com")
                || getHost().contains("www.konradokonski.com")
                || getHost().contains("thisis.delvecomic.com")) {
            elem = doc.select("a.comic-nav-next").first();
            if (elem == null) {
                throw new IOException("No more pages");
            }
            nextPage = elem.attr("href");
        }
        if (nextPage == "") {
            throw new IOException("No more pages");
        } else {
            return Http.url(nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<String>();
        if (getHost().contains("www.totempole666.com")
                || getHost().contains("buttsmithy.com")
                || getHost().contains("themonsterunderthebed.net")
                || getHost().contains("prismblush.com")
                || getHost().contains("www.konradokonski.com")
                || getHost().contains("thisis.delvecomic.com")) {
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
        }

        // freeadultcomix gets it own if because it needs to add http://freeadultcomix.com to the start of each link
        if (url.toExternalForm().contains("freeadultcomix.com")) {
            for (Element elem : doc.select("div.single-post > p > img.aligncenter")) {
                result.add("http://freeadultcomix.com" + elem.attr("src"));
            }
        }

        if (url.toExternalForm().contains("comics-xxx.com")) {
            for (Element elem : doc.select("div.single-post > center > p > img")) {
                result.add(elem.attr("src"));
            }
        }
        return result;
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

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }
}
