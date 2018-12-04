package com.wuwenze.wechatsmallapptmplmsg.wechat;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author wwz
 * @version 1 (2018/8/20)
 * @since Java7
 */
@Data
@Accessors(chain = true)
public class WechatTemplate {

    private String touser;
    private String template_id;
    private String page; // 跳转小程序页面
    private String form_id; //表单提交场景下为formid，支付场景下为prepay_id
    private Map<String, WechatTemplateItem> data;
    private String emphasis_keyword; // 需要放大的关键字，如：keyword1.DATA
}
