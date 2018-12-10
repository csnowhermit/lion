package com.lion.vip.core.router;

import com.lion.vip.api.connection.Connection;
import com.lion.vip.api.router.Router;

/**
 * 本地路由
 */
public final class LocalRouter implements Router<Connection> {

    private Connection connection;

    public LocalRouter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public RouterType getRouterType() {
        return RouterType.LOCAL;
    }

    public byte getClientType() {
        return connection.getSessionContext().getClientType();
    }

    @Override
    public Connection getRouteValue() {
        return connection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LocalRouter that = (LocalRouter) o;

        return getClientType() == that.getClientType();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getClientType());
    }

    @Override
    public String toString() {
        return "LocalRouter{" + connection + '}';
    }
}
