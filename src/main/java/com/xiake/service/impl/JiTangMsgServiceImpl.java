package com.xiake.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiake.domain.Information;
import com.xiake.domain.Users;
import com.xiake.service.JiTangMsgService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@SuppressWarnings("all")
public class JiTangMsgServiceImpl implements JiTangMsgService {
    @Value("${tianx.APIKEY}")
    private String apiKey;
    @Value("${wx.mp.city}")
    private String city;
    @Value("${wx.mp.appId}")
    private String appId;
    @Value("${wx.mp.secret}")
    private String secret;
    @Value("${wx.mp.templateId}")
    private String templateId;
    @Value("${url.fortune}")
    private String forturnUrl;
    @Value("${url.msg}")
    private String msgUrl;
    @Value("${url.weather}")
    private String weatherUrl;
    @Value("${content.type}")
    private String contentType;
    @Value("${wx.mp.type}")
    private String weatherType;
    @Autowired
    private Users users;
    @Value("${url.goodNight}")
    private String nightUrl;
    @Value("${wx.mp.templateIdForNight}")
    private String templateIdForNight;
    public String getFortune(String constellation) {
        String tianapi_data = "";
        try {
            URL url = new URL(forturnUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", contentType);
            OutputStream outputStream = conn.getOutputStream();
            String content = "key=" + apiKey + "&astro=" + constellation;
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder tianapi = new StringBuilder();
            String read = null;
            while ((read = bufferedReader.readLine()) != null) {
                tianapi.append(read);
                tianapi.append("\r\n");
            }
            tianapi_data = tianapi.toString();
            JSONObject jsonObject = JSONObject.parseObject(tianapi_data);
            JSONArray newList = jsonObject.getJSONArray("newslist");
            String result = newList.getJSONObject(8).getString("content");
            inputStream.close();
            log.info(result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String[] getMsg() {
        String[] result = new String[2];
        String tianapi_data = "";
        try {
            URL url = new URL( msgUrl);
            HttpURLConnection  conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", contentType);
            OutputStream outputStream = conn.getOutputStream();
            String content = "key=" + apiKey;
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder tianapi = new StringBuilder();
            String read = null;
            while ((read = bufferedReader.readLine()) != null) {
                tianapi.append(read);
                tianapi.append("\r\n");
            }
            tianapi_data = tianapi.toString();
            JSONObject jsonObject = JSON.parseObject(tianapi_data);
            log.info(jsonObject.toString());
            JSONArray newlist = jsonObject.getJSONArray("newslist");
            String content1 = newlist.getJSONObject(0).getString("content");
            String note = newlist.getJSONObject(0).getString("note");
            result[0] = content1;
            result[1] = note;
            inputStream.close();
            log.info("en={},ch={}",content1,note);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONObject getWeather() {
        String tianapi_data = "";
        try {
            URL url = new URL( weatherUrl);
            HttpURLConnection  conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", contentType);
            OutputStream outputStream = conn.getOutputStream();
            String content = "key=" + apiKey + "&city=" + city + "&type=" + weatherType;
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder tianapi = new StringBuilder();
            String read = null;
            while ((read = bufferedReader.readLine()) != null) {
                tianapi.append(read);
                tianapi.append("\r\n");
            }
            tianapi_data = tianapi.toString();
            JSONObject jsonObject = JSONObject.parseObject(tianapi_data);
            JSONArray newlist = jsonObject.getJSONArray("newslist");
            JSONObject jsonObject1 = newlist.getJSONObject(0);
            inputStream.close();
            return jsonObject1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void push() {
        List<Information> list = users.getList();
        if (list.size() == 0) {
            throw new RuntimeException("推送用户为空");
        }
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret)) {
            log.info("微信配置信息，appid:{},secret:{}", appId, secret);
            throw new RuntimeException("微信配置错误，请检查");
        }

        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(appId);
        wxMpConfigStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage);

        JSONObject weather = getWeather();

        try {
            for(Information info : list) {
                String[] msg = getMsg();
                String constellationMsg = getFortune(info.getConstellation());

                WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(info.getOpenid()).templateId(templateId).build();

                templateMessage.addData(new WxMpTemplateData("first", LocalDate.now().toString(), "#0030EE"));
                templateMessage.addData(new WxMpTemplateData("city", weather.getString("area"), "#00FF00"));
                templateMessage.addData(new WxMpTemplateData("weather", weather.getString("weather"), "#0099FF"));
                templateMessage.addData(new WxMpTemplateData("direct", weather.getString("wind"), "#912CEE"));
                templateMessage.addData(new WxMpTemplateData("windsc", weather.getString("windsc"), "#00FFFF"));

                templateMessage.addData(new WxMpTemplateData("lowTemperature", weather.getString("lowest"), "#E6421A"));
                templateMessage.addData(new WxMpTemplateData("highTemperature", weather.getString("highest"), "#E6421A"));
                templateMessage.addData(new WxMpTemplateData("humidity", weather.getString("humidity") + "%", "#3333CC"));

                templateMessage.addData(new WxMpTemplateData("luck",constellationMsg == null ? "" : constellationMsg , "#FFC0CB"));

                templateMessage.addData(new WxMpTemplateData("en", msg[0] == null ? "" : msg[0], "#FFC0CB"));
                templateMessage.addData(new WxMpTemplateData("ch", msg[1] == null ? "" : msg[1], "#FFC0CB"));
                log.info("发送模板消息，模板id:{},消息内容:{}", templateId, templateMessage.toJson());
                wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            }
        } catch (Exception e) {
            log.error("推送失败", e);
        }
    }

    @Override
    public int pushBySelf(String openid,String constellation) {
        if (StringUtils.isEmpty(openid)) {
            throw new RuntimeException("推送用户为空");
        }
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret)) {
            log.info("微信配置信息，appid:{},secret:{}", appId, secret);
            throw new RuntimeException("微信配置错误，请检查");
        }

        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(appId);
        wxMpConfigStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage);

        try {
            String[] msg = getMsg();
            String constellationMsg = getFortune(constellation);
            JSONObject weather = getWeather();

            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(openid).templateId(templateId).build();

            templateMessage.addData(new WxMpTemplateData("first", LocalDate.now().toString(), "#0030EE"));
            templateMessage.addData(new WxMpTemplateData("city", weather.getString("area"), "#00FF00"));
            templateMessage.addData(new WxMpTemplateData("weather", weather.getString("weather"), "#0099FF"));
            templateMessage.addData(new WxMpTemplateData("direct", weather.getString("wind"), "#912CEE"));
            templateMessage.addData(new WxMpTemplateData("windsc", weather.getString("windsc"), "#00FFFF"));

            templateMessage.addData(new WxMpTemplateData("lowTemperature", weather.getString("lowest"), "#E6421A"));
            templateMessage.addData(new WxMpTemplateData("highTemperature", weather.getString("highest"), "#E6421A"));
            templateMessage.addData(new WxMpTemplateData("humidity", weather.getString("humidity") + "%", "#3333CC"));

            templateMessage.addData(new WxMpTemplateData("luck",constellationMsg == null ? "" : constellationMsg , "#FFC0CB"));

            templateMessage.addData(new WxMpTemplateData("en", msg[0] == null ? "" : msg[0], "#FFC0CB"));
            templateMessage.addData(new WxMpTemplateData("ch", msg[1] == null ? "" : msg[1], "#FFC0CB"));
            log.info("发送模板消息，模板id:{},消息内容:{}", templateId, templateMessage.toJson());
            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            return 1;
        } catch (Exception e) {
            log.error("推送失败", e);
        }
        return 0;
    }

    @Override
    public String goodNightMsg() {
        String tianapi_data = "";
        try {
            URL url = new URL(nightUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", contentType);
            OutputStream outputStream = conn.getOutputStream();
            String content = "key=" + apiKey;
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder tianapi = new StringBuilder();
            String read = null;
            while ((read = bufferedReader.readLine()) != null) {
                tianapi.append(read);
                tianapi.append("\r\n");
            }
            tianapi_data = tianapi.toString();
            JSONObject jsonObject = JSONObject.parseObject(tianapi_data);
            JSONObject result1 = jsonObject.getJSONObject("result");
            String result1String = result1.getString("content");
            inputStream.close();
            log.info(result1String);
            return result1String;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void pushGoodNight(String msg) {
        List<Information> list = users.getList();
        if (list.size() == 0) {
            throw new RuntimeException("推送用户为空");
        }
        if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(secret)) {
            log.info("微信配置信息，appid:{},secret:{}", appId, secret);
            throw new RuntimeException("微信配置错误，请检查");
        }

        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(appId);
        wxMpConfigStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxMpConfigStorage);

        try {
            for(Information info : list) {
                String nightMsg = goodNightMsg();
                log.info("nightMsg:{}",nightMsg);

                WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(info.getOpenid()).templateId(templateIdForNight).build();

                templateMessage.addData(new WxMpTemplateData("nightMsg", nightMsg == null ? "" : nightMsg, "#FFC0CB"));

                log.info("发送模板消息，模板id:{},消息内容:{}", templateId, templateMessage.toJson());
                wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            }
        } catch (Exception e) {
            log.error("推送失败", e);
        }
    }
}
