package com.rainvice.sockettest_1.bean;

import com.rainvice.sockettest_1.constant.DataType;

public class DialogBean {

        //是否我发送的消息
        private boolean isMine;

        //消息时间
        private String time;

        //消息类型
        private DataType dataType;

        //消息内容
        private Object content;

        public DialogBean() {
        }

        public DialogBean(boolean isMine, String time, DataType dataType, Object content) {
                this.isMine = isMine;
                this.time = time;
                this.dataType = dataType;
                this.content = content;
        }

        public boolean isMine() {
                return isMine;
        }

        public void setMine(boolean mine) {
                isMine = mine;
        }

        public String getTime() {
                return time;
        }

        public void setTime(String time) {
                this.time = time;
        }

        public DataType getDataType() {
                return dataType;
        }

        public void setDataType(DataType dataType) {
                this.dataType = dataType;
        }

        public Object getContent() {
                return content;
        }

        public void setContent(Object content) {
                this.content = content;
        }
}