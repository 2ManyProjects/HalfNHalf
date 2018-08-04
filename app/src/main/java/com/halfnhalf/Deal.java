package com.halfnhalf;

public class Deal {
    private double rate;
    private String text;
    private int Amnt;

    public void create(double rate, String text, int amnt){
        this.rate = rate;
        this.text = text;
        this.Amnt = amnt;

    }
    public double getRate() {
        return rate;
    }

    public String getText() {
        return text;
    }

    public int getAmnt() {
        return Amnt;
    }


}
