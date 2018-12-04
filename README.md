模板消息是基于微信的通知渠道，为开发者提供了可以高效触达用户的模板消息能力，以便实现服务的闭环并提供更佳的体验。

想推送模板消息，得满足一些前提条件：
1. 用户在小程序中完成支付后，小程序可以向用户发送模板消息。
2. 用户在小程序中有提交表单的行为，小程序可以向用户发送模板消息。
例如：
1. 用户在小程序里购买了商品，小程序可以将商品物流的情况，实时发送给用户。
2. 用户在小程序里填写了活动报名表后，小程序可以将报名情况（成功或失败）推送给用户。
> 需要注意的是，即使条件达成了，小程序也不能无限制地发送模板消息。

具体的发送数量限制是：
1. 用户完成一次支付，小程序可以获得 3 次发送模板消息的机会。
2. 用户提交一次表单，小程序可以获得 1 次发送模板消息的机会。
3. 发送模板消息的机会在用户完成操作后的 7 天内有效。一旦超过 7 天，这些发送资格将会自动失效。
<!--- more --->
## 前置准备工作
### 内网穿透（需要支持80端口、绑定`已备案域名`、`SSL证书`）用于开发时调试后端接口。
> 源码中已提供该工具

![](http://cdn.cloudly.cn/blog_img_5752495be14d4a6388ccad27531bf462.png!blog)

### 注册小程序账号，同时申请或`定制`对应的模板消息，拿到模板ID和模板结构备用。
> https://mp.weixin.qq.com/wxopen/waregister?action=step1

![](http://cdn.cloudly.cn/blog_img_e5da7038fc6945939ac8bb5b95a5349a.png!blog)
![](http://cdn.cloudly.cn/blog_img_b26db5d660a64e79857fe48e2a459058.png!blog)
可以选择自行定制模板消息格式，但是最终需要微信审核后方可使用，这里我们测试，就随意在模板库中挑选了一款，最终得到模板消息格式如下：

```json
购买地点 {{keyword1.DATA}}
购买时间 {{keyword2.DATA}}
物品名称 {{keyword3.DATA}}
交易单号 {{keyword4.DATA}}
```

### 配置可信服务器域名
![](http://cdn.cloudly.cn/blog_img_ff24f35bd57f47e3b6d704e82f6191c1.png!blog)
此处的可信域名，最终为内网穿透映射的域名，用于小程序向本地后端接口发送HTTP请求。

## 相关的微信API
### 获取AccessToken [GET]

https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET

参数 | 是否必须 | 说明
:--- | :--- | :---
grant_type | 是| 获取access_token填写client_credential
appid | 是 | 第三方用户唯一凭证
secret | 是 | 第三方用户唯一凭证密钥，即appsecret

正常情况下，微信会返回下述JSON数据包给公众号：

```json
{"access_token":"ACCESS_TOKEN","expires_in":7200}
```

### 登录凭证校验: 根据js_code换取当前用户的openId [GET]

先通过小程序获取当前用户的js_code，再调用相关接口接口换取openId

`wx.login(OBJECT)`

调用接口wx.login() 获取临时登录凭证（js_code）

```js
wx.login({
  success: function(res) {
    if (res.code) {
      // 获取到js_code, 可继续调用接口换取openId
    } else {
      console.log('登录失败！' + res.errMsg)
    }
  }
});
```

https://api.weixin.qq.com/sns/jscode2session?appid={}&secret={}&js_code={}&grant_type=authorization_code

参数 | 是否必须 | 说明
:--- | :--- | :---
appid | 是| 小程序唯一标识
secret | 是 | 小程序的 app secret
js_code | 是 | 登录时获取的 code
grant_type | 是 | 填写为 authorization_code

```json
//正常返回的JSON数据包
{
    "openid": "OPENID",
    "session_key": "SESSIONKEY",
}

//满足UnionID返回条件时，返回的JSON数据包
{
    "openid": "OPENID",
    "session_key": "SESSIONKEY",
    "unionid": "UNIONID"
}
//错误时返回JSON数据包(示例为Code无效)
{
    "errcode": 40029,
    "errmsg": "invalid code"
}
```

### 发送模板消息 [POST]
https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token=ACCESS_TOKEN

参数 | 是否必须 | 说明
:---- | :---- | :----
touser | 是| 接收者（用户）的 openid
template_id | 是 | 所需下发的模板消息的id
page | 否 | 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
form_id | 是 | 表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的 prepay_id
data | 是 | 模板内容，不填则下发空模板
emphasis_keyword | 否 | 模板需要放大的关键词，不填则默认无放大

请求示例：
```json
{
  "touser": "OPENID",
  "template_id": "TEMPLATE_ID",
  "page": "index",
  "form_id": "FORMID",
  "data": {
      "keyword1": {
          "value": "339208499"
      },
      "keyword2": {
          "value": "2015年01月05日 12:30"
      },
      "keyword3": {
          "value": "粤海喜来登酒店"
      } ,
      "keyword4": {
          "value": "广州市天河区天河路208号"
      }
  },
  "emphasis_keyword": "keyword1.DATA"
}
```

## 代码实现
> 注意：下面的代码均为测试代码，未考虑严谨性，仅为实现功能。

#### 小程序端
```js
<!--index.wxml-->
<view class="container">
  <view class="userinfo">
    <button wx:if="{{!hasUserInfo || !hasOpenId}}"
            open-type="getUserInfo"
            bindgetuserinfo="getUserInfo"
            type='primary'
            size='mini'>获取用户信息</button>
    <block wx:else>
      <image class="userinfo-avatar" src="{{userInfo.avatarUrl}}" mode="cover"></image>
      <text class="userinfo-nickname">{{userInfo.nickName}}</text>
      <text class="userinfo-nickname">{{openId}}</text>
    </block>
  </view>
  <view wx:if="{{hasUserInfo && hasOpenId}}" class='usermotto'>
    <form bindsubmit="templateSend" report-submit="true">
      <button type='primary' formType="submit" size='mini'>发送模板消息</button>
    </form>
  </view>
  <view wx:if="{{logMessage}}">
    <p style="color:red"><span>{{logMessage}}</span></p>
  </view>
</view>
```

需要注意的是，这里的表单需要加上`report-submit="true"`属性，标识该属性表示可以获得一次`formId`的机会，该formId可以用来推送模板消息，下面是控制器相关的代码：

```js
//index.js
//获取应用实例
const app = getApp();
const requestHost = "https://wuwz.guyubao.com/wx_small_app";

Page({
  data: {
    userInfo: {},
    openId: null,
    hasUserInfo: false,
    hasOpenId: false,
    logMessage: null
  },
  getUserInfo: function(e) {
    app.globalData.userInfo = e.detail.userInfo
    this.setData({
      userInfo: e.detail.userInfo,
      hasUserInfo: true,
      logMessage: '加载用户信息中..'
    })
    this.getOpenId();
  },
  getOpenId: function() {
    var _this = this;
    wx.login({
      success: function(res) {
        if (res.code) {
          // 换取openid
          wx.request({
            url: requestHost + "/get_openid_by_js_code",
            data: {
              js_code: res.code
            },
            method: 'GET',
            success: function(res) {
              if (res.data.openid) {
                _this.setData({
                  openId: res.data.openid,
                  hasOpenId: true,
                  logMessage: '加载用户信息完成'
                });
              }
            },
            fail: function (err) {
              _this.setData({
                logMessage: '[fail]' + JSON.stringify(err)
              });
            }
          });
        }
      }
    })
  },
  templateSend: function(e) {
    var _this = this;
    var openId = _this.data.openId;
    // 表单需设置report-submit="true"
    var formId = e.detail.formId;

    if (!formId || 'the formId is a mock one' === formId) {
      _this.setData({
        logMessage: '[fail]请使用真机调试，否则获取不到formId'
      });
      return;
    }

    // 发送随机模板消息
    wx.request({
      url: requestHost + "/template_send",
      data: {
        openId: openId,
        formId: formId
      },
      method: 'POST',
      success: function(res) {
        if (res.data.status === 0) {
          _this.setData({
            logMessage: '发送模板消息成功[' + new Date().getTime()+']'
          });
        }
      },
      fail: function(err) {
        _this.setData({
          logMessage: '[fail]' + JSON.stringify(err)
        });
      }
    });
  }
})
```

### 后端接口
先针对需要使用的微信API做一个简单的封装：

```java
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
```

对外开放相关的接口：
```java
package com.wuwenze.wechatsmallapptmplmsg.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wuwenze.wechatsmallapptmplmsg.util.MapUtil;
import com.wuwenze.wechatsmallapptmplmsg.wechat.WechatApi;
import com.wuwenze.wechatsmallapptmplmsg.wechat.WechatConf;
import com.wuwenze.wechatsmallapptmplmsg.util.SecurityUtil;
import com.wuwenze.wechatsmallapptmplmsg.util.WebUtil;
import com.wuwenze.wechatsmallapptmplmsg.wechat.WechatTemplate;
import com.wuwenze.wechatsmallapptmplmsg.wechat.WechatTemplateItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author wwz
 * @version 1 (2018/8/16)
 * @since Java7
 */
@Slf4j
@RestController
@RequestMapping("/wx_small_app")
public class WechatController {

    @GetMapping("/get_openid_by_js_code")
    public Map<String, Object> getOpenIdByJSCode(String js_code) {
        return WechatApi.getOpenIdByJSCode(js_code);
    }

    @PostMapping("/template_send")
    public Map<String, Object> templateSend() {
        String accessToken = WechatApi.getAccessToken();
        JSONObject body = JSON.parseObject(WebUtil.getBody());

        // 填充模板数据 （测试代码，写死）
        WechatTemplate wechatTemplate = new WechatTemplate()
                .setTouser(body.getString("openId"))
                .setTemplate_id(WechatConf.templateId)
                // 表单提交场景下为formid，支付场景下为prepay_id
                .setForm_id(body.getString("formId"))
                // 跳转页面
                .setPage("index")
                /**
                 * 模板内容填充：随机字符
                 * 购买地点 {{keyword1.DATA}}
                 * 购买时间 {{keyword2.DATA}}
                 * 物品名称 {{keyword3.DATA}}
                 * 交易单号 {{keyword4.DATA}}
                 * -> {"keyword1": {"value":"xxx"}, "keyword2": ...}
                 */
                .setData(MapUtil.newHashMap(//
                        "keyword1", new WechatTemplateItem(RandomUtil.randomString(10)),//
                        "keyword2", new WechatTemplateItem(DateUtil.now()),//
                        "keyword3", new WechatTemplateItem(RandomUtil.randomString(10)),//
                        "keyword4", new WechatTemplateItem(RandomUtil.randomNumbers(10)) //
                ));
        WechatApi.templateSend(accessToken, wechatTemplate);
        return MapUtil.newHashMap("status", 0);
    }

    @GetMapping("/validate")
    public void validate(String signature, String timestamp, String nonce, String echostr) {
        final StringBuilder attrs = new StringBuilder();
        Stream.of(WechatConf.token, timestamp, nonce)//
                .sorted()//
                .forEach((item) -> attrs.append(item));
        String sha1 = SecurityUtil.getSha1(attrs.toString());
        if (StrUtil.equalsIgnoreCase(sha1, signature)) {
            WebUtil.write(echostr);
            return;
        }
        log.error("#0820 WechatController.validate() error, attrs = {}", attrs);
    }
}
```

## 最终效果
### 小程序界面
![](http://cdn.cloudly.cn/blog_img_228df2619d0b4d57882744644fbfde28.png!blog)
### 收到的模板消息
![](http://cdn.cloudly.cn/blog_img_2b7a8bdb024a4eb49fe28c9f971aab31.png!blog)

## 其他：突破发送模板消息的限制
> 如非必要，尽量不要这样做，一旦发现小程序滥用模板消息，微信是有权进行封禁的。

简单来说，我们可以将小程序的表单组件进行封装，伪装小程序中其他功能按钮。当用户点击按钮时，表单组件就自动把formId上传给服务器保存（7天后过期），当收集到一定的用户点击事件后，就可以拿来使用了（主动消息推送群发），哈哈哈。
