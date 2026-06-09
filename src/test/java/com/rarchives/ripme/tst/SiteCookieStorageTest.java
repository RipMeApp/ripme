package com.rarchives.ripme.tst;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rarchives.ripme.utils.RipUtils;
import com.rarchives.ripme.utils.SiteCookieStorage;

public class SiteCookieStorageTest {

    @Test
    public void testNormalizeDomainStripsWww() {
        Assertions.assertEquals("reddit.com", SiteCookieStorage.normalizeDomain("www.reddit.com"));
    }

    @Test
    public void testNormalizeCookieStringStripsCookiePrefix() {
        Assertions.assertEquals("a=b; c=d", SiteCookieStorage.normalizeCookieString("Cookie: a=b; c=d"));
    }

    @Test
    public void testGetCookiesFromStringSplitsOnFirstEquals() {
        Map<String, String> cookies = RipUtils.getCookiesFromString("token_v2=abc=def; reddit_session=xyz");
        Assertions.assertEquals("abc=def", cookies.get("token_v2"));
        Assertions.assertEquals("xyz", cookies.get("reddit_session"));
    }

    @Test
    public void testGetCookiesFromStringParsesJwtLikeValues() {
        String jwtLike = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.signature=with=equals";
        Map<String, String> cookies = RipUtils.getCookiesFromString("reddit_session=" + jwtLike);
        Assertions.assertEquals(jwtLike, cookies.get("reddit_session"));
    }

    @Test
    public void testGetCookiesFromStringParsesJsonCookieValue() {
        String line = "g_state={\"i_l\":0,\"i_ll\":1781015544601}; csv=2; pc=xg";
        Map<String, String> cookies = RipUtils.getCookiesFromString(line);
        Assertions.assertEquals("2", cookies.get("csv"));
        Assertions.assertEquals("xg", cookies.get("pc"));
        Assertions.assertEquals("{\"i_l\":0,\"i_ll\":1781015544601}", cookies.get("g_state"));
    }

    @Test
    public void testGetCookiesFromStringParsesNetworkHeaderStyleLine() {
        String line = "edgebucket=abc; loid=loidval; reddit_session=jwt=a=b=c; token_v2=tok=x=y=z; seeker_session=true";
        Map<String, String> cookies = RipUtils.getCookiesFromString(line);
        Assertions.assertEquals(5, cookies.size());
        Assertions.assertEquals("abc", cookies.get("edgebucket"));
        Assertions.assertEquals("jwt=a=b=c", cookies.get("reddit_session"));
        Assertions.assertEquals("tok=x=y=z", cookies.get("token_v2"));
        Assertions.assertEquals("true", cookies.get("seeker_session"));
    }
}
