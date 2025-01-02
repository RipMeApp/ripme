package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

/**
 * @author Tushar
 *
 */
public class ComicextraRipper extends AbstractHTMLRipper {

    private static final String FILE_NAME = "page";

    private Pattern p1 =
            Pattern.compile("https:\\/\\/www.comicextra.com\\/comic\\/([A-Za-z0-9_-]+)");
    private Pattern p2 = Pattern.compile(
            "https:\\/\\/www.comicextra.com\\/([A-Za-z0-9_-]+)\\/([A-Za-z0-9_-]+)(?:\\/full)?");
    private UrlType urlType = UrlType.UNKNOWN;
    private List<String> chaptersList = null;
    private int chapterIndex = -1; // index for the chaptersList, useful in getting the next page.
    private int imageIndex = 0; // image index for each chapter images.

    public ComicextraRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "comicextra.com";
    }

    @Override
    public String getHost() {
        return "comicextra";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m1 = p1.matcher(url.toExternalForm());
        if (m1.matches()) {
            // URL is of comic( https://www.comicextra.com/comic/the-punisher-frank-castle-max).
            urlType = UrlType.COMIC;
            return m1.group(1);
        }

        Matcher m2 = p2.matcher(url.toExternalForm());
        if (m2.matches()) {
            // URL is of chapter( https://www.comicextra.com/the-punisher-frank-castle-max/chapter-75).
            urlType = UrlType.CHAPTER;
            return m2.group(1);
        }

        throw new MalformedURLException(
                "Expected comicextra.com url of type: https://www.comicextra.com/comic/some-comic-name\n"
                        + " or https://www.comicextra.com/some-comic-name/chapter-001 got " + url
                        + " instead");
    }

    @Override
    protected Document getFirstPage() throws IOException {
        Document doc = null;

        switch (urlType) {
            case COMIC:
                // For COMIC type url we extract the urls of each chapters and store them in chapters.
                chaptersList = new ArrayList<>();
                Document comicPage = Http.url(url).get();
                Elements elements = comicPage.select("div.episode-list a");
                for (Element e : elements) {
                    chaptersList.add(getCompleteChapterUrl(e.attr("abs:href")));
                }

                // Set the first chapter from the chapterList as the doc.                
                chapterIndex = 0;
                doc = Http.url(chaptersList.get(chapterIndex)).get();
                break;
            case CHAPTER:
                doc = Http.url(url).get();
                break;
            case UNKNOWN:
            default:
                throw new IOException("Unknown url type encountered.");
        }

        return doc;
    }

    @Override
    public Document getNextPage(Document doc) throws IOException, URISyntaxException {
        if (urlType == UrlType.COMIC) {
            ++chapterIndex;
            imageIndex = 0; // Resetting the imagesIndex so that images prefix within each chapter starts from '001_'.
            if (chapterIndex < chaptersList.size()) {
                return Http.url(chaptersList.get(chapterIndex)).get();
            }
        }

        return super.getNextPage(doc);
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {
        List<String> urls = new ArrayList<>();

        if (urlType == UrlType.COMIC || urlType == UrlType.CHAPTER) {
            Elements images = page.select("img.chapter_img");
            for (Element img : images) {
                urls.add(img.attr("src"));
            }
        }

        return urls;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        String subdirectory = getSubDirectoryName();
        String prefix = getPrefix(++imageIndex);

        addURLToDownload(url, subdirectory, null, null, prefix, FILE_NAME, null, Boolean.TRUE);
    }

    /*
     * This function appends /full at the end of the chapters url to get all the images for the
     * chapter in the same Document.
     */
    private String getCompleteChapterUrl(String chapterUrl) {
        if (!chapterUrl.endsWith("/full")) {
            chapterUrl = chapterUrl + "/full";
        }
        return chapterUrl;
    }

    /*
     * This functions returns sub folder name for the current chapter.
     */
    private String getSubDirectoryName() {
        String subDirectory = "";

        if (urlType == UrlType.COMIC) {
            Matcher m = p2.matcher(chaptersList.get(chapterIndex));
            if (m.matches()) {
                subDirectory = m.group(2);
            }
        }

        if (urlType == UrlType.CHAPTER) {
            Matcher m = p2.matcher(url.toExternalForm());
            if (m.matches()) {
                subDirectory = m.group(2);
            }
        }

        return subDirectory;
    }

    /*
     * Enum to classify different types of urls.
     */
    private enum UrlType {
        COMIC, CHAPTER, UNKNOWN
    }
}
