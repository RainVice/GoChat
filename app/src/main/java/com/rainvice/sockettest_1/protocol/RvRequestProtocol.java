package com.rainvice.sockettest_1.protocol;


/**
 * 自定义协议
 * 以 json 形式呈现
 */

public class RvRequestProtocol<T> {

    //消息类型
    private String type;

    //消息内容
    private T data;


    public RvRequestProtocol(){

    }

    public RvRequestProtocol(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public RvRequestProtocol(T data) {
        this.data = data;
        this.type = MsgType.MESSAGE;
    }

    public static RvRequestProtocol<String> getName(){
        return new RvRequestProtocol<String>(MsgType.GET_NAME,null);
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
