package com.lion.vip.core.router;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.lion.vip.api.event.UserOfflineEvent;
import com.lion.vip.api.event.UserOnlineEvent;
import com.lion.vip.api.spi.common.MQClient;
import com.lion.vip.api.spi.common.MQClientFactory;
import com.lion.vip.common.router.RemoteRouterManager;
import com.lion.vip.common.user.UserManager;
import com.lion.vip.tools.event.EventConsumer;

import static com.lion.vip.api.event.Topics.OFFLINE_CHANNEL;
import static com.lion.vip.api.event.Topics.ONLINE_CHANNEL;

/**
 * 用户事件消费
 */
public final class UserEventConsumer extends EventConsumer {
    private final MQClient mqClient = MQClientFactory.create();

    private final UserManager userManager;    //用户管理

    public UserEventConsumer(RemoteRouterManager remoteRouterManager) {
        this.userManager = new UserManager(remoteRouterManager);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * 用户上线事件
     *
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    void on(UserOnlineEvent event) {
        userManager.addToOnlineList(event.getUserId());
        mqClient.publish(ONLINE_CHANNEL, event.getUserId());
    }

    /**
     * 用户下线事件
     *
     * @param event
     */
    @Subscribe
    @AllowConcurrentEvents
    void on(UserOfflineEvent event) {
        userManager.remFormOnlineList(event.getUserId());
        mqClient.publish(OFFLINE_CHANNEL, event.getUserId());
    }


}
