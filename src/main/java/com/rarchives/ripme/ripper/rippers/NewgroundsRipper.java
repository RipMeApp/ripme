package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewgroundsRipper extends AbstractHTMLRipper {

    private String username = "";  // Name of artist

    // Extensions supported by Newgrounds
    private List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "gif", "jpeg", "jpg");

    // Images are pulled 60 at a time, a new page request is needed when count == 60
    private int pageNumber = 1;
    private int count = 0;


    public NewgroundsRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "newgrounds";
    }

    @Override
    protected String getDomain() {
        return "newgrounds.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(.+).newgrounds.com/?.*");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            this.username = m.group(1);
            return m.group(1);
        }
        throw new MalformedURLException("Expected newgrounds.com URL format: " +
                "username.newgrounds.com/art - got " + url + " instead");
    }

    @Override
    protected Document getFirstPage() throws IOException {
        return Http.url("https://" + this.username + ".newgrounds.com/art").timeout(10*1000).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        if(this.count < 60) {
            throw new IOException("No more pages");
        }
        this.count = 0; // New page found so reset count
        return Http.url("https://" + this.username + ".newgrounds.com/art/page/" + this.pageNumber)
                .header("X-Requested-With", "XMLHttpRequest").get(); // Send header to imitate scrolling
    }

    @Override
    protected List<String> getURLsFromPage(Document page) {

        List<String> imageURLs = new ArrayList<>();
        String documentHTMLString = page.toString().replaceAll("&quot;", "");
        String findStr = "newgrounds.com/art/view/" + this.username;
        int lastIndex = 0;

        // Index where findStr is found; each occasion contains the link to an image
        ArrayList<Integer> indices = new ArrayList<>();

        while(lastIndex != -1){
            lastIndex = documentHTMLString.indexOf(findStr, lastIndex);
            if(lastIndex != -1){
                this.count ++;
                lastIndex += findStr.length();
                indices.add(lastIndex);
            }
        }

        // Retrieve direct URL for image
        for(int i = 0; i < indices.size(); i++){
            String imageUrl = "https://art.ngfiles.com/images/";

            String inLink = "https://www.newgrounds.com/art/view/" + this.username + "/";
            String s;
            if(i == indices.size() - 1){
                s = documentHTMLString.substring(indices.get(i) + 2);
            } else{
                s = documentHTMLString.substring(indices.get(i) + 1, indices.get(i + 1));
            }

            s = s.replaceAll("\n", "").replaceAll("\t", "")
                    .replaceAll("\\\\", "");

            Pattern p = Pattern.compile("(.*?)\" class.*/thumbnails/(.*?)/(.*?)\\.");
            Matcher m = p.matcher(s);

            if (m.lookingAt()) {
                String testURL = m.group(3) + "_" + this.username + "_" + m.group(1);
                testURL = testURL.replace("_full", "");

                // Open new document to get full sized image
                try {
                    Document imagePage = Http.url(inLink + m.group(1)).get();
                    for(String extensions: this.ALLOWED_EXTENSIONS){
                        if(imagePage.toString().contains(testURL + "." + extensions)){
                            imageUrl += m.group(2) + "/" + m.group(3).replace("_full","") + "_" + this.username + "_" + m.group(1) + "." + extensions;
                            imageURLs.add(imageUrl);
                            break;
                        }
                    }

                } catch (IOException e) {
                    LOGGER.error("IO Error on trying to check extension: " + inLink + m.group(1));
                }
            }
        }
        this.pageNumber += 1;
        return imageURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
