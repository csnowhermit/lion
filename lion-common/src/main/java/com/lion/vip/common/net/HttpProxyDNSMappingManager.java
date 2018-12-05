package com.lion.vip.common.net;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lion.vip.api.service.BaseService;
import com.lion.vip.api.service.Listener;
import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.ServiceDiscoveryFactory;
import com.lion.vip.api.spi.net.DNSMapping;
import com.lion.vip.api.spi.net.DNSMappingManager;
import com.lion.vip.api.srd.ServiceDiscovery;
import com.lion.vip.api.srd.ServiceListener;
import com.lion.vip.api.srd.ServiceNode;
import com.lion.vip.tools.Jsons;
import com.lion.vip.tools.config.CC;
import com.lion.vip.tools.thread.NamedPoolThreadFactory;
import com.lion.vip.tools.thread.ThreadNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.lion.vip.api.srd.ServiceNames.DNS_MAPPING;
import static com.lion.vip.tools.Utils.checkHealth;

@Spi(order = 1)
public class HttpProxyDNSMappingManager extends BaseService implements DNSMappingManager, Runnable, ServiceListener {
    private final Logger LOGGER = LoggerFactory.getLogger(HttpProxyDNSMappingManager.class);

    protected final Map<String, List<DNSMapping>> mappings = Maps.newConcurrentMap();

    private final Map<String, List<DNSMapping>> all = Maps.newConcurrentMap();
    private Map<String, List<DNSMapping>> available = Maps.newConcurrentMap();

    private ScheduledExecutorService executorService;

    @Override
    protected void doStart(Listener listener) throws Throwable {
        ServiceDiscovery discovery = ServiceDiscoveryFactory.create();
        discovery.subscribe(DNS_MAPPING, this);
        discovery.lookup(DNS_MAPPING).forEach(this::add);

        if (all.size() > 0) {
            executorService = Executors.newSingleThreadScheduledExecutor(
                    new NamedPoolThreadFactory(ThreadNames.T_HTTP_DNS_TIMER)
            );
            executorService.scheduleAtFixedRate(this, 1, 20, TimeUnit.SECONDS); //20秒 定时扫描dns
        }
        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (executorService != null) {
            executorService.shutdown();
        }
        listener.onSuccess();
    }

    @Override
    public void init() {
        all.putAll(CC.lion.http.dns_mapping);
        available.putAll(CC.lion.http.dns_mapping);
    }

    @Override
    public boolean isRunning() {
        return executorService != null && !executorService.isShutdown();
    }

    public void update(Map<String, List<DNSMapping>> nowAvailable) {
        available = nowAvailable;
    }

    public Map<String, List<DNSMapping>> getAll() {
        return all;
    }

    public DNSMapping lookup(String origin) {
        List<DNSMapping> list = mappings.get(origin);

        if (list == null || list.isEmpty()) {
            if (available.isEmpty()) return null;
            list = available.get(origin);
        }

        if (list == null || list.isEmpty()) return null;
        int L = list.size();
        if (L == 1) return list.get(0);
        return list.get((int) (Math.random() * L % L));
    }

    @Override
    public void run() {
        LOGGER.debug("do dns mapping checkHealth ...");
        Map<String, List<DNSMapping>> all = this.getAll();
        Map<String, List<DNSMapping>> available = Maps.newConcurrentMap();
        all.forEach((key, dnsMappings) -> {
            List<DNSMapping> okList = Lists.newArrayList();
            dnsMappings.forEach(dnsMapping -> {
                if (checkHealth(dnsMapping.getIp(), dnsMapping.getPort())) {
                    okList.add(dnsMapping);
                } else {
                    LOGGER.warn("dns can not reachable:" + Jsons.toJson(dnsMapping));
                }
            });
            available.put(key, okList);
        });
        this.update(available);
    }

    @Override
    public void onServiceAdded(String path, ServiceNode node) {
        add(node);
    }

    @Override
    public void onServiceUpdated(String path, ServiceNode node) {
        add(node);
    }

    @Override
    public void onServiceRemoved(String path, ServiceNode node) {
        mappings.computeIfAbsent(node.getAttr("origin"), k -> new ArrayList<>())
                .remove(new DNSMapping(node.getHost(), node.getPort()));
    }

    private void add(ServiceNode node){
        mappings.computeIfAbsent(node.getAttr("origin"), k -> new ArrayList<>())
                .add(new DNSMapping(node.getHost(), node.getPort()));
    }
}

