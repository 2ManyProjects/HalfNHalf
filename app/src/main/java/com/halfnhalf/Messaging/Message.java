package com.halfnhalf.Messaging;

public class Message {
    private String text; // message body
    private MemberData data; // data of the user that sent this message

    private boolean belongsToCurrentUser; // is this message sent by us?

    public void setText(String text) {
        this.text = text;
    }

    public void setData(MemberData data) {
        this.data = data;
    }


    public void setData(String sender, String receiver) {
        MemberData temp = new MemberData(sender, receiver);
        this.data = temp;
    }

    public void setBelongsToCurrentUser(boolean belongsToCurrentUser) {
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return rebuildText(text);
    }

    private String cleanText(String t){
        return t.replaceAll("#", "$^");
    }

    private String rebuildText(String t){
        return t.replaceAll("$^", "#");
    }

    public MemberData getData() {
        return data;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }

    @Override
    public String toString(){
        return data.toString() + "#" +  cleanText(text) + "#";
    }
}