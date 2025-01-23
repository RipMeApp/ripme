package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rarchives.ripme.utils.Http;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rarchives.ripme.ripper.AbstractJSONRipper;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HentaiNexusRipper extends AbstractJSONRipper {

    public HentaiNexusRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "hentainexus";
    }
    @Override
    public String getDomain() {
        return "hentainexus.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        /*
            Valid URLs are /view/id, /read/id and those 2 with #pagenumber
            https://hentainexus.com/view/9202
            https://hentainexus.com/read/9202
            https://hentainexus.com/view/9202#001
            https://hentainexus.com/read/9202#001
         */

        Pattern p = Pattern.compile("^https?://hentainexus\\.com/(?:view|read)/([0-9]+)(?:\\#[0-9]+)*$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected hentainexus.com URL format: " +
                "hentainexus.com/view/id OR hentainexus.com/read/id - got " + url + "instead");
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }


    @Override
    protected List<String> getURLsFromJSON(JSONObject json) throws JSONException {

        List<String> urlList = new ArrayList<>();

        JSONArray imagesList = json.getJSONArray("f");
        String host = json.getString("b");
        String folder = json.getString("r");
        String id = json.getString("i");

        for (Object singleImage : imagesList) {
            String hashTMP = ((JSONObject) singleImage).getString("h");
            String fileNameTMP = ((JSONObject) singleImage).getString("p");
            String imageUrlTMP = String.format("%s%s%s/%s/%s",host,folder,hashTMP,id,fileNameTMP);
            urlList.add(imageUrlTMP);
        }

        return urlList;
    }

    @Override
    protected JSONObject getFirstPage() throws IOException, URISyntaxException {
        String jsonEncodedString = getJsonEncodedStringFromPage();
        String jsonDecodedString = decodeJsonString(jsonEncodedString);
        return new JSONObject(jsonDecodedString);
    }

    public String getJsonEncodedStringFromPage() throws MalformedURLException, IOException, URISyntaxException {
        // Image data only appears on the /read/ page and not on the /view/ one.
        URL readUrl = new URI(String.format("http://hentainexus.com/read/%s",getGID(url))).toURL();
        Document document = Http.url(readUrl).response().parse();

        for (Element scripts : document.getElementsByTag("script")) {
            for (DataNode dataNode : scripts.dataNodes()) {
                if (dataNode.getWholeData().contains("initReader")) {
                    // Extract JSON encoded string from the JavaScript initReader() call.
                    String data = dataNode.getWholeData().trim().replaceAll("\\r|\\n|\\t","");

                    Pattern p = Pattern.compile(".*?initReader\\(\"(.*?)\",.*?\\).*?");
                    Matcher m = p.matcher(data);
                    if (m.matches()) {
                        return m.group(1);
                    }
                }
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public String decodeJsonString(String jsonEncodedString)
    {
        /*
            The initReader() JavaScript function accepts 2 parameters: a weird string and the window title (we can ignore this).
            The weird string is a JSON string with some bytes shifted and swapped around and then encoded in base64.
            The following code is a Java adaptation of the initRender() JavaScript function after manual deobfuscation.
         */

        byte[] jsonBytes = Base64.getDecoder().decode(jsonEncodedString);

        @SuppressWarnings("rawtypes")
        ArrayList unknownArray = new ArrayList();
        ArrayList<Integer> indexesToUse = new ArrayList<>();

        for (int i = 0x2; unknownArray.size() < 0x10; ++i) {
            if (!indexesToUse.contains(i)) {
                unknownArray.add(i);
                for (int j = i << 0x1; j <= 0x100; j += i) {
                    if (!indexesToUse.contains(j)) {
                        indexesToUse.add(j);
                    }
                }
            }
        }

        byte magicByte = 0x0;
        for (int i = 0x0; i < 0x40; i++) {
            magicByte = (byte) (signedToUnsigned(magicByte) ^ signedToUnsigned(jsonBytes[i]));
            for (int j = 0x0; j < 0x8; j++) {
                long unsignedMagicByteTMP = signedToUnsigned(magicByte);
                magicByte = (byte) ((unsignedMagicByteTMP & 0x1) == 1 ? unsignedMagicByteTMP >>> 0x1 ^ 0xc : unsignedMagicByteTMP >>> 0x1);
            }
        }

        magicByte = (byte) (magicByte & 0x7);
        ArrayList<Integer> newArray = new ArrayList<>();

        for (int i = 0x0; i < 0x100; i++) {
            newArray.add(i);
        }

        int newIndex = 0, backup = 0;
        for (int i = 0x0; i < 0x100; i++) {
            newIndex = (newIndex + newArray.get(i) + (int) signedToUnsigned(jsonBytes[i % 0x40])) % 0x100;
            backup = newArray.get(i);
            newArray.set(i, newArray.get(newIndex));
            newArray.set(newIndex, backup);
        }

        int magicByteTranslated = (int) unknownArray.get(magicByte);
        int index1 = 0x0, index2 = 0x0, index3 = 0x0, swap1 = 0x0, xorNumber = 0x0;
        String decodedJsonString = "";

        for (int i = 0x0; i + 0x40 < jsonBytes.length; i++) {
            index1 = (index1 + magicByteTranslated) % 0x100;
            index2 = (index3 + newArray.get((index2 + newArray.get(index1)) % 0x100)) % 0x100;
            index3 = (index3 + index1 + newArray.get(index1)) % 0x100;
            swap1 = newArray.get(index1);
            newArray.set(index1, newArray.get(index2));
            newArray.set(index2,swap1);
            xorNumber = newArray.get((index2 + newArray.get((index1 + newArray.get((xorNumber + index3) % 0x100)) % 0x100)) % 0x100);
            decodedJsonString += Character.toString((char) signedToUnsigned((jsonBytes[i + 0x40] ^ xorNumber)));
        }

        return decodedJsonString;
    }


    private static long signedToUnsigned(int signed) {
        return (byte) signed & 0xFF;
    }

}