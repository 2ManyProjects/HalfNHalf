package com.halfnhalf.store;

import com.halfnhalf.Deal;

import java.util.ArrayList;

public class Store {
    public ArrayList<Deal> storeDeals = new ArrayList<Deal>();
    private String ID;
    private String name;
    private String address;
    private int imageResource;
    private boolean isNew;

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        this.isNew = aNew;
    }

    public Store(String id, String str, String address, int imageResource){
        this.ID = id;
        this.name = str;
        this.address = address;
        this.imageResource = imageResource;
    }

    public ArrayList<Deal> getData(){
        return this.storeDeals;
    }
    public Deal getData(int i){
        return this.storeDeals.get(i);
    }
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getID() {
        return ID;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void addDeal(String rate, String txt, String amount) {
        Deal temp = new Deal(rate, txt, amount);
        storeDeals.add(temp);
    }

    public void changeDeal(int index, String rate, String txt, String amount, String id ) {
        if(index >= storeDeals.size()){
            Deal temp = new Deal(rate, txt, amount, id);
            storeDeals.add(temp);
            return;
        }
        Deal temp = new Deal(rate, txt, amount, id);
        storeDeals.set(index, temp);
    }

    public int getDealNum() {
        return storeDeals.size();
    }

}
