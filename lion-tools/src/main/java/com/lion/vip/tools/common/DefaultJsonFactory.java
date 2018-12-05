package com.lion.vip.tools.common;

import com.lion.vip.api.spi.Spi;
import com.lion.vip.api.spi.common.Json;
import com.lion.vip.api.spi.common.JsonFactory;
import com.lion.vip.tools.Jsons;

@Spi
public final class DefaultJsonFactory implements JsonFactory, Json {
    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return Jsons.fromJson(json, clazz);
    }

    @Override
    public String toJson(Object json) {
        return Jsons.toJson(json);
    }

    @Override
    public Json get() {
        return this;
    }
}
