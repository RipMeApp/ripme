package com.rarchives.ripme.ui;

/**
 * Contains information about downloaded files and rips, and their states.
 */
public class RipStatusMessage {

    public enum STATUS {
        LOADING_RESOURCE("Loading Resource"),
        DOWNLOAD_STARTED("Download Started"),
        DOWNLOAD_COMPLETE("Download Complete"),
        DOWNLOAD_ERRORED("Download Errored"),
        RIP_COMPLETE("Rip Complete"), 
        DOWNLOAD_WARN("Download problem"),
        TOTAL_BYTES("Total bytes"),
        COMPLETED_BYTES("Completed bytes"),
        RIP_ERRORED("Rip Errored");

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

    @Override
    public String toString() {
        return status.value + ": " + object.toString();
    }
}
