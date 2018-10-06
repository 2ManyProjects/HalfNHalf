package com.halfnhalf.store;

import com.google.gson.Gson;

public class storeSummery {
    private String [] deals = {"", "", ""};
    private String address;
    private String name;
    private int index = 0;
    private String userName;
    private Store storeData;
    private String snapshotGson;


    public void setsnapshotGson(String snapshotGson) {
        this.snapshotGson = snapshotGson;
    }

    public String getsnapshotGson() {
        return snapshotGson;
    }

    public storeSummery(String user, String name, String address){
        this.userName = user;
        this.name = name;
        this.address = address;
    }

    public void addDeal(String val, boolean atCost){
        float value = 0.0f;
        if(atCost) {
            if(Integer.parseInt(val) < 20)
                value = Float.parseFloat(val) * 1.50f;
            else
                value = Float.parseFloat(val) * 1.20f;
        }else {
            value = Float.parseFloat(val) * 0.90f;
        }
        deals[index] = Integer.toString((int) Math.floor(value))+"%";
//        deals[index] = val;
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
