package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * For ripping VSCO pictures.
 */
public class VscoRipper extends AbstractHTMLRipper{

    private static final String DOMAIN = "vsco.co",
                        HOST   = "vsco";
    
    public VscoRipper(URL url) throws IOException{
        super(url);
    }
    
    /**
     * Checks to see if VscoRipper can Rip specified url.
     * @param url
     * @return True if can rip.
     *         False if cannot rip.
     */
    @Override
    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
            return false;
        }
        // Ignores personalized things (e.g. login, feed) and store page
        // Allows links to user profiles and links to images.
        //@TODO: Add support for journals and collections.
        String u = url.toExternalForm();
        return !u.contains("/store/")    ||
               !u.contains("/feed/")     ||
               !u.contains("/login/")    ||
               !u.contains("/journal/")   ||
               !u.contains("/collection/")||
               !u.contains("/images/")    ||
                u.contains("/media/");   
        
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        //no sanitization needed.
        return url;
    }

    /**
     * <p>Gets the direct URL of full-sized image through the <meta> tag.</p>
     * When expanding future functionality (e.g. support from journals), put everything into this method.
     * @param page
     * @return 
     */
    @Override
    public List<String> getURLsFromPage(Document page){
        List<String> toRip = new ArrayList<>();
        //If user wanted to rip single image
        if (url.toString().contains("/media/")){
            try {
                toRip.add(vscoImageToURL(url.toExternalForm()));
            } catch (IOException ex) {
                logger.debug("Failed to convert " + url.toString() + " to external form.");
            }
            
        } else {//want to rip a member profile
            /*
            String baseURL = "https://vsco.co";


            //Find all the relative links, adds Base URL, then adds them to an ArrayList
            List<URL> relativeLinks = new ArrayList<>();
            Elements links = page.getElementsByTag("a");

            
            for(Element link : links){
                System.out.println(link.toString());
                //if link includes "/media/", add it to the list
                if (link.attr("href").contains("/media")) {
                    try {
                        String relativeURL = vscoImageToURL(link.attr("href"));
                        toRip.add(baseURL + relativeURL);
                    } catch (IOException ex) {
                        logger.debug("Could not add \"" + link.toString() + "\" to list for ripping.");
                    }
                }
            }
            */
            logger.debug("Sorry, RipMe currently only supports ripping single images.");
            
            
        }

        return toRip;
    }

    private String vscoImageToURL(String url) throws IOException{
        Document page = Jsoup.connect(url).userAgent(USER_AGENT)
                                          .get();
        //create Elements filled only with Elements with the "meta" tag.
        Elements metaTags = page.getElementsByTag("meta");
        String result = "";

        for(Element metaTag : metaTags){
            //find URL inside meta-tag with property of "og:image"
            if (metaTag.attr("property").equals("og:image")){
                String givenURL = metaTag.attr("content");
                givenURL = givenURL.replaceAll("\\?h=[0-9]+", "");//replace the "?h=xxx" tag at the end of the URL (where each x is a number)
                
                result = givenURL;
                logger.debug("Found image URL: " + givenURL);
                break;//immediatly stop after getting URL (there should only be 1 image to be downloaded)
            }
        }
        
        //Means website changed, things need to be fixed.
        if (result.isEmpty()){
            logger.error("Could not find image URL at: " + url);
        }
        
        return result;
        
    }
    
    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        
        //Single Image
        Pattern p = Pattern.compile("^https?://vsco\\.co/([a-zA-Z0-9]+)/media/([a-zA-Z0-9]+)");
        Matcher m = p.matcher(url.toExternalForm());
        
        if (m.matches()){
            // Return the text contained between () in the regex
            String user = m.group(1);
            String imageNum = m.group(2).substring(0, 5);//first 5 characters should be enough to make each rip unique
            return user + "/" + imageNum;
        }
        
        //Member profile (Usernames should all be different, so this should work.
        p = Pattern.compile("^https?://vsco.co/([a-zA-Z0-9]+)/images/[0-9]+");
        m = p.matcher(url.toExternalForm());
        
        if (m.matches()){
            String user = m.group(1);
            return user;
        }
        
        throw new MalformedURLException("Expected a URL to a single image or to a member profile, got " + url + " instead");
            
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }
    
    @Override
    public Document getNextPage(Document doc) throws IOException {
        return super.getNextPage(doc);
    }
    
    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
    
}
