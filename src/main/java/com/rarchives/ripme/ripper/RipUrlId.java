package com.rarchives.ripme.ripper;

import java.net.URL;
import java.util.Objects;

/**
 * RipUrlId represents a unique file on a host.
 * Necessary because some files may be accessible from multiple URLs, for example:
 * - a file in multiple albums, or
 * - a file only accessible with a tokened URL.
 */
public class RipUrlId {
    Class<? extends AbstractRipper> ripper;
    String ripperHost;
    String ripUrlId;
    URL url;

    /**
     * @param ripper     The ripper associated with the id
     * @param ripperHost The ripper's getHost(), because a ripper may support multiple hosts
     * @param ripUrlId   The unique identifier of the file fetchable by the ripper
     */
    public RipUrlId(Class<? extends AbstractRipper> ripper, String ripperHost, String ripUrlId) {
        if (ripper == null) {
            throw new IllegalArgumentException("ripper cannot be null");
        }
        if (ripperHost == null) {
            throw new IllegalArgumentException("ripperHost cannot be null");
        }
        if (ripUrlId == null) {
            throw new IllegalArgumentException("ripUrlId cannot be null");
        }
        this.ripper = ripper;
        this.ripperHost = ripperHost;
        this.ripUrlId = ripUrlId;
    }

    /**
     * Transitionary constructor for rippers that do not yet create an id
     *
     * @param ripper     The ripper associated with the id
     * @param ripperHost The ripper's getHost(), because a ripper may support multiple hosts
     * @param url        A URL fetchable by the ripper
     * @deprecated The other constructor is preferable
     */
    @Deprecated
    public RipUrlId(Class<? extends AbstractRipper> ripper, String ripperHost, URL url) {
        if (ripper == null) {
            throw new IllegalArgumentException("ripper cannot be null");
        }
        if (ripperHost == null) {
            throw new IllegalArgumentException("ripperHost cannot be null");
        }
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null");
        }
        this.ripper = ripper;
        this.ripperHost = ripperHost;
        this.url = url;
    }

    public Class<? extends AbstractRipper> getRipper() {
        return ripper;
    }

    public String getRipperHost() {
        return ripperHost;
    }

    public String getRipUrlId() {
        return ripUrlId;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RipUrlId ripUrlId1 = (RipUrlId) o;
        return Objects.equals(ripper, ripUrlId1.ripper) && Objects.equals(ripperHost, ripUrlId1.ripperHost) && Objects.equals(ripUrlId, ripUrlId1.ripUrlId) && Objects.equals(url, ripUrlId1.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ripper, ripperHost, ripUrlId, url);
    }

    @Override
    public String toString() {
        if (url != null) {
            return url.toString();
        }
        return ripper.getSimpleName() + ": " + ripperHost + ": " + ripUrlId;
    }
}
