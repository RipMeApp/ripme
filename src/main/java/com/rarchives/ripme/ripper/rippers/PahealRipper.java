/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author
 */
public class PahealRipper extends AbstractHTMLRipper{
	private static Map<String,String> cookies=null;
	private static Pattern gidPattern=null;

	public static Map<String, String> getCookies() {
		if(cookies==null){
			cookies=new HashMap<String, String>(1);
			cookies.put("ui-tnc-agreed","true");
		}
		return cookies;
	}

	public PahealRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	public String getDomain() {
		return "rule34.paheal.net";
	}

	@Override
	public String getHost() {
		return "paheal";
	}

	@Override
	public Document getFirstPage() throws IOException {
		return Http.url("http://rule34.paheal.net/post/list/"+getGID(url)+"/1").cookies(getCookies()).get();
	}

	@Override
	public Document getNextPage(Document page) throws IOException {
		for(Element e:page.select("#paginator a")){
			if(e.text().toLowerCase().equals("next"))
				return Http.url(e.absUrl("href")).cookies(getCookies()).get();
		}
		
		return null;
	}

	@Override
	public List<String> getURLsFromPage(Document page) {
		Elements elements=page.select(".shm-thumb.thumb>a").not(".shm-thumb-link");
		List<String> res=new ArrayList<String>(elements.size());
		
		for(Element e:elements)
			res.add(e.attr("href"));
		
		return res;
	}

	@Override
	public void downloadURL(URL url, int index) {
		String file=url.getFile();
		try {
			addURLToDownload(new URL(url.getProtocol(),url.getHost(),url.getPort(),file.substring(0, Math.min(128,file.lastIndexOf('.')))+file.substring(file.lastIndexOf('.'))));
		} catch (MalformedURLException ex) {
			Logger.getLogger(PahealRipper.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		if(gidPattern==null)
			gidPattern=Pattern.compile("^https?://(www\\.)?rule34\\.paheal\\.net/post/list/([a-zA-Z0-9$_.+!*'(),-]+)(/.*)?(#.*)?$");
		
		Matcher m = gidPattern.matcher(url.toExternalForm());
		if(m.matches())
			return m.group(2);
		
		throw new MalformedURLException("Expected paheal.net URL format: rule34.paheal.net/post/list/searchterm - got "+url+" instead");
	}
	
}
