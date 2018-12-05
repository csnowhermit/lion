package com.lion.vip.common.router;

import com.lion.vip.api.router.ClientLocation;
import com.lion.vip.api.router.Router;

/**
 * 远程路由
 */
public final class RemoteRouter implements Router<ClientLocation> {
    private final ClientLocation clientLocation;

    public RemoteRouter(ClientLocation clientLocation) {
        this.clientLocation = clientLocation;
    }

    public boolean isOnline() {
        return clientLocation.isOnline();
    }

    public boolean isOffline() {
        return clientLocation.isOffline();
    }

    @Override
    public ClientLocation getRouteValue() {
        return clientLocation;
    }

    @Override
    public RouterType getRouterType() {
        return RouterType.REMOTE;
    }

    @Override
    public String toString() {
        return "RemoteRouter{" + clientLocation + '}';
    }
}
