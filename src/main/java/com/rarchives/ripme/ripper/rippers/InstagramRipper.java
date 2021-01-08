package com.rarchives.ripme.ripper.rippers;

import com.oracle.js.parser.ErrorManager;
import com.oracle.js.parser.Parser;
import com.oracle.js.parser.ScriptEnvironment;
import com.oracle.js.parser.Source;
import com.oracle.js.parser.ir.*;
import com.rarchives.ripme.ripper.AbstractJSONRipper;
import com.rarchives.ripme.utils.Http;
import com.rarchives.ripme.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;

// Available configuration options:
// instagram.download_images_only - use to skip video links
// instagram.session_id - should be set for stories and private accounts (look for sessionid cookie)
public class InstagramRipper extends AbstractJSONRipper {

    private String qHash;
    private Map<String, String> cookies = new HashMap<>();
    private String idString;
    private List<String> itemPrefixes = new ArrayList<>();
    private List<String> failedItems = new ArrayList<>();

    private boolean hashtagRip;
    private boolean taggedRip;
    private boolean igtvRip;
    private boolean postRip;
    private boolean storiesRip;
    private boolean pinnedRip;
    private boolean pinnedReelRip;

    private enum UrlTypePattern {
        // e.g. https://www.instagram.com/explore/tags/rachelc00k/
        HASHTAG("explore/tags/(?<tagname>[^?/]+)"),

        // e.g. https://www.instagram.com/stories/rachelc00k/
        STORIES("stories/(?<username>[^?/]+)"),

        // e.g. https://www.instagram.com/rachelc00k/tagged/
        USER_TAGGED("(?<username>[^?/]+)/tagged"),

        // e.g. https://www.instagram.com/rachelc00k/channel/
        IGTV("(?<username>[^?/]+)/channel"),

        // e.g. https://www.instagram.com/p/Bu4CEfbhNk4/
        SINGLE_POST("(?:p|tv)/(?<shortcode>[^?/]+)"),

        // pseudo-url, e.g. https://www.instagram.com/rachelc00k/?pinned
        PINNED("(?<username>[^?/]+)/?[?]pinned"),

        // e.g. https://www.instagram.com/rachelc00k/
        USER_PROFILE("(?<username>[^?/]+)");

        private final String urlTypePattern;

        UrlTypePattern(String urlTypePattern) {
            this.urlTypePattern = urlTypePattern;
        }
    }

    public InstagramRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    protected String getDomain() {
        return "instagram.com";
    }

    @Override
    public String getHost() {
        return "instagram";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        for (UrlTypePattern urlType : UrlTypePattern.values()) {
            Matcher urlMatcher = getUrlMatcher(url, urlType);
            if (urlMatcher.matches()) {
                switch (urlType) {
                    case HASHTAG:
                        hashtagRip = true;
                        return "tag_" + urlMatcher.group("tagname");
                    case PINNED:
                        pinnedRip = true;
                        return urlMatcher.group("username") + "_pinned";
                    case STORIES:
                        storiesRip = true;
                        return urlMatcher.group("username") + "_stories";
                    case USER_TAGGED:
                        taggedRip = true;
                        return urlMatcher.group("username") + "_tagged";
                    case IGTV:
                        igtvRip = true;
                        return urlMatcher.group("username") + "_igtv";
                    case SINGLE_POST:
                        postRip = true;
                        return "post_" + urlMatcher.group("shortcode");
                    case USER_PROFILE:
                        return urlMatcher.group("username");
                    default:
                        throw new RuntimeException("Reached unreachable");
                }
            }
        }
        throw new MalformedURLException("This URL can't be ripped");
    }

    private Matcher getUrlMatcher(URL url, UrlTypePattern type) {
        String baseRegex = "^https?://(?:www[.])?instagram[.]com/%s(?:[?/].*)?";
        Pattern pattern = Pattern.compile(format(baseRegex, type.urlTypePattern));
        return pattern.matcher(url.toExternalForm());
    }

