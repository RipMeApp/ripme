package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * For ripping VSCO pictures.
 */
public class VscoRipper extends AbstractHTMLRipper {

    int pageNumber = 1;
    JSONObject profileJSON;


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
                LOGGER.debug("Failed to convert " + url.toString() + " to external form.");
            }
            
        } else {
            String username = getUserName();
            String userTkn = getUserTkn(username);
            String siteID = getSiteID(userTkn, username);
            while (true) {
                profileJSON = getProfileJSON(userTkn, username, Integer.toString(pageNumber), siteID);
                for (int i = 0; i < profileJSON.getJSONArray("media").length(); i++) {
                    toRip.add("https://" + profileJSON.getJSONArray("media").getJSONObject(i).getString("responsive_url"));
                }
                if (pageNumber * 1000 > profileJSON.getInt("total")) {
                    return toRip;
                }
                pageNumber++;
            }


        }

        return toRip;
    }

    private String getUserTkn(String username) {
        String userTokenPage = "https://vsco.co/content/Static";
        Map<String,String> responseCookies = new HashMap<>();
        try {
            Response resp = Http.url(userTokenPage).ignoreContentType().response();
            responseCookies = resp.cookies();
            return responseCookies.get("vs");
        } catch (IOException e) {
            LOGGER.error("Could not get user tkn");
            return null;
        }
    }

    private String getUserName() {
        Pattern p = Pattern.compile("^https?://vsco.co/([a-zA-Z0-9-]+)(/gallery)?(/)?");
        Matcher m = p.matcher(url.toExternalForm());

        if (m.matches()) {
            String user = m.group(1);
            return user;
        }
        return null;
    }

    private JSONObject getProfileJSON(String tkn, String username, String page, String siteId) {
        String size = "1000";
        String purl = "https://vsco.co/ajxp/" + tkn + "/2.0/medias?site_id=" + siteId + "&page=" + page + "&size=" + size;
        Map<String,String> cookies = new HashMap<>();
        cookies.put("vs", tkn);
        try {
            JSONObject j = Http.url(purl).cookies(cookies).getJSON();
            return j;
        } catch (IOException e) {
            LOGGER.error("Could not profile images");
            return null;
        }
    }

    private String getSiteID(String tkn, String username) {
        Map<String,String> cookies = new HashMap<>();
        cookies.put("vs", tkn);
        try {
            JSONObject j = Http.url("https://vsco.co/ajxp/" + tkn + "/2.0/sites?subdomain=" + username).cookies(cookies).getJSON();
            return Integer.toString(j.getJSONArray("sites").getJSONObject(0).getInt("id"));
        } catch (IOException e) {
            LOGGER.error("Could not get site id");
            return null;
        }
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
                LOGGER.debug("Found image URL: " + givenURL);
                break;//immediately stop after getting URL (there should only be 1 image to be downloaded)
            }
        }
        
        //Means website changed, things need to be fixed.
        if (result.isEmpty()){
            LOGGER.error("Could not find image URL at: " + url);
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
        Pattern p = Pattern.compile("^https?://vsco\\.co/([a-zA-Z0-9-]+)/media/([a-zA-Z0-9]+)");
        Matcher m = p.matcher(url.toExternalForm());
        
        if (m.matches()){
            // Return the text contained between () in the regex
            String user = m.group(1);
            String imageNum = m.group(2).substring(0, 5);//first 5 characters should be enough to make each rip unique
            return user + "/" + imageNum;
        }
        
        //Member profile (Usernames should all be different, so this should work.
        p = Pattern.compile("^https?://vsco.co/([a-zA-Z0-9-]+)(/gallery)?(/)?");
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
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
    
}
