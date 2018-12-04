package com.wuwenze.wechatsmallapptmplmsg.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Assert extends cn.hutool.core.lang.Assert {

    public static void notAnyEmpty(String... valueAndMessages) {
        Map<String, String> valueAndMessagesMap = MapUtil.newHashMap(valueAndMessages);
        if (null == valueAndMessagesMap) {
            log.warn("Assert#notAnyEmpty() valueAndMessages is null.");
            return;
        }
        valueAndMessagesMap.keySet().forEach((fieldValue) -> Assert.notEmpty(fieldValue, valueAndMessagesMap.get(fieldValue)));
    }

    public static void notAnyNull(Object... valueAndMessages) {
        Map<Object, Object> valueAndMessagesMap = MapUtil.newHashMap(valueAndMessages);
        if (null == valueAndMessagesMap) {
            log.warn("Assert#notAnyEmpty() valueAndMessages is null.");
            return;
        }
        valueAndMessagesMap.keySet().forEach((fieldValue) -> {
            if (null == fieldValue) {
                throw new IllegalArgumentException((String) valueAndMessagesMap.get(fieldValue));
            }
        });
    }
}
