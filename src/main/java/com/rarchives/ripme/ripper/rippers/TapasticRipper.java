package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

class TapasticEpisode {
    int index;
    int id;    
    String title;
    String filename;
    public TapasticEpisode(int index, int id, String title) {
        this.index=index;
        this.id=id;
        this.title=title;
        this.filename=title // Windows filenames may not contain any of these...
                .replace("\\", "")
                .replace("/", "")
                .replace(":", "")
                .replace("*", "")
                .replace("?", "")
                .replace("\"", "")
                .replace("<", "")
                .replace(">", "")
                .replace("|", "");
    }
}

public class TapasticRipper extends AbstractHTMLRipper {

    private List<TapasticEpisode> episodes=new ArrayList<TapasticEpisode>();

    public TapasticRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getDomain() {
        return "tapastic.com";
    }

    @Override
    public String getHost() {
        return "tapastic";
    }

    @Override
    public Document getFirstPage() throws IOException {
        return Http.url(url).get();
    }

    @Override
    public List<String> getURLsFromPage(Document page) {
        List<String> urls = new ArrayList<String>();
        Elements scripts=page.select("script");
        for(Element script: scripts) {
            String text=script.data();
            if(text.contains("var _data")) {
                String[] lines=text.split("\n");
                for(String line:lines) {
                    String trimmed=line.trim();
                    if(trimmed.startsWith("episodeList : ")) {
                        JSONArray json_episodes=new JSONArray(trimmed.substring("episodeList : ".length()));
                        for(int i=0;i<json_episodes.length();i++) {
                            JSONObject obj=json_episodes.getJSONObject(i);
                            TapasticEpisode episode=new TapasticEpisode(i, obj.getInt("id"), obj.getString("title"));
                            episodes.add(episode);
                            urls.add("http://tapastic.com/episode/"+episode.id);
                        }
                    }
                }
                break;
            }
        }
        return urls;
    }

    @Override
    public void downloadURL(URL url, int index) {
        try {
        Document doc = Http.url(url).get();
        Elements images=doc.select("article.ep-contents img");
        for(int i=0;i<images.size();i++) {
            String link=images.get(i).attr("src");
            String postfix=String.format(" %d-%d ", i+1,images.size());
            TapasticEpisode episode=episodes.get(index-1);
            addURLToDownload(new URL(link), getPrefix(index)+episode.filename+postfix+" ");
        }
        } catch (IOException e) {
            logger.error("[!] Exception while loading/parsing " + this.url,e);
        }

    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^http://tapastic.com/series/(.*)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected tapastic.com URL format: "
                + "tapastic.com/series/name - got " + url + " instead");
    }
}
