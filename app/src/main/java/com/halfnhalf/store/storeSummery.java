package com.halfnhalf.store;

public class storeSummery {
    private String [] deals = {"", "", ""};
    private String address;
    private String name;
    private int index = 0;
    private String userName;
    private Store storeData;
    public storeSummery(String user, String name, String address){
        this.userName = user;
        this.name = name;
        this.address = address;
    }
    public void addDeal(String val){
        deals[index] = val+"%";
        index++;
    }
    public String[] getDeals(){
        return this.deals;
    }
    public String getAddress(){
        return this.address;
    }
    public String getName(){
        return this.name;
    }
    public String getUserName(){
        return this.userName;
    }
    public Store getStore(){
        return this.storeData;
    }
    public void setStore(Store s){
        this.storeData = s;
    }
}
