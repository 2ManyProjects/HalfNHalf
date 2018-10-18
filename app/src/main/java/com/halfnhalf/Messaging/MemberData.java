package com.halfnhalf.Messaging;

public class MemberData {
    private String receiver;
    private String sender;
    private boolean System = false;

    public MemberData(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public boolean getSystem() {
        return System;
    }
    public void setSystem(boolean system){
        this.System = system;
    }
    public String getSender() {
        return sender;
    }
    public String getReceiver() {
        return receiver;
    }


    @Override
    public String toString() {
        return sender + '#' + receiver;
    }
}