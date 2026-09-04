package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class HitomiRipper extends AbstractHTMLRipper {

    private static final Logger logger = LogManager.getLogger(HitomiRipper.class);

    private static final String DEFAULT_DOMAIN = "gold-usergeneratedcontent.net";
    private static final Pattern GID_PATTERN = Pattern.compile("https?://hitomi\\.la/(?:cg|doujinshi|gamecg|manga|galleries|reader)/([^/?#]+?)(?:\\.html)?(?:[/?#]|$)");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("(\\d+)");
    private static final Pattern GG_B_PATTERN = Pattern.compile("b:\\s*'([^']+)'");
    private static final Pattern GG_CASE_PATTERN = Pattern.compile("case\\s+(\\d+):");

    private String galleryId = "";
    private String ggB = "";
    private Set<Integer> ggCases = new HashSet<>();
    private String cdnDomain = DEFAULT_DOMAIN;

    public HitomiRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hitomi";
    }

    @Override
    public String getDomain() {
        return "hitomi.la";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Matcher m = GID_PATTERN.matcher(url.toExternalForm());
        if (m.find()) {
            String idCandidate = m.group(1);
            Matcher numMatcher = NUMERIC_PATTERN.matcher(idCandidate);
            if (numMatcher.find()) {
                galleryId = numMatcher.group(1);
                return galleryId;
            }
            galleryId = idCandidate;
            return galleryId;
        }
        throw new MalformedURLException("Expected hitomi URL format: " +
                "https://hitomi.la/(cg|doujinshi|gamecg|manga|galleries|reader)/ID.html - got " + url + " instead");
    }

    private void fetchGgJs() {
        try {
            String ggJsUrl = "https://ltn." + cdnDomain + "/gg.js";
            String ggJs = Http.url(ggJsUrl).ignoreContentType().response().body();
            Matcher bMatcher = GG_B_PATTERN.matcher(ggJs);
            if (bMatcher.find()) {
                ggB = bMatcher.group(1);
            }
            Matcher caseMatcher = GG_CASE_PATTERN.matcher(ggJs);
            Set<Integer> cases = new HashSet<>();
            while (caseMatcher.find()) {
                cases.add(Integer.parseInt(caseMatcher.group(1)));
            }
            ggCases = cases;
        } catch (Exception e) {
            logger.warn("Failed to fetch or parse gg.js from " + cdnDomain, e);
        }
    }

    private int ggM(int g) {
        return ggCases.contains(g) ? 1 : 0;
    }

    private String ggS(String hash) {
        if (hash == null || hash.length() < 3) {
            return "";
        }
        String lastThree = hash.substring(hash.length() - 3);
        String reordered = "" + lastThree.charAt(2) + lastThree.charAt(0) + lastThree.charAt(1);
        return Integer.toString(Integer.parseInt(reordered, 16), 10);
    }

    private String subdomainFromHash(String hash, String ext) {
        String retval = "";
        if ("webp".equals(ext)) {
            retval = "w";
        } else if ("avif".equals(ext)) {
            retval = "a";
        }

        if (hash == null || hash.length() < 3) {
            return retval + (1 + ggM(0));
        }

        String sub = hash.substring(hash.length() - 3);
        String gHex = "" + sub.charAt(2) + sub.charAt(0) + sub.charAt(1);
        try {
            int g = Integer.parseInt(gHex, 16);
            int m = ggM(g);
            return retval + (1 + m);
        } catch (NumberFormatException e) {
            return retval + (1 + ggM(0));
        }
    }

    public String urlFromHash(JSONObject imageObj) {
        String hash = imageObj.getString("hash");
        int hasWebp = imageObj.optInt("haswebp", 0);
        int hasAvif = imageObj.optInt("hasavif", 0);

        String ext;
        if (hasWebp == 1) {
            ext = "webp";
        } else if (hasAvif == 1) {
            ext = "avif";
        } else {
            String name = imageObj.optString("name", "");
            ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "webp";
        }

        String sub = subdomainFromHash(hash, ext);
        String b = ggB;
        if (b != null && !b.isEmpty() && !b.endsWith("/")) {
            b += "/";
        }
        return "https://" + sub + "." + cdnDomain + "/" + (b != null ? b : "") + ggS(hash) + "/" + hash + "." + ext;
    }

    @Override
    public Document getFirstPage() throws IOException, URISyntaxException {
        getGID(url);
        fetchGgJs();
        String jsonUrl = "https://ltn." + cdnDomain + "/galleries/" + galleryId + ".js";
        return Http.url(new URI(jsonUrl).toURL()).ignoreContentType().get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        String raw = doc.text();
        int jsonStart = raw.indexOf('[');
        int jsonEnd = raw.lastIndexOf(']');
        JSONArray json_data;
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            json_data = new JSONArray(raw.substring(jsonStart, jsonEnd + 1));
        } else {
            String json = raw.replaceAll("var\\s+galleryinfo\\s*=", "").trim();
            if (json.startsWith("{")) {
                JSONObject obj = new JSONObject(json);
                json_data = obj.optJSONArray("files");
            } else {
                json_data = new JSONArray(json);
            }
        }

        if (json_data != null) {
            for (int i = 0; i < json_data.length(); i++) {
                JSONObject img = json_data.getJSONObject(i);
                result.add(urlFromHash(img));
            }
        }

        return result;
    }

    @Override
    public String getAlbumTitle(URL url) throws MalformedURLException, URISyntaxException {
        try {
            getGID(url);
            String jsonUrl = "https://ltn." + cdnDomain + "/galleries/" + galleryId + ".js";
            String raw = Http.url(new URI(jsonUrl).toURL()).ignoreContentType().response().body();
            String json = raw.replaceAll("var\\s+galleryinfo\\s*=", "").trim();
            if (json.startsWith("{")) {
                JSONObject obj = new JSONObject(json);
                String title = obj.optString("title", "");
                if (!title.isEmpty()) {
                    return getHost() + "_" + galleryId + "_" + title;
                }
            }
        } catch (Exception e) {
            logger.info("Falling back for album title", e);
        }
        return super.getAlbumTitle(url);
    }

    @Override
    public void downloadURL(URL url, int index) {
        String referer = "https://hitomi.la/reader/" + galleryId + ".html";
        addURLToDownload(url, getPrefix(index), "", referer, null);
    }
}
