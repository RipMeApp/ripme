package com.rarchives.ripme.ripper.rippers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Base64;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;

public class MadokamiRipper extends AbstractHTMLRipper {
    private static String header;
    private Map<String, String> headers;

    public MadokamiRipper(URL url) throws IOException {
        super(url);
        String authKey = Utils.getConfigString("madokami.auth", null);
        if (authKey == null) {
            throw new IOException("Could not find madokami authentication key (madokami.auth) in configuration in the form of user:password");
        }
        headers = new HashMap<>();
        String encoded = Base64.encode(authKey.getBytes());
        header = "Basic " + encoded;
        headers.put("Authorization", header);
    }

    @Override
    public String getHost() {
        return "madokami";
    }

    @Override
    public String getDomain() {
        return "manga.madokami.al";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("^https://manga\\.madokami\\.al/Manga/./.*/.*/(.*)$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            String galleryId = m.group(1);
            try {
                galleryId = URLDecoder.decode(galleryId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return galleryId;
        }
        throw new MalformedURLException("Expected madokami URL format: " +
                "https://manga.madokami.al/Manga/./.*/.*/albumid - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        Http client = Http.url(url);
        client.header("Authorization", header);
        return client.get();
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> fileURLs = new ArrayList<>();
        try {
            URL baseUrl = new URL(url.getProtocol() + "://" + url.getHost());
            for (Element link : doc.select("#index-table > tbody > tr > td:nth-child(1) > a")) {
                String href = link.attr("href");
                if (href.endsWith(".zip") || href.endsWith(".rar") || href.endsWith(".cbr") || href.endsWith(".cbz")) {
                    URL url = new URL(baseUrl, href);
                    fileURLs.add(url.toString());
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return fileURLs;
    }

    @Override
    public void downloadURL(URL url, int index) {
        File saveFile = getSaveFile("", "", getFileName(url), null);
        addURLToDownload(url, saveFile, null, null, false, headers);
    }

    public static String getFileName(URL url) {
        String saveAs;
        saveAs = url.toExternalForm();
        saveAs = saveAs.substring(saveAs.lastIndexOf('/') + 1);
        try {
            saveAs = URLDecoder.decode(saveAs, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return saveAs;
    }
}