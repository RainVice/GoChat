package com.rainvice.gochat.protocol;


/**
 * 自定义协议
 * 以 json 形式呈现
 */

public class RvResponseProtocol<T> extends RvRequestProtocol<T>{

    public static final Integer OK = 200;

    public static final Integer FAIL = 500;


    //状态码
    private Integer status;


    public RvResponseProtocol(String type, Integer status, T data) {
        super(type, data);
        this.status = status;
    }


    public RvResponseProtocol(String type, T data) {
        super(type, data);
        this.status = OK;
    }

    public RvResponseProtocol(){

    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
