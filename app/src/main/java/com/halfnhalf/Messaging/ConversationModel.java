package com.halfnhalf.Messaging;

import java.util.ArrayList;

public class ConversationModel {
    String name = "";
    ArrayList<Message> msgs;

    public ConversationModel(String name, ArrayList<Message> msgs){
        this.name = name;
        this.msgs = msgs;
    }

    public String getName(){
        return name;
    }

    public ArrayList<Message> getMessages(){
        return msgs;
    }
}
