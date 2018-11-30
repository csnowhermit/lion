/**
 * FileName: ScriptCondition
 * Author:   Ren Xiaotian
 * Date:     2018/11/30 20:31
 */

package com.lion.vip.common.condition;

import com.lion.vip.api.common.Condition;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;

public final class ScriptCondition implements Condition {
    private static final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static final ScriptEngine jsEngine = scriptEngineManager.getEngineByName("js");

    private final String script;

    public ScriptCondition(String script) {
        this.script = script;
    }

    @Override
    public boolean test(Map<String, Object> env) {
        try {
            return (boolean) jsEngine.eval(script, new SimpleBindings(env));
        } catch (ScriptException e) {
            return false;
        }
    }
}