    @Override
    public JSONObject getFirstPage() throws IOException {
        setAuthCookie();
        Document document = Http.url(url).cookies(cookies).response().parse();
        qHash = getQhash(document);
        JSONObject jsonObject = getJsonObjectFromDoc(document);
        String hashtagNamePath = "entry_data.TagPage[0].graphql.hashtag.name";
        String singlePostIdPath = "graphql.shortcode_media.shortcode";
        String profileIdPath = "entry_data.ProfilePage[0].graphql.user.id";
        String storiesPath = "entry_data.StoriesPage[0].user.id";
        String idPath = hashtagRip ? hashtagNamePath : storiesRip ? storiesPath : postRip ? singlePostIdPath : profileIdPath;
        idString = getJsonStringByPath(jsonObject, idPath);
        return taggedRip ? getNextPage(null) : pinnedRip ? getPinnedItems(document) : storiesRip ? getStoriesItems() : jsonObject;
    }

    private void setAuthCookie() throws IOException {
        String sessionId = Utils.getConfigString("instagram.session_id", null);
        if ((storiesRip || pinnedRip) && sessionId == null) {
            throw new IOException("instagram.session_id should be set up for Instagram stories");
        }
        if (sessionId != null) {
            cookies.put("sessionid", sessionId);
        }
    }

    // Query hash is used for graphql requests
    private String getQhash(Document doc) throws IOException {
        if (postRip) {
            return null;
        }

        Predicate<String> hrefFilter = href -> href.contains("Consumer.js");
        if (taggedRip) {
            hrefFilter = href -> href.contains("ProfilePageContainer.js") || href.contains("TagPageContainer.js");
        }

        String href = doc.select("link[rel=preload]").stream()
                .map(link -> link.attr("href"))
                .filter(hrefFilter)
                .findFirst().orElse("");

        String body = Http.url("https://www.instagram.com" + href).cookies(cookies).response().body();

        Function<String, String> hashExtractor =
                storiesRip || pinnedReelRip ? this::getStoriesHash :
                        pinnedRip ? this::getPinnedHash : hashtagRip ? this::getTagHash :
                                taggedRip ? this::getUserTagHash : this::getProfileHash;

        return hashExtractor.apply(body);
    }

    private String getStoriesHash(String jsData) {
        return getHashValue(jsData, "loadStoryViewers", -5);
    }

    private String getProfileHash(String jsData) {
        return getHashValue(jsData, "loadProfilePageExtras", -1,
                s -> s.replaceAll(".*queryId\\s?:\\s?\"([0-9a-f]*)\".*", "$1"));
    }

    private String getPinnedHash(String jsData) {
        return getHashValue(jsData, "loadProfilePageExtras", -2);
    }

    private String getTagHash(String jsData) {
        return getHashValue(jsData, "requestNextTagMedia", -1);
    }

    private String getUserTagHash(String jsData) {
        return getHashValue(jsData, "requestNextTaggedPosts", -1);
    }

    private JSONObject getJsonObjectFromDoc(Document document) {
        for (Element script : document.select("script[type=text/javascript]")) {
            String scriptText = script.data();
            if (scriptText.startsWith("window._sharedData") || scriptText.startsWith("window.__additionalDataLoaded")) {
                String jsonText = scriptText.replaceAll("[^{]*([{].*})[^}]*", "$1");
                if (jsonText.contains("graphql") || jsonText.contains("StoriesPage")) {
                    return new JSONObject(jsonText);
                }
            }
        }
        return null;
    }

    @Override
    public JSONObject getNextPage(JSONObject source) throws IOException {
        if (postRip || storiesRip || pinnedReelRip) {
            return null;
        }
        JSONObject nextPageQuery = new JSONObject().put(hashtagRip ? "tag_name" : "id", idString).put("first", 12);
        if (source == null) {
            return graphqlRequest(nextPageQuery);
        }
        JSONObject pageInfo = getMediaRoot(source).getJSONObject("page_info");
        if (pageInfo.getBoolean("has_next_page")) {
            return graphqlRequest(nextPageQuery.put("after", pageInfo.getString("end_cursor")));
        } else {
            failedItems.forEach(LOGGER::error);
            return null;
        }
    }

