package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.jsoup.Connection.Method;

import com.rarchives.ripme.ripper.rippers.SpankbangRipper;
import com.rarchives.ripme.utils.Http;

public class SpankBangRipperTest extends RippersTest {

    public void testSpankBangVideo() throws IOException {
        SpankbangRipper ripper = new SpankbangRipper(new URL("https://spankbang.com/2a7fh/video/mdb901"));  //most popular video of all time on site; should stay up
        testRipper(ripper);
    }

	public void testSBFix() throws IOException {
		String url = "https://spankbang.com/2a7fh/video/mdb901";
		Http http = Http.url(url);
		org.jsoup.nodes.Document doc = http.get();
		Map<String, String> cookies = http.connection().response().cookies();
		String sb_csrf_session = cookies.get("sb_csrf_session");
		System.out.println(sb_csrf_session);// 9c5018459bb441fb6064e404acd0a2d17d9445ca32350b9ff437d7cae18e69ad
		String contentUrl = new JSONObject(doc.select("script[type=application/ld+json]").first().data())
				.get("contentUrl").toString();
		System.out.println(contentUrl);// https://spankbang.com/stream/MzgzNTQyMQ.0JaO6IUfIXyR7Z41mbfYfABMp4s.mp4
		String id = contentUrl.substring(contentUrl.lastIndexOf('/') + 1, contentUrl.lastIndexOf('.'));
		System.out.println(id);// MzgzNTQyMQ.0JaO6IUfIXyR7Z41mbfYfABMp4s
		String infoUrl = url.substring(0, url.indexOf("/", 10)) + "/api/videos/stream";
		System.out.println(infoUrl); // https://spankbang.com/api/videos/stream
		Map<String, String> formData = new HashMap<>();
		formData.put("id", id);
		formData.put("data", "0");
		formData.put("sb_csrf_session", sb_csrf_session);
		Http infoHttp = Http.url(infoUrl);
		infoHttp.data(formData).connection().method(Method.POST);
		JSONObject infoJson = infoHttp.getJSON();
		System.out.println(infoJson); // print info sjon
		Comparator<Integer> reversed = Comparator.<Integer>naturalOrder().reversed();
		@SuppressWarnings("unchecked")
		String p = ((Collection<String>) infoJson.keySet()).stream().filter(k -> k.startsWith("stream_url_"))
				.filter(k -> StringUtils.isNotBlank(infoJson.get(k).toString()))
				.map(k -> new SimpleEntry<String, Integer>(k,
						Integer.parseInt(k.replace("stream_url_", "").replace("p", "").replace("4k", "3840")
								.replace("8k", "7680"))))
				.sorted((e1, e2) -> reversed.compare(e1.getValue(), e2.getValue())).findFirst().get().getKey();
		System.out.println(p); // highest of 4k, 1080p, 720p, ... with prefix "stream_url_" as found in json
		String actualUrl = infoJson.get(p).toString();
		System.out.println(actualUrl); // https://vcdn222.spankbang.com/3/8/3835421-480p.mp4?st=RebZLKtgRLPi16y-CnNt5g&e=1553711616
	}
}
