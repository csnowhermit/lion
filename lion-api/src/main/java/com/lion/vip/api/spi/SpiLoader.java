package com.lion.vip.api.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI加载器
 */
public class SpiLoader {
    private static final ConcurrentHashMap<String, Object> CACHE = new ConcurrentHashMap<>();

    public static void clear() {
        CACHE.clear();
    }

    public static <T> T load(Class<T> clazz) {
        return load(clazz, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> T load(Class<T> clazz, String name) {
        String key = clazz.getName();
        Object o = CACHE.get(key);
        if (o == null) {
            T t = load0(clazz, name);
            if (t != null) {
                CACHE.put(key, t);
                return t;
            }
        } else if (clazz.isInstance(0)) {
            return (T) o;
        }
        return load0(clazz, name);
    }

    private static <T> T load0(Class<T> clazz, String name) {
        ServiceLoader<T> factories = ServiceLoader.load(clazz);
        T t = filterByName(factories, name);

        if (t == null) {
            factories = ServiceLoader.load(clazz, SpiLoader.class.getClassLoader());
            t = filterByName(factories, name);
        }
        if (t != null) {
            return t;
        } else {
            throw new IllegalStateException("Cannot find META-INF/services/" + clazz.getName() + " on classpath");
        }
    }

    private static <T> T filterByName(ServiceLoader<T> factories, String name) {
        Iterator<T> iterator = factories.iterator();
        if (name == null) {
            List<T> list = new ArrayList<T>(2);
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
            if (list.size() > 1) {
                list.sort((o1, o2) -> {
                    Spi spi1 = o1.getClass().getAnnotation(Spi.class);
                    Spi spi2 = o2.getClass().getAnnotation(Spi.class);
                    int order1 = spi1 == null ? 0 : spi1.order();
                    int order2 = spi2 == null ? 0 : spi2.order();
                    return order1 - order2;
                });
            }
            if (list.size() > 0) {
                return list.get(0);
            }
        } else {
            while (iterator.hasNext()) {
                T t = iterator.next();
                if (name.equals(t.getClass().getName())
                        || name.equals(t.getClass().getSimpleName())) {
                    return t;
                }
            }
        }
        return null;
    }
}
