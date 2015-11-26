
package com.rarchives.ripme.ripper.rippers;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
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
 *
 * @author
 */
public class XbooruRipper extends AbstractHTMLRipper{
	private Pattern gidPattern=null;

	public XbooruRipper(URL url) throws IOException {
		super(url);
	}

	@Override
	public String getDomain() {
		return "xbooru.com";
	}

	@Override
	public String getHost() {
		return "xbooru";
	}
	
	private String getPage(int num) throws MalformedURLException{
		return "http://xbooru.com/index.php?page=dapi&s=post&q=index&pid="+num+"&tags="+getGID(url);
	}

	@Override
	public Document getFirstPage() throws IOException {
		return Http.url(getPage(0)).get();
	}

	@Override
	public Document getNextPage(Document doc) throws IOException {
		int offset=Integer.parseInt(doc.getElementsByTag("posts").first().attr("offset"));
		int num=Integer.parseInt(doc.getElementsByTag("posts").first().attr("count"));
		
		if(offset+100>num)
			return null;
		
		return Http.url(getPage(offset/100+1)).get();
	}

	@Override
	public List<String> getURLsFromPage(Document page) {
		List<String> res=new ArrayList<String>(100);
		for(Element e:page.getElementsByTag("post"))
			res.add(e.attr("file_url")+"#"+e.attr("id"));
		return res;
	}

	@Override
	public void downloadURL(URL url, int index) {
		addURLToDownload(url,Utils.getConfigBoolean("download.save_order",true)?url.getRef()+"-":"");
	}

	@Override
	public String getGID(URL url) throws MalformedURLException {
		if(gidPattern==null)
			gidPattern=Pattern.compile("^https?://(www\\.)?xbooru\\.com/(index.php)?.*([?&]tags=([a-zA-Z0-9$_.+!*'(),%-]+))(\\&|(#.*)?$)");
		
		Matcher m = gidPattern.matcher(url.toExternalForm());
		if(m.matches())
			return m.group(4);
		
		throw new MalformedURLException("Expected xbooru.com URL format: xbooru.com - got "+url+" instead");
	}
	
}
