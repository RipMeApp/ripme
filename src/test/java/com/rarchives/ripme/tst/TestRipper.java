package com.rarchives.ripme.tst;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.io.IOException;

import com.rarchives.ripme.ripper.AbstractRipper;

public class TestRipper extends AbstractRipper {

  public TestRipper(URL url) throws IOException {
    super(url);
  }

  @Override
  public String getStatusText() {
    return "SomeStatusText";
  }

  @Override
  public int getCompletionPercentage() {
    return 50;
  }

  @Override
  public void setWorkingDir(URL url) {
    return;
  }

  @Override
  public void downloadExists(URL url, File file){
    return;
  }
  
  @Override
  public void downloadErrored(URL url, String string) {
    return;
  }

  @Override
  public void downloadCompleted(URL url, File file) {
    return;
  }

  @Override
  public boolean addURLToDownload(URL url, File saveAs) {
    return false;
  }

  @Override
  public boolean addURLToDownload(URL url, File saveAs, String referrer, Map<String, String> cookies, Boolean getFileExtFromMIME) {
    return false;
  }

  @Override
  public String getGID(URL url) {
    return "SomeGID";
  }

  @Override
  public String getHost() {
    return "SomeHost";
  }

  @Override
  public void rip() {
    return;
  }

  @Override
  public URL sanitizeURL(URL url) {
    return url;
  }

  @Override
  public boolean canRip(URL url) {
    return true;
  }

  public boolean callHasDownloadedURL(String url) {
    return this.hasDownloadedURL(url);
  }

  public void callInitializeHistoryStore() {
    this.initializeHistoryStore();
  }

  public void insertToURLHashSet(String url) {
    this.urlHistoryHashSet.add(url.trim());
  }

  public boolean checkURLInHashSet(String url) {
    return this.urlHistoryHashSet.contains(url);
  }

  public void callWriteDownloadedURL(String url) throws IOException {
    this.writeDownloadedURL(url);
  }
}