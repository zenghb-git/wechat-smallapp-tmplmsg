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
                //.setPage("pages/index")
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
