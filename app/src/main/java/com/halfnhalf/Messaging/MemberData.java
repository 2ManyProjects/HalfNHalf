package com.halfnhalf.Messaging;

public class MemberData {
    private String receiver;
    private String sender;

    public MemberData(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
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