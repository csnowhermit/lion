package com.lion.vip.api.push;

/**
 * 枚举消息类型
 */
public enum MsgType {
    NOTIFICATION("提醒", 1),                   //会在通知栏显示
    MESSAGE("消息", 2),                         //不会在通知栏显示，业务自定义消息
    NOTIFICATION_AND_MESSAGE("提醒+消息", 3);   //1+2

    private final String desc;    //消息描述
    private final int value;      //消息所对应的值

    MsgType(String desc, int value) {
        this.desc = desc;
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

}
