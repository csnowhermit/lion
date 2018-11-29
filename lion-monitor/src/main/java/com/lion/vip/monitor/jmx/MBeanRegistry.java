/**
 * FileName: MBeanRegistry
 * Author:   Ren Xiaotian
 * Date:     2018/11/26 14:31
 */

package com.lion.vip.monitor.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MBeanRegistry {
    public static final String DOMAIN = "com.lion.vip";
    private static final Logger LOGGER = LoggerFactory.getLogger(MBeanRegistry.class);

    private static MBeanRegistry instance = new MBeanRegistry();

    private Map<MBeanInfo, String> mapBean2Path = new ConcurrentHashMap<>();
    private MBeanServer mBeanServer;

    public static MBeanRegistry getInstance() {
        return instance;
    }

    public MBeanRegistry() {
        try {
            this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
        } catch (Error e) {
            this.mBeanServer = MBeanServerFactory.createMBeanServer();
        }
    }

    public MBeanServer getmBeanServer() {
        return mBeanServer;
    }


    /**
     * Registers a new MBean with the platform MBean server.
     *
     * @param bean   the bean being registered
     * @param parent if not null, the new bean will be registered as a child
     *               node of this parent.
     */
    public void register(MBeanInfo bean, MBeanInfo parent) {
        assert bean != null;
        String path = null;

        if (parent != null) {
            path = mapBean2Path.get(parent);
            assert parent != null;
        }

        path = makeFullPath(path, parent);
        try {
            ObjectName objectName = makeObjectName(path, bean);
            mBeanServer.registerMBean(bean, objectName);
            mapBean2Path.put(bean, path);
        } catch (JMException e) {
            LOGGER.warn("Failed to register MBean " + bean.getName());
            throw new MException(e);
        }
    }

    private ObjectName makeObjectName(String path, MBeanInfo bean) throws MalformedObjectNameException {
        if (path == null) {
            return null;
        }

        StringBuilder beanName = new StringBuilder(DOMAIN).append(":");
        int counter = 0;
        counter = tokenize(beanName, path, counter);
        tokenize(beanName, bean.getName(), counter);
        beanName.deleteCharAt(beanName.length() - 1);
        try {
            return new ObjectName(beanName.toString());
        } catch (MalformedObjectNameException e) {
            LOGGER.warn("Invalid name \"" + beanName.toString() + "\" for class "
                    + bean.getClass().toString());
            throw e;
        }
    }

    private int tokenize(StringBuilder sb, String path, int index) {
        String[] tokens = path.split("/");
        for (String s : tokens) {
            if (s.length() == 0) {
                continue;
            }
            sb.append("name").append(index++)
                    .append("=").append(s).append(",");
        }
        return index;
    }

    protected String makeFullPath(String prefix, MBeanInfo bean) {
        return makeFullPath(prefix, bean == null ? null : bean.getName());
    }

    /**
     * 组装成操作系统路径一样的全路径
     *
     * @param prefix 前缀
     * @param name   各级节点的名称
     * @return
     */
    private String makeFullPath(String prefix, String... name) {
        StringBuilder sb = new StringBuilder(prefix == null ? "/" : (prefix.equals("/") ? prefix : prefix + "://"));
        boolean first = true;
        for (String s : name) {
            if (s == null) {
                continue;
            }
            if (!first) {
                sb.append("/");
            } else {
                first = false;
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 取消注册
     *
     * @param path
     * @param bean
     */
    private void unregister(String path, MBeanInfo bean) {
        if (path == null) {
            return;
        }
        try {
            mBeanServer.unregisterMBean(makeObjectName(path, bean));
        } catch (JMException e) {
            LOGGER.error("Failed to unregister MBean " + bean.getName());
            throw new MException(e);
        }
    }

    public void unregister(MBeanInfo bean) {
        if (bean == null) {
            return;
        }

        String path = mapBean2Path.get(bean);
        unregister(path, bean);
        mapBean2Path.remove(bean);
    }

    /**
     * 所有的MBean都反注册
     */
    public void unregister() {
        for (Map.Entry<MBeanInfo, String> entry : mapBean2Path.entrySet()) {
            try {
                unregister(entry.getValue(), entry.getKey());
            } catch (MException e) {
                LOGGER.warn("Error during unregister, ", e);
            }
        }
        mapBean2Path.clear();
    }


//    public static void main(String[] args) {
//        System.out.println(new MBeanRegistry().makeFullPath("https", "aaa", "bbb", "ccc"));
//    }
}
