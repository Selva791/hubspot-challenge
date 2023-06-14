package com.hubspot.util;

public class HubspotEventException extends Exception{
    private int code;
    public HubspotEventException(String message){
        super(message);
    }
    public HubspotEventException(String message, int code) {
        super(message);
        this.code = code;
    }
}
