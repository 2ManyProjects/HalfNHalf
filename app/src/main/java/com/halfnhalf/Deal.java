package com.halfnhalf;

import java.io.Serializable;
import java.util.UUID;

public class Deal implements Serializable {
    private String rate, text, Amnt, id;

    Deal(String rate, String text, String amnt){
        this.rate = rate;
        this.text = text;
        this.Amnt = amnt;
        this.id = UUID.randomUUID().toString();

    }

    Deal(String rate, String text, String amnt, String id){
        this.rate = rate;
        this.text = text;
        this.Amnt = amnt;
        this.id = id;

    }

    public String getId() {
        return id;
    }

    public String getRate() {
        return rate;
    }

    public String getText() {
        return text;
    }

    public String getAmnt() {
        return Amnt;
    }
}
