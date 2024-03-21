package com.rarchives.ripme.utils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Map;
import java.util.HashMap;

/**
 * Proxy/Socks setter
 */
public class Proxy {
    private Proxy() {
    }

    /**
     * Parse the proxy server settings from string, using the format
     * [user:password]@host[:port].
     *
     * @param fullproxy the string to parse
     * @return HashMap containing proxy server, port, user and password
     */
    private static Map<String, String> parseServer(String fullproxy) {
        Map<String, String> proxy = new HashMap<String, String>();

        if (fullproxy.lastIndexOf("@") != -1) {
            int sservli = fullproxy.lastIndexOf("@");
            String userpw = fullproxy.substring(0, sservli);
            String[] usersplit = userpw.split(":");
            proxy.put("user", usersplit[0]);
            proxy.put("password", usersplit[1]);
            fullproxy = fullproxy.substring(sservli + 1);
        }
        String[] servsplit = fullproxy.split(":");
        if (servsplit.length == 2) {
            proxy.put("port", servsplit[1]);
        }
        proxy.put("server", servsplit[0]);
        return proxy;
    }

    /**
     * Set a HTTP Proxy.
     * WARNING: Authenticated HTTP Proxy won't work from jdk1.8.111 unless
     * passing the flag -Djdk.http.auth.tunneling.disabledSchemes="" to java
     * see https://stackoverflow.com/q/41505219
     *
     * @param fullproxy the proxy, using format [user:password]@host[:port]
     */
    public static void setHTTPProxy(String fullproxy) {
        Map<String, String> proxyServer = parseServer(fullproxy);

        if (proxyServer.get("user") != null && proxyServer.get("password") != null) {
            Authenticator.setDefault(new Authenticator(){
                protected PasswordAuthentication  getPasswordAuthentication(){
                    PasswordAuthentication p = new PasswordAuthentication(proxyServer.get("user"), proxyServer.get("password").toCharArray());
                    return p;
                }
            });
            System.setProperty("http.proxyUser", proxyServer.get("user"));
            System.setProperty("http.proxyPassword", proxyServer.get("password"));
            System.setProperty("https.proxyUser", proxyServer.get("user"));
            System.setProperty("https.proxyPassword", proxyServer.get("password"));
        }

        if (proxyServer.get("port") != null) {
            System.setProperty("http.proxyPort", proxyServer.get("port"));
            System.setProperty("https.proxyPort", proxyServer.get("port"));
        }

        System.setProperty("http.proxyHost", proxyServer.get("server"));
        System.setProperty("https.proxyHost", proxyServer.get("server"));
    }

    /**
     * Set a Socks Proxy Server (globally).
     *
     * @param fullsocks the socks server, using format [user:password]@host[:port]
     */
    public static void setSocks(String fullsocks) {

        Map<String, String> socksServer = parseServer(fullsocks);
        if (socksServer.get("user") != null && socksServer.get("password") != null) {
            Authenticator.setDefault(new Authenticator(){
                protected PasswordAuthentication  getPasswordAuthentication(){
                    PasswordAuthentication p = new PasswordAuthentication(socksServer.get("user"), socksServer.get("password").toCharArray());
                    return p;
                }
            });
            System.setProperty("java.net.socks.username", socksServer.get("user"));
            System.setProperty("java.net.socks.password", socksServer.get("password"));
        }
        if (socksServer.get("port") != null) {
            System.setProperty("socksProxyPort", socksServer.get("port"));
        }

        System.setProperty("socksProxyHost", socksServer.get("server"));
    }

}
