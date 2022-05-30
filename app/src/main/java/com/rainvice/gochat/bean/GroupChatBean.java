package com.rainvice.gochat.bean;

public class GroupChatBean {

    private String username;
    private String message;
    private String msgType;

    public GroupChatBean() {
    }

    public GroupChatBean(String username, String message,String msgType) {
        this.username = username;
        this.message = message;
        this.msgType = msgType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}
