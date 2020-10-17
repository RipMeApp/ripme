package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PornpicsRipper extends AbstractHTMLRipper {

<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/com/rarchives/ripme/ripper/rippers/PornpicsRipper.java
    public PornpicsRipper(URL url) throws IOException {
=======
public class KingcomixRipper extends AbstractHTMLRipper {

    public KingcomixRipper(URL url) throws IOException {
>>>>>>> upstream/master:src/main/java/com/rarchives/ripme/ripper/rippers/KingcomixRipper.java
=======
    public PornpicsRipper(URL url) throws IOException {
>>>>>>> upstream/master
        super(url);
    }

    @Override
    public String getHost() {
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/com/rarchives/ripme/ripper/rippers/PornpicsRipper.java
        return "pornpics";
=======
        return "kingcomix";
>>>>>>> upstream/master:src/main/java/com/rarchives/ripme/ripper/rippers/KingcomixRipper.java
=======
        return "pornpics";
>>>>>>> upstream/master
    }

    @Override
    public String getDomain() {
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/com/rarchives/ripme/ripper/rippers/PornpicsRipper.java
        return "pornpics.com";
=======
        return "kingcomix.com";
>>>>>>> upstream/master:src/main/java/com/rarchives/ripme/ripper/rippers/KingcomixRipper.java
=======
        return "pornpics.com";
>>>>>>> upstream/master
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/com/rarchives/ripme/ripper/rippers/PornpicsRipper.java
        Pattern p = Pattern.compile("https?://www.pornpics.com/galleries/([a-zA-Z0-9_-]*)/?");
=======
        Pattern p = Pattern.compile("https://kingcomix.com/([a-zA-Z1-9_-]*)/?$");
>>>>>>> upstream/master:src/main/java/com/rarchives/ripme/ripper/rippers/KingcomixRipper.java
=======
        Pattern p = Pattern.compile("https?://www.pornpics.com/galleries/([a-zA-Z0-9_-]*)/?");
>>>>>>> upstream/master
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
<<<<<<< HEAD
<<<<<<< HEAD:src/main/java/com/rarchives/ripme/ripper/rippers/PornpicsRipper.java
        throw new MalformedURLException("Expected pornpics URL format: " +
                "www.pornpics.com/galleries/ID - got " + url + " instead");
=======
        throw new MalformedURLException("Expected kingcomix URL format: " +
                "kingcomix.com/COMIX - got " + url + " instead");
>>>>>>> upstream/master:src/main/java/com/rarchives/ripme/ripper/rippers/KingcomixRipper.java
=======
        throw new MalformedURLException("Expected pornpics URL format: " +
                "www.pornpics.com/galleries/ID - got " + url + " instead");
>>>>>>> upstream/master
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

<<<<<<< HEAD

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
<<<<<<< HEAD:src/main/java/com/rarchives/ripme/ripper/rippers/PornpicsRipper.java
        for (Element el : doc.select("a.rel-link")) {
            result.add(el.attr("href"));
=======
        for (Element el : doc.select("div.entry-content > p > img")) {
            result.add(el.attr("src"));
>>>>>>> upstream/master:src/main/java/com/rarchives/ripme/ripper/rippers/KingcomixRipper.java
=======
    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        for (Element el : doc.select("a.rel-link")) {
            result.add(el.attr("href"));
>>>>>>> upstream/master
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
