/**
 * FileName: PushContext
 * Author:   Ren Xiaotian
 * Date:     2018/11/21 14:44
 */

package com.lion.vip.api.push;

import com.lion.vip.api.Constants;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 推送上下文
 */
public class PushContext {
    private byte[] context;    //待推送的内容
    private PushMsg pushMsg;   //待推送的消息
    private String userId;     //目标用户
    private List<String> userIds;    //目标用户，批量
    private AckModel ackModel = AckModel.NO_ACK;    //消息确认模式，默认无确认
    private PushCallback callback;    //推送成功后的回调
    private int timeout = 3000;    //推送超时时间，默认3s

    private boolean broadcast = false;    //全网广播在线用户，默认为false
    private Set<String> tags;             //用户标签过滤，目前只有include，后续会增加exclude

    /**
     * 条件表达式, 满足条件的用户会被推送，目前支持的脚本语言为js
     * 可以使用的参数为 userId,tags,clientVersion,osName,osVersion
     * 比如 :
     * 灰度：userId % 100 < 20
     * 包含test标签：tags!=null && tags.indexOf("test")!=-1
     * 判断客户端版本号：clientVersion.indexOf("android")!=-1 && clientVersion.replace(/[^\d]/g,"") > 20
     * 等等
     */
    private String condition;
    private String taskId;     //广播的时候考虑生成一个ID，便于控制任务


    public PushContext(byte[] context) {
        this.context = context;
    }

    public PushContext(PushMsg pushMsg) {
        this.pushMsg = pushMsg;
    }

    public static PushContext build(String msg) {
        return new PushContext(msg.getBytes(Constants.UTF_8));
    }

    public static PushContext build(PushMsg msg) {
        return new PushContext(msg);
    }

    public String getTaskId() {
        return UUID.randomUUID().toString();
    }


    public byte[] getContext() {
        return context;
    }

    public PushContext setContext(byte[] context) {
        this.context = context;
        return this;
    }

    public PushMsg getPushMsg() {
        return pushMsg;
    }

    public PushContext setPushMsg(PushMsg pushMsg) {
        this.pushMsg = pushMsg;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public PushContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public PushContext setUserIds(List<String> userIds) {
        this.userIds = userIds;
        return this;
    }

    public AckModel getAckModel() {
        return ackModel;
    }

    public PushContext setAckModel(AckModel ackModel) {
        this.ackModel = ackModel;
        return this;
    }

    public PushCallback getCallback() {
        return callback;
    }

    public PushContext setCallback(PushCallback callback) {
        this.callback = callback;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public PushContext setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public PushContext setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public PushContext setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public PushContext setCondition(String condition) {
        this.condition = condition;
        return this;
    }
}