    private JSONObject getStoriesItems() throws IOException {
        return graphqlRequest(new JSONObject().append("reel_ids", idString).put("precomposed_overlay", false));
    }

    // Two requests with different query hashes required for pinned items.
    // Query hash to be used depends on flag specified:
    // pinnedRip flag is used initially to get list of pinned albums;
    // pinnedReelRip flag is used next to get media urls.
    private JSONObject getPinnedItems(Document document) throws IOException {
        JSONObject queryForIds = new JSONObject().put("user_id", idString).put("include_highlight_reels", true);
        JSONObject pinnedIdsJson = graphqlRequest(queryForIds);
        JSONArray pinnedItems = getJsonArrayByPath(pinnedIdsJson, "data.user.edge_highlight_reels.edges");
        pinnedRip = false;
        pinnedReelRip = true;
        qHash = getQhash(document);
        JSONObject queryForDetails = new JSONObject();
        getStreamOfJsonArray(pinnedItems)
                .map(object -> getJsonStringByPath(object, "node.id"))
                .forEach(id -> queryForDetails.append("highlight_reel_ids", id));
        queryForDetails.put("precomposed_overlay", false);
        return graphqlRequest(queryForDetails);
    }

    private JSONObject graphqlRequest(JSONObject vars) throws IOException {
        // Sleep for a while to avoid a ban
        sleep(2500);
        String url = format("https://www.instagram.com/graphql/query/?query_hash=%s&variables=%s", qHash, vars.toString());
        return Http.url(url).cookies(cookies).getJSON();
    }

    @Override
    public List<String> getURLsFromJSON(JSONObject json) {
        if (storiesRip || pinnedReelRip) {
            JSONArray storyAlbums = getJsonArrayByPath(json, "data.reels_media");
            return getStreamOfJsonArray(storyAlbums)
                    .flatMap(album -> getStreamOfJsonArray(album.getJSONArray("items")))
                    .peek(storyItem -> itemPrefixes.add(getTimestampPrefix(storyItem)))
                    .flatMap(this::parseStoryItemForUrls)
                    .collect(Collectors.toList());
        }
        if (postRip) {
            JSONObject detailsJson = downloadItemDetailsJson(idString);
            addPrefixInfo(detailsJson);
            return parseItemDetailsForUrls(detailsJson).collect(Collectors.toList());
        }
        JSONArray edges = getMediaRoot(json).getJSONArray("edges");
        return getStreamOfJsonArray(edges)
                .map(edge -> getJsonStringByPath(edge, "node.shortcode"))
                .map(this::downloadItemDetailsJson)
                .filter(Objects::nonNull)
                .peek(this::addPrefixInfo)
                .flatMap(this::parseItemDetailsForUrls)
                .collect(Collectors.toList());
    }

    private Stream<? extends String> parseStoryItemForUrls(JSONObject storyItem) {
        if (storyItem.getBoolean("is_video")) {
            itemPrefixes.add(getTimestampPrefix(storyItem) + "preview_");
            int lastIndex = storyItem.getJSONArray("video_resources").length() - 1;
            return Stream.of(
                    getJsonStringByPath(storyItem, "video_resources[" + lastIndex + "].src"),
                    storyItem.getString("display_url"));
        }
        return Stream.of(storyItem.getString("display_url"));
    }

    private JSONObject getMediaRoot(JSONObject json) {
        String userExtra = "data.user.edge_owner_to_timeline_media";
        String igtvExtra = "data.user.edge_felix_video_timeline";
        String taggedExtra = "data.user.edge_user_to_photos_of_you";
        String hashtagExtra = "data.hashtag.edge_hashtag_to_media";
        String userHomeRoot = "entry_data.ProfilePage[0].graphql.user.edge_owner_to_timeline_media";
        String igtvHomeRoot = "entry_data.ProfilePage[0].graphql.user.edge_felix_video_timeline";
        String hashtagHomeRoot = "entry_data.TagPage[0].graphql.hashtag.edge_hashtag_to_media";
        String mediaRootPath = json.optJSONObject("entry_data") != null ?
                (hashtagRip ? hashtagHomeRoot : igtvRip ? igtvHomeRoot : userHomeRoot) : hashtagRip ?
                hashtagExtra : igtvRip ? igtvExtra : taggedRip ? taggedExtra : userExtra;
        return getJsonObjectByPath(json, mediaRootPath);
    }

