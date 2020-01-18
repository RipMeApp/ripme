package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.json.JSONObject;
import org.json.JSONArray;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;

/**
 * @author owaiswiz
 *
 */
public class FolioRipper extends AbstractJSONRipper {
    public FolioRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "folio";
    }

    @Override
    public String getDomain() {
        return "folio.ink";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https?://(?:www.)?folio.ink/([a-zA-Z0-9]+).*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected folio.ink URL format: " +
            "folio.ink/albumid (e.g: folio.ink/DmBe6i) - got " + url + " instead");
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        String jsonArrayString = Http.url("https://folio.ink/getimages/" + getGID(url)).ignoreContentType().response().body();
        JSONArray imagesArray = new JSONArray(jsonArrayString);
        JSONObject imagesObject = new JSONObject();
        imagesObject.put("images", imagesArray);

        return imagesObject;
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        List<String> result = new ArrayList<String>();
        JSONArray imagesArray = json.getJSONArray("images");

        for (int i = 0; i < imagesArray.length(); i++) {
            JSONObject image = imagesArray.getJSONObject(i);
            result.add(image.getString("image_url"));
        }

        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
