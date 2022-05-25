package com.rainvice.sockettest_1.bean;

import com.rainvice.sockettest_1.protocol.RvRequestProtocol;

//接受到的信息
public class InputMsgBean {

    //发送者的IP
    private String ip;
    //发送方者发送的消息
    private RvRequestProtocol<String> protocol;

    public InputMsgBean() {
    }

    public InputMsgBean(String ip, RvRequestProtocol<String> protocol) {
        this.ip = ip;
        this.protocol = protocol;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public RvRequestProtocol<String> getProtocol() {
        return protocol;
    }

    public void setProtocol(RvRequestProtocol<String> protocol) {
        this.protocol = protocol;
    }
}