    private JSONObject downloadItemDetailsJson(String shortcode) {
        String url = "https://www.instagram.com/p/%s/?__a=1";
        try {
            Http http = Http.url(format(url, shortcode));
            http.ignoreContentType();
            http.connection().followRedirects(false);
            Connection.Response response = http.cookies(cookies).response();
            // Fix for redirection link; repeat request with the new shortcode
            if (response.statusCode() == 302) {
                Pattern redirectIdPattern = Pattern.compile("/p/(?<shortcode>[^?/]+)");
                Matcher m = redirectIdPattern.matcher(response.header("location"));
                return m.find() ? downloadItemDetailsJson(m.group("shortcode")) : null;
            }
            return new JSONObject(response.body());
        } catch (Exception e) {
            failedItems.add(shortcode);
            LOGGER.trace(format("No item %s found", shortcode), e);
        }
        return null;
    }

    private void addPrefixInfo(JSONObject itemDetailsJson) {
        JSONObject mediaItem = getJsonObjectByPath(itemDetailsJson, "graphql.shortcode_media");
        String shortcode = mediaItem.getString("shortcode");
        int subItemsCount = "GraphSidecar".equals(mediaItem.getString("__typename")) ?
                getJsonArrayByPath(mediaItem, "edge_sidecar_to_children.edges").length() : 1;
        for (int i = 0; i < subItemsCount; i++) {
            itemPrefixes.add(getTimestampPrefix(mediaItem) + shortcode + "_");
        }
    }

    private String getTimestampPrefix(JSONObject item) {
        Instant instant = Instant.ofEpochSecond(item.getLong("taken_at_timestamp"));
        return DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_").format(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC));
    }

    private Stream<? extends String> parseItemDetailsForUrls(JSONObject itemDetailsJson) {
        JSONObject mediaItem = getJsonObjectByPath(itemDetailsJson, "graphql.shortcode_media");
        // For some reason JSON video_url has lower quality than the HTML-tag one
        // HTML-tag url is requested here and marked with _extra_ prefix
        if ("GraphVideo".equals(mediaItem.getString("__typename"))) {
            String shortcode = mediaItem.getString("shortcode");
            String urlFromPage = getVideoUrlFromPage(shortcode);
            if (!urlFromPage.isEmpty()) {
                itemPrefixes.add(getTimestampPrefix(mediaItem) + shortcode + "_extra_");
                return Stream.of(mediaItem.getString("video_url"), urlFromPage);
            }
        }
        return parseRootForUrls(mediaItem);
    }

    // Uses recursion for GraphSidecar
    private Stream<? extends String> parseRootForUrls(JSONObject mediaItem) {
        String typeName = mediaItem.getString("__typename");
        switch (typeName) {
            case "GraphImage":
                return Stream.of(mediaItem.getString("display_url"));
            case "GraphVideo":
                return Stream.of(mediaItem.getString("video_url"));
            case "GraphSidecar":
                JSONArray sideCar = getJsonArrayByPath(mediaItem, "edge_sidecar_to_children.edges");
                return getStreamOfJsonArray(sideCar).map(object -> object.getJSONObject("node"))
                        .flatMap(this::parseRootForUrls);
            default:
                return Stream.empty();
        }
    }

    private String getVideoUrlFromPage(String videoID) {
        try {
            Document doc = Http.url("https://www.instagram.com/p/" + videoID).cookies(cookies).get();
            return doc.select("meta[property=og:video]").attr("content");
        } catch (Exception e) {
            LOGGER.warn("Unable to get page " + "https://www.instagram.com/p/" + videoID);
        }
        return "";
    }

