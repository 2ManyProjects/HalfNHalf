package com.halfnhalf;

public class Deal {
    private String rate, text, Amnt;

    public void create(String rate, String text, String amnt){
        this.rate = rate;
        this.text = text;
        this.Amnt = amnt;

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
