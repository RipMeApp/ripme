package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;

public class ArtStationRipper extends AbstractJSONRipper {
    enum URL_TYPE {
        SINGLE_PROJECT, USER_PORTFOLIO, UNKNOWN
    }

    private ParsedURL albumURL;
    private String projectName;
    private Integer projectIndex;
    private Integer projectPageNumber;

    public ArtStationRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "artstation.com";
    }

    @Override
    public String getHost() {
        return "ArtStation";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        JSONObject groupData;

        // Parse URL and store for later use
        albumURL = parseURL(url);

        if (albumURL.getType() == URL_TYPE.SINGLE_PROJECT) {
            // URL points to single project, use project title as GID
            try {
                // groupData = Http.url(albumURL.getLocation()).getJSON();
                groupData = getJson(albumURL.getLocation());
            } catch (IOException | URISyntaxException e) {
                throw new MalformedURLException("Couldn't load JSON from " + albumURL.getLocation());
            }
            return groupData.getString("title");
        }

        if (albumURL.getType() == URL_TYPE.USER_PORTFOLIO) {
            // URL points to user portfolio, use user's full name as GID
            String userInfoURL = "https://www.artstation.com/users/" + albumURL.getID() + "/quick.json";
            try {
//                 groupData = Http.url(userInfoURL).getJSON();
                groupData = getJson(userInfoURL);
            } catch (IOException | URISyntaxException e) {
                throw new MalformedURLException("Couldn't load JSON from " + userInfoURL);
            }
            return groupData.getString("full_name");
        }

        // No JSON found in the URL entered, can't rip
        throw new MalformedURLException(
                "Expected URL to an ArtStation 'project url' or 'user profile url' - got " + url + " instead");
    }

    @Override
    protected JSONObject getFirstPage() throws IOException, URISyntaxException {
        if (albumURL.getType() == URL_TYPE.SINGLE_PROJECT) {
            // URL points to JSON of a single project, just return it
            // return Http.url(albumURL.getLocation()).getJSON();
            return getJson(albumURL.getLocation());
        }

        if (albumURL.getType() == URL_TYPE.USER_PORTFOLIO) {
            // URL points to JSON of a list of projects, load it to parse individual
            // projects
            // JSONObject albumContent = Http.url(albumURL.getLocation()).getJSON();
            JSONObject albumContent = getJson(albumURL.getLocation());

            if (albumContent.getInt("total_count") > 0) {
                // Get JSON of the first project and return it
                JSONObject projectInfo = albumContent.getJSONArray("data").getJSONObject(0);
                ParsedURL projectURL = parseURL(new URI(projectInfo.getString("permalink")).toURL());
                // return Http.url(projectURL.getLocation()).getJSON();
                return getJson(projectURL.getLocation());
            }
        }

        throw new IOException("URL specified points to an user with empty portfolio");
    }

    @Override
    protected JSONObject getNextPage(JSONObject doc) throws IOException, URISyntaxException {
        if (albumURL.getType() == URL_TYPE.USER_PORTFOLIO) {
            // Initialize the page number if it hasn't been initialized already
            if (projectPageNumber == null) {
                projectPageNumber = 1;
            }

            // Each page holds a maximum of 50 projects. Initialize the index if it hasn't
            // been initialized already or increment page number and reset the index if all
            // projects of the current page were already processed
            if (projectIndex == null) {
                projectIndex = 0;
            } else if (projectIndex > 49) {
                projectPageNumber++;
                projectIndex = 0;
            }

            int currentProject = ((projectPageNumber - 1) * 50) + (projectIndex + 1);
            // JSONObject albumContent = Http.url(albumURL.getLocation() + "?page=" +
            // projectPageNumber).getJSON();
            JSONObject albumContent = getJson(albumURL.getLocation() + "?page=" + projectPageNumber);

            if (albumContent.getInt("total_count") > currentProject) {
                // Get JSON of the next project and return it
                JSONObject projectInfo = albumContent.getJSONArray("data").getJSONObject(projectIndex);
                ParsedURL projectURL = parseURL(new URI(projectInfo.getString("permalink")).toURL());
                projectIndex++;
                // return Http.url(projectURL.getLocation()).getJSON();
                return getJson(projectURL.getLocation());
            }

            throw new IOException("No more projects");
        }

        throw new IOException("Downloading a single project");
    }

    @Override
    protected List<String> getURLsFromJSON(JSONObject json) {
        List<String> assetURLs = new ArrayList<>();
        JSONObject currentObject;

        // Update project name variable from JSON data. Used by downloadURL() to create
        // subfolders when input URL is URL_TYPE.USER_PORTFOLIO
        projectName = json.getString("title");

        for (int i = 0; i < json.getJSONArray("assets").length(); i++) {
            currentObject = json.getJSONArray("assets").getJSONObject(i);

            if (!currentObject.getString("image_url").isEmpty()) {
                // TODO: Find a way to rip external content.
                // ArtStation hosts only image content, everything else (videos, 3D Models, etc)
                // is hosted in other websites and displayed through embedded HTML5 players
                assetURLs.add(currentObject.getString("image_url"));
            }
        }

        return assetURLs;
    }

    @Override
    protected void downloadURL(URL url, int index) {
        if (albumURL.getType() == URL_TYPE.USER_PORTFOLIO) {
            // Replace not allowed characters with underlines
            String folderName = projectName.replaceAll("[\\\\/:*?\"<>|]", "_");

            // Folder name also can't end with dots or spaces, strip them
            folderName = folderName.replaceAll("\\s+$", "");
            folderName = folderName.replaceAll("\\.+$", "");

            // Downloading multiple projects, separate each one in subfolders
            addURLToDownload(url, "", folderName);
        } else {
            addURLToDownload(url);
        }
    }

    @Override
    public String normalizeUrl(String url) {
        // Strip URL parameters
        return url.replaceAll("\\?\\w+$", "");
    }

    private static class ParsedURL {
        URL_TYPE urlType;
        String jsonURL, urlID;

        /**
         * Construct a new ParsedURL object.
         * 
         * @param urlType URL_TYPE enum containing the URL type
         * @param jsonURL String containing the JSON URL location
         * @param urlID   String containing the ID of this URL
         * 
         */
        ParsedURL(URL_TYPE urlType, String jsonURL, String urlID) {
            this.urlType = urlType;
            this.jsonURL = jsonURL;
            this.urlID = urlID;
        }

        /**
         * Get URL Type of this ParsedURL object.
         * 
         * @return URL_TYPE enum containing this object type
         * 
         */
        URL_TYPE getType() {
            return this.urlType;
        }

        /**
         * Get JSON location of this ParsedURL object.
         * 
         * @return String containing the JSON URL
         * 
         */
        String getLocation() {
            return this.jsonURL;
        }

        /**
         * Get ID of this ParsedURL object.
         * 
         * @return For URL_TYPE.SINGLE_PROJECT, returns the project hash. For
         *         URL_TYPE.USER_PORTFOLIO, returns the account name
         */
        String getID() {
            return this.urlID;
        }
    }

    /**
     * Parses an ArtStation URL.
     * 
     * @param url URL to an ArtStation user profile
     *            (https://www.artstation.com/username) or single project
     *            (https://www.artstation.com/artwork/projectid)
     * @return ParsedURL object containing URL type, JSON location and ID (stores
     *         account name or project hash, depending of the URL type identified)
     * 
     */
    private ParsedURL parseURL(URL url) {
        String htmlSource;
        ParsedURL parsedURL;

        // Load HTML Source of the specified URL
        try {
            // htmlSource = Http.url(url).get().html();
            Connection con = Http.url(url).method(Method.GET).connection();
            con.ignoreHttpErrors(true);
            con.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0");
            con.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            con.header("Accept-Language", "en-US,en;q=0.5");
//            con.header("Accept-Encoding", "gzip, deflate, br");
            con.header("Upgrade-Insecure-Requests", "1");
            Response res = con.execute();
            int status = res.statusCode();

            if (status / 100 == 2) {
                htmlSource = res.parse().html();
            } else if (status == 403 && url.toString().contains("artwork/")) {
                // Catches cloudflare page. Error 403.
                // Usually caused by artwork URLs( arstation.com/artwork/someProjectId)
                String urlId = url.toString().substring(url.toString().lastIndexOf("/") + 1);
                String jsonURL = "https://www.artstation.com/projects/" + urlId + ".json";
                parsedURL = new ParsedURL(URL_TYPE.SINGLE_PROJECT, jsonURL, urlId);
                return parsedURL;
            } else {
                LOGGER.error("Couldnt fetch URL: " + url);
                throw new IOException("Error fetching URL: " + url + " Status Code: " + status);
            }
        } catch (IOException e) {
            htmlSource = "";
        }

        // Check if HTML Source of the specified URL references a project
        Pattern p = Pattern.compile("'/projects/(\\w+)\\.json'");
        Matcher m = p.matcher(htmlSource);
        if (m.find()) {
            parsedURL = new ParsedURL(URL_TYPE.SINGLE_PROJECT,
                    "https://www.artstation.com/projects/" + m.group(1) + ".json", m.group(1));
            return parsedURL;
        }

        // Check if HTML Source of the specified URL references a user profile
        p = Pattern.compile("'/users/([\\w-]+)/quick\\.json'");
        m = p.matcher(htmlSource);
        if (m.find()) {
            parsedURL = new ParsedURL(URL_TYPE.USER_PORTFOLIO,
                    "https://www.artstation.com/users/" + m.group(1) + "/projects.json", m.group(1));
            return parsedURL;
        }

        // HTML Source of the specified URL doesn't reference a user profile or project
        parsedURL = new ParsedURL(URL_TYPE.UNKNOWN, null, null);
        return parsedURL;
    }

    // Use this method instead of direct call to Http.url(url).getJson() to avoid
    // cloudflare 403 page.
    private JSONObject getJson(URL url) throws IOException {
        Connection con = Http.url(url).method(Method.GET).connection();
        con.ignoreHttpErrors(true);
        con.ignoreContentType(true);
        con.userAgent(
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        con.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        con.header("Accept-Language", "en-US,en;q=0.5");
//        con.header("Accept-Encoding", "gzip, deflate, br");
        con.header("Upgrade-Insecure-Requests", "1");
        Response res = con.execute();
        int status = res.statusCode();
        if (status / 100 == 2) {
            String jsonString = res.body();
            return new JSONObject(jsonString);
        }
        throw new IOException("Error fetching json. Status code:" + status);
    }

    private JSONObject getJson(String url) throws IOException, URISyntaxException {
        return getJson(new URI(url).toURL());
    }

}
