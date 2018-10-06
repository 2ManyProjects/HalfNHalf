package com.halfnhalf;

import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Deal implements Serializable {
    private String text = "";
    private String totalAmnt = "0";
    private String currentAmnt = "0";
    private String id = "";
    private String period = "0";
    private String rate = "0";
    private boolean limit = true;
    private boolean atCost = false;
    private boolean reoccuring = false;
    private Calendar currentDate;
    private Calendar resetDate;
    private int selected = 0;
    private int selectedAmnt = 0;

    public int getSelected(){
        return selected;
    }

    public void setSelected(int sel){
        this.selected = sel;
    }

    public int getSelectedAmnt(){
        return selectedAmnt;
    }

    public void setSelectedAmnt(int sel){
        if(sel > Integer.parseInt(currentAmnt))
            sel = Integer.parseInt(currentAmnt);
        this.selectedAmnt = sel;
    }
    public boolean getReoccuring() {
        return reoccuring;
    }

    public Calendar getResetDate(){
        return resetDate;
    }

    public String getCurrentAmnt() {
        return currentAmnt;
    }

    public boolean getAtCost() {
        return atCost;
    }

    public boolean getLimit(){
        return limit;
    }

    public String getId() {
        return id;
    }

    public String getRate() {
        return rate;
    }

    public String getText() {
        return fix(text);
    }

    public String getTotalAmnt() {
        return totalAmnt;
    }

    public String getPeriodVal(){
        return period;
    }

    public int getPeriod(){
        switch (period){
            case "0":
                return 7;
            case "1":
                return 14;
            case "2":
                return 30;
            case "3":
                return 60;
            case "4":
                return 90;
            case "5":
                return 180;
            case "6":
                return 360;
            case "7":
                return 720;
            case "8":
                return 1080;
            default:
                return 0;
        }
    }

    @Override
    public String toString(){
        String temp = "";
        if(this.reoccuring) {
            temp = rate + "#" + text + "#" + totalAmnt + "#" + currentAmnt + "#" + Boolean.toString(atCost) + "#" + Boolean.toString(reoccuring) + "#" + period + "#" + resetDate.get(Calendar.YEAR) + "~" + resetDate.get(Calendar.MONTH) + "~" + resetDate.get(Calendar.DAY_OF_MONTH) + "#";
        }else{
            temp = rate + "#" + text + "#" + totalAmnt + "#" + currentAmnt + "#" + Boolean.toString(atCost) + "#" + Boolean.toString(reoccuring) + "#" + "11" + "#" + "0" + "~" + "0" + "~" + "0" + "#";
        }
        Log.e("Deal Data", "" + temp + " Reoccuring" + Boolean.toString(this.reoccuring));
        return temp;
    }
    public String toStringSelection(){
        String temp = "";
        if(this.reoccuring) {
            temp = rate + "#" + text + "#" + totalAmnt + "#" + currentAmnt + "#" + Integer.toString(selectedAmnt) + "#" + Boolean.toString(atCost) + "#" + Boolean.toString(reoccuring) + "#" + period + "#" + resetDate.get(Calendar.YEAR) + "~" + resetDate.get(Calendar.MONTH) + "~" + resetDate.get(Calendar.DAY_OF_MONTH) + "#";
        }else{
            temp = rate + "#" + text + "#" + totalAmnt + "#" + currentAmnt + "#" + Integer.toString(selectedAmnt) + "#" + Boolean.toString(atCost) + "#" + Boolean.toString(reoccuring) + "#" + "11" + "#" + "0" + "~" + "0" + "~" + "0" + "#";
        }
        return temp + Integer.toString(selected) + "#";
    }



    public Deal(String rate, String text, String totalAmnt, String currentAmnt, boolean atCost, boolean reoccuring, String period, String year, String month, String day){
        this.rate = rate;
        this.text = " " + clean(text);
        this.totalAmnt = totalAmnt;
        this.currentAmnt = currentAmnt;
        if(Integer.parseInt(this.totalAmnt) >= 50) {
            limit = false;
            this.currentAmnt = "50";
        }
        this.id = UUID.randomUUID().toString();
        this.reoccuring = reoccuring;
        this.atCost = atCost;
        this.period = period;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        this.resetDate = calendar;
        Calendar temp = Calendar.getInstance();
        temp.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        this.currentDate = temp;
        if(limit && reoccuring){
            if(this.currentDate.after(this.resetDate)){
                this.currentAmnt = this.totalAmnt;
                while(this.currentDate.after(this.resetDate)){
                    this.resetDate.add(this.resetDate.DATE, getPeriod());
                }
            }
        }
    }

    private String clean(String str){
        String temp = "";
        temp = str.replaceAll("#", "~@");
        return temp;
    }


    private String fix(String str){
        String temp = "";
        temp = str.replaceAll("~@", "#");
        return temp;
    }



    public Deal(String rate, String text, String totalAmnt, String currentAmnt, boolean atCost, boolean reoccuring, String period, String year, String month, String day, String id){
        this.rate = rate;
        this.text = " " + clean(text);
        this.totalAmnt = totalAmnt;
        this.currentAmnt = currentAmnt;
        if(Integer.parseInt(this.totalAmnt) >= 50)
            this.limit = false;
        this.id = UUID.randomUUID().toString();
        this.reoccuring = reoccuring;
        this.atCost = atCost;
        this.period = period;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        this.resetDate = calendar;
        Calendar temp = Calendar.getInstance();
        this.currentDate = temp;
        this.id = id;
        if(limit && reoccuring){
            if(this.currentDate.after(this.resetDate)){
                this.currentAmnt = this.totalAmnt;
                while(this.currentDate.after(this.resetDate)){
                    this.resetDate.add(this.resetDate.DATE, getPeriod());
                }
            }
        }
    }
}
