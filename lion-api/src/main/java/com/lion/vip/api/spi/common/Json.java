package com.lion.vip.api.spi.common;

/**
 * Json SPI扩展
 */
public interface Json {

    /**
     * 创建json
     */
    Json JSON = JsonFactory.create();

    /**
     * 将json转换为对象
     *
     * @param json
     * @param claxx
     * @param <T>
     * @return
     */
    <T> T fromJson(String json, Class<T> claxx);

    /**
     * 对象转换为json字符串
     *
     * @param json
     * @return
     */
    String toJson(Object json);
}
