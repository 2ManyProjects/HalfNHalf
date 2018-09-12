package com.halfnhalf;

import java.io.Serializable;
import java.util.UUID;

public class Deal implements Serializable {
    private String rate, text, totalAmnt, id;

    public Deal(String rate, String text, String amnt){
        this.rate = rate;
        this.text = text;
        this.totalAmnt = amnt;
        this.id = UUID.randomUUID().toString();

    }

    public Deal(String rate, String text, String amnt, String id){
        this.rate = rate;
        this.text = text;
        this.totalAmnt = amnt;
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

    public String getTotalAmnt() {
        return totalAmnt;
    }

    @Override
    public String toString(){
        return rate + "#" + text + "#" + totalAmnt + "#";
    }
}
