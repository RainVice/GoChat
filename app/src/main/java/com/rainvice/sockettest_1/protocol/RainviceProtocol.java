package com.rainvice.sockettest_1.protocol;


/**
 * 自定义协议
 * 以 json 形式呈现
 */

public class RainviceProtocol<T> {

    //消息类型
    private String type;

    //消息内容
    private T data;


    public RainviceProtocol(){

    }

    public RainviceProtocol(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public RainviceProtocol(String type) {
        this.type = type;
        this.data = null;
    }

    public RainviceProtocol(T data) {
        this.data = data;
        this.type = MsgType.MESSAGE;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
