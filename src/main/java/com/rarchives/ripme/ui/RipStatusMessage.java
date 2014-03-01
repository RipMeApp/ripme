package com.rarchives.ripme.ui;

public class RipStatusMessage {

    public enum STATUS {
        LOADING_RESOURCE("Loading Resource"),
        DOWNLOAD_STARTED("Download Started"),
        DOWNLOAD_COMPLETE("Download Domplete"),
        DOWNLOAD_ERRORED("Download Errored"),
        RIP_COMPLETE("Rip Complete");

        String value;
        STATUS(String value) {
            this.value = value;
        }
    }

    private STATUS status;
    private Object object;

    public RipStatusMessage(STATUS status, Object object) {
        this.status = status;
        this.object = object;
    }
    
    public STATUS getStatus() {
        return status;
    }
    
    public Object getObject() {
        return object;
    }

    public String toString() {
        return status.value + ": " + object.toString();
    }
}
