package com.lion.vip.common.condition;

import com.lion.vip.api.common.Condition;

import java.util.Map;

public final class AwaysPassCondition implements Condition {
    public static final Condition I = new AwaysPassCondition();

    @Override
    public boolean test(Map<String, Object> stringObjectMap) {
        return true;
    }
}
