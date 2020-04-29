  package com.rarchives.ripme.ripper.rippers;

  import java.io.IOException;
  import java.net.MalformedURLException;
  import java.net.URL;
  import java.util.ArrayList;
  import java.util.List;
  import java.util.regex.Matcher;
  import java.util.regex.Pattern;

  import org.jsoup.nodes.Document;
  import org.jsoup.nodes.Element;

  import com.rarchives.ripme.ripper.AbstractHTMLRipper;
  import com.rarchives.ripme.utils.Http;

  public class CoedcherryRipper extends AbstractHTMLRipper {

    public CoedcherryRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "coedcherry";
    }
    @Override
    public String getDomain() {
        return "www.coedcherry.com";
    }

    @Override
    public boolean hasQueueSupport() {
        return true;
    }

    @Override
    public boolean pageContainsAlbums(URL url) {
        LOGGER.info("Page contains albums");

        // Site, Search, Models  pattern
        // https://www.coedcherry.com/site/abrianna
        // https://www.coedcherry.com/models/emma-nicholls
        // https://www.coedcherry.com/pics/search/oily

        Pattern pa1 = Pattern.compile("https://www.coedcherry.com/(site|pics/search|models)/([a-zA-Z0-9_-])+/?");
        Matcher ma1 = pa1.matcher(url.toExternalForm());
        if (ma1.matches()) {
          LOGGER.info("Site album");
          return true;
        }

        // Tag pattern
        Pattern pa2 = Pattern.compile("https://www.coedcherry.com/galleries\\?tags=[a-zA-Z0-9+_-]+");
        Matcher ma2 = pa2.matcher(url.toExternalForm());
        if (ma2.matches()) {
          LOGGER.info("Tag album");
          return true;
        }

        LOGGER.info("No album found");
        return false;
    }

    @Override
    public List<String> getAlbumsToQueue(Document doc) {
        LOGGER.info("Albums to Queue");
        List<String> urlsToAddToQueue = new ArrayList<>();
        for (Element elem : doc.select(".thumbs > figure > a")) {
            LOGGER.info(elem.attr("href"));
            //urlsToAddToQueue.add(getDomain() + elem.attr("href"));
            urlsToAddToQueue.add(elem.attr("href"));
        }
        return urlsToAddToQueue;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
      LOGGER.info("Reviewing for GID");

        // Single Gallery
        // https://www.coedcherry.com/pics/emma-strips-reading-paper
        Pattern p = Pattern.compile("https://www.coedcherry.com/pics/([a-zA-Z0-9_-]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            LOGGER.info("Match Single Gallery");
            return m.group(1);
        }

        // Site, Model gallery
        // https://www.coedcherry.com/models/emma-nicholls/pics/emma-strips-reading-paper
        // https://www.coedcherry.com/site/abrianna/pics/abrianna-shows-us-juicy-round-jugs
        Pattern pa = Pattern.compile("https://www.coedcherry.com/(site|models)/([a-zA-Z0-9_-]+)/pics/([a-zA-Z0-9_-]+)");
        Matcher ma = pa.matcher(url.toExternalForm());
        if (ma.matches()) {
            LOGGER.info("Match Site or Model Gallery");
            return ma.group(1)+'_'+ma.group(2)+'_'+ma.group(3);
        }


        Pattern pa1 = Pattern.compile("https://www.coedcherry.com/(site|pics/search|models)/([a-zA-Z0-9_-])+");
        Matcher ma1 = pa1.matcher(url.toExternalForm());
        if (ma1.matches()) {
          LOGGER.info("Site album");
          return ma1.group(1)+'_'+ma1.group(2);
        }

        // Tag pattern
        Pattern pa2 = Pattern.compile("https://www.coedcherry.com/galleries\\?tags=([a-zA-Z0-9+_-]+)");
        Matcher ma2 = pa2.matcher(url.toExternalForm());
        if (ma2.matches()) {
          LOGGER.info("Tag album");
          return ma2.group(1);
        }


        throw new MalformedURLException("Expected coedcherry.com URL format: " +
                        "www.coedcherry.com/pics/albumid - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> imageURLs = new ArrayList<>();
        for (Element thumb : doc.select("#gallery > .thumbs > figure > a")) {
            String image = thumb.attr("href").replaceAll("thumbs", "images");
            image = image.replace("_b", "_o");
            image = image.replaceAll("\\d-s", "i");
            imageURLs.add(image);
        }
        return imageURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
  }
