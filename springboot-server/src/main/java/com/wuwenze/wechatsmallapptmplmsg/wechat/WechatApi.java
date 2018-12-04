package com.wuwenze.wechatsmallapptmplmsg.wechat;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author wwz
 * @version 1 (2018/8/20)
 * @since Java7
 */
@Slf4j
public class WechatApi {
    private final static LoadingCache<String, String> mAccessTokenCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(7200, TimeUnit.SECONDS)
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String key) {
                            // key: appId#appSecret
                            String[] array = key.split("#");
                            if (null == array || array.length != 2) {
                                throw new IllegalArgumentException("load access_token error, key = " + key);
                            }
                            return getAccessToken(array[0], array[1]);
                        }
                    });

    public static String getAccessToken() {
        String cacheKey = WechatConf.appId + "#" + WechatConf.appSecrct;
        try {
            return mAccessTokenCache.get(cacheKey);
        } catch (ExecutionException e) {
            log.error("#getAccessToken error, cacheKey=" + cacheKey, e);
        }
        return null;
    }

    private static String getAccessToken(String appId, String appSecret) {
        String apiUrl = StrUtil.format(//
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}",//
                appId, appSecret
        );
        String body = HttpRequest.get(apiUrl).execute().body();
        return throwErrorMessageIfExists(body).getString("access_token");
    }

    public static void templateSend(String accessToken, WechatTemplate template) {
        String apiUrl = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token="//
                + (StrUtil.isEmpty(accessToken) ? getAccessToken() : accessToken);
        String body = HttpRequest.post(apiUrl).body(JSON.toJSONString(template)).execute().body();
        throwErrorMessageIfExists(body);
    }

    public static JSONObject getOpenIdByJSCode(String js_code) {
        String apiUrl = StrUtil.format(//
                "https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code",//
                WechatConf.appId, WechatConf.appSecrct, js_code
        );
        String body = HttpRequest.get(apiUrl).execute().body();
        return throwErrorMessageIfExists(body);
    }

    private static JSONObject throwErrorMessageIfExists(String body) {
        String callMethodName = (new Throwable()).getStackTrace()[1].getMethodName();
        log.info("#0820 {} body={}", callMethodName, body);
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject.containsKey("errcode") && jsonObject.getIntValue("errcode") > 0) {
            throw new RuntimeException(StrUtil.format("#WechatApi[{}] call error: {}", callMethodName, body));
        }
        return jsonObject;
    }
}
