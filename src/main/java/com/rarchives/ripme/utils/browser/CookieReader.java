package com.rarchives.ripme.utils.browser;

import java.io.IOException;
import java.util.List;

/**
 * Reads the cookies that apply to one host from a browser's cookie store.
 */
interface CookieReader {

    /** A single cookie that applies to the requested host. The value is never logged. */
    record BrowserCookie(String hostKey, String name, String value) {
    }

    /**
     * @param cookies       matching, non-expired cookies
     * @param undecryptable number of matching cookies that exist but could not be decrypted
     * @param notes         human readable reasons for anything that went wrong
     */
    record ReadResult(List<BrowserCookie> cookies, int undecryptable, List<String> notes) {
    }

    Browser browser();

    /** Whether a cookie store for this browser exists on this computer. */
    boolean isInstalled();

    ReadResult read(String host) throws IOException;
}
