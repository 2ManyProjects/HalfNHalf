package com.halfnhalf.store;

import com.halfnhalf.Deal;

import java.util.ArrayList;

public class Store {

    public ArrayList<Deal> storeDeals = new ArrayList<Deal>();
    private String ID;
    private String name;
    private String address;
    private String seller = "";
    private String buyer = "";
    private int dealProgression;
    private int imageResource;
    private boolean isNew;

    public int getDealProgression() {
        return dealProgression;
    }

    public void setDealProgression(int dealProgression) {
        this.dealProgression = dealProgression;
    }


    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

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

    public Store(String id, String str, String address){
        this.ID = id;
        this.name = str;
        this.address = address;
    }
    public ArrayList<Deal> getData(){
        return this.storeDeals;
    }
    public void setStoreDeals(ArrayList<Deal> storeDeals) {
        this.storeDeals = storeDeals;
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

    public void addDeal(String rate, String txt, String amount, String currentAmnt, boolean atCost, boolean reoccuring, String period, String date) {
        String [] t = date.split("~");
        Deal temp = new Deal(rate, txt, amount, currentAmnt, atCost, reoccuring, period, t[0], t[1], t[2]);
        storeDeals.add(temp);
    }

    public void changeDeal(int index, String rate, String txt, String totalAmount, String currentAmnt, boolean atCost, boolean reoccur, String period, String year, String month, String day, String id ) {
        Deal temp = new Deal(rate, txt, totalAmount, currentAmnt, atCost, reoccur, period, year, month, day, id);
        if(index >= storeDeals.size()){
            storeDeals.add(temp);
            return;
        }
        storeDeals.set(index, temp);
    }

    public int getDealNum() {
        return storeDeals.size();
    }

    private String fixString(String str){
        return str.replaceAll("#", "~@");
    }

    @Override
    public String toString(){
        String temp = ID + "#" + name + "#" + fixString(address) + "#" + Integer.toString(storeDeals.size()) + "#";
        for(int i = 0; i < storeDeals.size(); i++){
            temp += storeDeals.get(i).toString();
        }
        return temp;
    }

    public boolean equals(Store store){
        return this.buyer.equals(store.getBuyer()) && this.seller.equals(store.getSeller()) && this.ID.equals(store.ID);
    }


}
