package com.xiake.service;

import com.alibaba.fastjson.JSONObject;

public interface JiTangMsgService {
    String getFortune(String constellation);
    String[] getMsg();
    JSONObject getWeather();
    void push();
    int pushBySelf(String openid,String constellation);
    String goodNightMsg();
    void pushGoodNight(String msg);
}
