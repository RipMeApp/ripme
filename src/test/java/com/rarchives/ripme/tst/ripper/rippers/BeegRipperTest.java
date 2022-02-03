package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.rarchives.ripme.utils.Http;


public class BeegRipperTest extends RippersTest {
	public void test() throws IOException {
		String url = "https://beeg.com/1958535";
		Http http = Http.url(url);
		http.get();
		Map<String, String> cookies = http.connection().response().cookies();
		String html = http.response().body();
		String beeg_version;
		{
			Pattern p = Pattern.compile("var beeg_version = (\\d++);");
			Matcher m = p.matcher(html);
			m.find();
			beeg_version = m.group(1);
		}
		String actualUrl;
		{
			String u = "https://beeg.com/api/v6/" + beeg_version + "/video/" + url.substring(url.lastIndexOf("/") + 1);
			System.out.println(u);
			Http http2 = Http.url(u);
			http2.cookies(cookies);
			JSONObject struct = http2.getJSON();
			System.out.println(struct);
			try {
				actualUrl = struct.getString("2160p").toString();
			} catch (Exception ex1) {
				try {
					actualUrl = struct.getString("1080p").toString();
				} catch (Exception ex2) {
					try {
						actualUrl = struct.getString("720p").toString();
					} catch (Exception ex3) {
						try {
							actualUrl = struct.getString("480p").toString();
						} catch (Exception ex4) {
							actualUrl = struct.getString("240p").toString();
						}
					}
				}
			}
			System.out.println(actualUrl);
		}
		{
			actualUrl = "https:" + actualUrl.replace("{DATA_MARKERS}", "data=pc_"//
					+ "UK"//languagecode?
					+ "__" + beeg_version + "_");
			System.out.println(actualUrl);
		}
	}
}
