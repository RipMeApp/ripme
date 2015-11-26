
package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.ripper.DownloadThreadPool;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
public class E621Ripper extends AbstractHTMLRipper{
	private static Pattern gidPattern=null;
	private static Pattern gidPattern2=null;
	private static Pattern gidPatternPool=null;
	
	private DownloadThreadPool e621ThreadPool=new DownloadThreadPool("e621");
	
	public E621Ripper(URL url) throws IOException {
		super(url);
	}

	@Override
	public DownloadThreadPool getThreadPool() {
		return e621ThreadPool;
	}

	@Override
	public String getDomain() {
		return "e621.net";
	}

	@Override
	public String getHost() {
		return "e621";
	}

	@Override
	public Document getFirstPage() throws IOException {
		if(url.getPath().startsWith("/pool/show/"))
			return Http.url("https://e621.net/pool/show/"+getTerm(url)).get();
		else
			return Http.url("https://e621.net/post/index/1/"+getTerm(url)).get();
	}

	@Override
	public List<String> getURLsFromPage(Document page) {
		Elements elements=page.select("#post-list .thumb a,#pool-show .thumb a");
		List<String> res=new ArrayList<String>(elements.size());
		
		for(Element e:elements){
			res.add(e.absUrl("href")+"#"+e.child(0).attr("id").substring(1));
		}
		
		return res;
	}

	@Override
	public Document getNextPage(Document page) throws IOException {
		for(Element e:page.select("#paginator a")){
			if(e.attr("rel").equals("next"))
				return Http.url(e.absUrl("href")).get();
		}
		
		return null;
	}

	@Override
	public void downloadURL(final URL url, int index) {
		e621ThreadPool.addThread(new Thread(new Runnable() {
			public void run() {
				try {
					Document page=Http.url(url).get();
					
					addURLToDownload(new URL(page.getElementById("image").absUrl("src")),Utils.getConfigBoolean("download.save_order",true)?url.getRef()+"-":"");
				} catch (IOException ex) {
					Logger.getLogger(E621Ripper.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}));
	}
	
	private String getTerm(URL url) throws MalformedURLException{
		if(gidPattern==null)
			gidPattern=Pattern.compile("^https?://(www\\.)?e621\\.net/post/index/[^/]+/([a-zA-Z0-9$_.+!*'(),%-]+)(/.*)?(#.*)?$");
		if(gidPatternPool==null)
			gidPatternPool=Pattern.compile("^https?://(www\\.)?e621\\.net/pool/show/([a-zA-Z0-9$_.+!*'(),%-]+)(\\?.*)?(/.*)?(#.*)?$");

		Matcher m = gidPattern.matcher(url.toExternalForm());
		if(m.matches())
			return m.group(2);
		
		m = gidPatternPool.matcher(url.toExternalForm());
		if(m.matches())
			return m.group(2);
		
		throw new MalformedURLException("Expected e621.net URL format: e621.net/post/index/1/searchterm - got "+url+" instead");
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		try {
			String prefix="";
			if(url.getPath().startsWith("/pool/show/"))
				prefix="pool_";
			
			return Utils.filesystemSafe(prefix+new URI(getTerm(url)).getPath());
		} catch (URISyntaxException ex) {
			Logger.getLogger(PahealRipper.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		throw new MalformedURLException("Expected e621.net URL format: e621.net/post/index/1/searchterm - got "+url+" instead");
	}

	@Override
	public URL sanitizeURL(URL url) throws MalformedURLException {
		if(gidPattern2==null)
			gidPattern2=Pattern.compile("^https?://(www\\.)?e621\\.net/post/search\\?tags=([a-zA-Z0-9$_.+!*'(),%-]+)(/.*)?(#.*)?$");
		
		Matcher m = gidPattern2.matcher(url.toExternalForm());
		if(m.matches())
			return new URL("https://e621.net/post/index/1/"+m.group(2).replace("+","%20"));
		
		return url;
	}
	
}