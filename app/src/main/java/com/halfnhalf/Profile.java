package com.halfnhalf;

import java.util.ArrayList;
import java.util.List;

public class Profile {

    public List<Store> storeList = new ArrayList();
    private String email;
    private String username;
    private int storenum = 0;
    private String userData;

    public String getUserData() {
        return userData;
    }

    public void setUserData(String data) {
        this.userData = data;
    }

}
