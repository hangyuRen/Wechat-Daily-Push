package com.xiake.task;


import com.xiake.service.JiTangMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PushTask {
    @Autowired
    private JiTangMsgService jiTangMsgService;

    @Scheduled(cron = "${wx.mp.cron}")
    @Async
    public void autoPush() {
        try {
            jiTangMsgService.push();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "${wx.mp.nightCron}")
    @Async
    public void pushGoodNight() {
        try {
            String nightMsg = jiTangMsgService.goodNightMsg();
            jiTangMsgService.pushGoodNight(nightMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
