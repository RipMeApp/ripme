package com.rarchives.ripme.ripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.rarchives.ripme.utils.Utils;

public class DownloadFileThread extends Thread {

    private static final Logger logger = Logger.getLogger(DownloadFileThread.class);

    private URL url;
    private File saveAs;

    public DownloadFileThread(URL url, File saveAs) {
        super();
        this.url = url;
        this.saveAs = saveAs;
    }

    public void run() {
        // Check if file already exists
        if (saveAs.exists()) {
            if (Utils.getConfigBoolean("file.overwrite", false)) {
                logger.info("File already exists and 'file.overwrite' is true, deleting: " + saveAs);
                saveAs.delete();
            } else {
                logger.info("Not downloading " + url + " because file already exists: " + saveAs);
                return;
            }
        }

        logger.info("Downloading file from: " + url);
        try {
            Response response;
            response = Jsoup.connect(url.toExternalForm())
                                     .ignoreContentType(true)
                                     .execute();
            FileOutputStream out = (new FileOutputStream(saveAs));
            out.write(response.bodyAsBytes());
            out.close();
        } catch (IOException e) {
            logger.error("Exception while downloading file: " + url, e);
            return;
        }
        logger.info("Download completed: " + url);
    }

}