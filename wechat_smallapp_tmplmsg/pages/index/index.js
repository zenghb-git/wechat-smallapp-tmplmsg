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
        } else {
          _this.setData({
            logMessage: '[fail]' + JSON.stringify(res)
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