    @Override
    protected void downloadURL(URL url, int index) {
        if (Utils.getConfigBoolean("instagram.download_images_only", false) && url.toString().contains(".mp4?")) {
            LOGGER.info("Skipped video url: " + url);
            return;
        }
        addURLToDownload(url, itemPrefixes.get(index - 1), "", null, cookies);
    }

    // Javascript parsing
    /* ------------------------------------------------------------------------------------------------------- */
    private String getHashValue(String javaScriptData, String keyword, int offset,
            Function<String, String> extractHash) {
        List<Statement> statements = getJsBodyBlock(javaScriptData).getStatements();

        return statements.stream()
                .flatMap(statement -> filterItems(statement, ExpressionStatement.class))
                .map(ExpressionStatement::getExpression)
                .flatMap(expression -> filterItems(expression, CallNode.class))
                .map(CallNode::getArgs)
                .map(expressions -> expressions.get(0))
                .flatMap(expression -> filterItems(expression, FunctionNode.class))
                .map(FunctionNode::getBody)
                .map(Block::getStatements)
                .map(statementList -> lookForHash(statementList, keyword, offset, extractHash))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private String getHashValue(String javaScriptData, String keyword, int offset) {
        return getHashValue(javaScriptData, keyword, offset, null);
    }

    private String lookForHash(List<Statement> list, String keyword, int offset, Function<String, String> extractHash) {
        for (int i = 0; i < list.size(); i++) {
            Statement st = list.get(i);
            if (st.toString().contains(keyword)) {
                if (extractHash != null) {
                    return extractHash.apply(list.get(i + offset).toString());
                }
                return list.get(i + offset).toString().replaceAll(".*\"([0-9a-f]*)\".*", "$1");
            }
        }
        return null;
    }

    private <T> Stream<T> filterItems(Object obj, Class<T> aClass) {
        return Stream.of(obj).filter(aClass::isInstance).map(aClass::cast);
    }

    private Block getJsBodyBlock(String javaScriptData) {
        ScriptEnvironment env = ScriptEnvironment.builder().ecmaScriptVersion(10).constAsVar(true).build();
        ErrorManager errorManager = new ErrorManager.ThrowErrorManager();
        Source src = Source.sourceFor("name", javaScriptData);
        return new Parser(env, src, errorManager).parse().getBody();
    }

    // Some JSON helper methods below
    /* ------------------------------------------------------------------------------------------------------- */
    private JSONObject getJsonObjectByPath(JSONObject object, String key) {
        Pattern arrayPattern = Pattern.compile("(?<arr>.*)\\[(?<idx>\\d+)]");
        JSONObject result = object;
        for (String s : key.split("[.]")) {
            Matcher m = arrayPattern.matcher(s);
            result = m.matches() ?
                    result.getJSONArray(m.group("arr")).getJSONObject(Integer.parseInt(m.group("idx"))) :
                    result.getJSONObject(s);
        }
        return result;
    }

    private <T> T getByPath(BiFunction<JSONObject, String, T> func, JSONObject object, String key) {
        int namePos = key.lastIndexOf('.');
        JSONObject parent = namePos < 0 ? object : getJsonObjectByPath(object, key.substring(0, namePos));
        return func.apply(parent, key.substring(namePos + 1));
    }

    private JSONArray getJsonArrayByPath(JSONObject object, String key) {
        return getByPath(JSONObject::getJSONArray, object, key);
    }

    private String getJsonStringByPath(JSONObject object, String key) {
        return getByPath(JSONObject::getString, object, key);
    }

    private Stream<JSONObject> getStreamOfJsonArray(JSONArray array) {
        return StreamSupport.stream(new JSONSpliterator(array), false);
    }

    private class JSONSpliterator extends Spliterators.AbstractSpliterator<JSONObject> {
        private JSONArray array;
        private int index = 0;

        JSONSpliterator(JSONArray array) {
            super(array.length(), SIZED | ORDERED);
            this.array = array;
        }

        @Override
        public boolean tryAdvance(Consumer<? super JSONObject> action) {
            if (index == array.length()) {
                return false;
            }
            action.accept(array.getJSONObject(index++));
            return true;
        }
    }
}
