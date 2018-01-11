package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.AlbumRipper;
import com.rarchives.ripme.utils.Http;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * For ripping VSCO members' pages.
 */
public class VscoRipper extends AbstractHTMLRipper{

    private static final String DOMAIN = "vsco.co",
                        HOST   = "vsco";
    
    public VscoRipper(URL url) throws IOException{
        super(url);
    }
    
    @Override
    public boolean canRip(URL url) {
        if (!url.getHost().endsWith(DOMAIN)) {
            return false;
        }
        // Ignores personalized things (e.g. login, feed) and store page
        // Allows links to user profiles and links to images.
        //TODO: Add support for journals and collections.
        String u = url.toExternalForm();
        return !u.contains("/store")    ||
               !u.contains("/feed")     ||
               !u.contains("/login")    ||
               !u.contains("/journal")   ||
               !u.contains("/collection")||
                u.contains("images")    ||
                u.contains("media");   
        
    }

    @Override
    public URL sanitizeURL(URL url) throws MalformedURLException {
        //no sanitization needed.
        return url;
    }

    @Override
    public void rip() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://vsco\\.co/([a-zA-Z0-9]+)/media/([a-zA-Z0-9]+)");
        Matcher m = p.matcher(url.toExternalForm());
        if (!m.matches()){
            throw new MalformedURLException("Expected " + DOMAIN + " URL format: " +
                        "vsco.co/username/media/postNumber - got " + url + " instead");
            
        }
        // Return the text contained between () in the regex
        String user = m.group(1);
        String imageNum = m.group(2);
        
        return user + "/" + imageNum;
        
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
    public List<String> getURLsFromPage(Document page) {
        List<String> result = new ArrayList<>();
        
        //get them from page
        for(Element el : page.select("meta.og:image")){
            //MUST replace im.vsco instead of just "im" because the URL to image could contain string "im"
            result.add(
                    el.attr("content").replaceFirst("im.vsco", "images.vsco")); //sanitize
            
        }
        

        
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
    
}
