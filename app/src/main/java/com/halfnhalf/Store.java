package com.halfnhalf;

import java.util.ArrayList;
import java.util.List;

public class Store {
    public List<Deal> storeDeals = new ArrayList();
    private String ID;

    public void setID(String str) {
        ID = str;
    }

    public String getID() {
        return ID;
    }

    public void addDeal(double rate, String txt, int amount) {
        Deal temp = new Deal();
        temp.create(rate, txt, amount);
        storeDeals.add(temp);
    }

    public void changeDeal(int index, double rate, String txt, int amount ) {
        Deal temp = storeDeals.get(index);
        temp.create(rate, txt, amount);
        storeDeals.set(index, temp);
    }

    public int getDealNum() {
        return storeDeals.size();
    }

}
