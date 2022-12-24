package com.xiake.controller;

import com.xiake.common.result.ResponseApi;
import com.xiake.service.JiTangMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author vic_miao
 * @date 2022年8月23日 10点23分
 */
@RestController
@Slf4j
@SuppressWarnings("all")
public class WxController {
    @Autowired
    private JiTangMsgService jiTangMsgService;

    /**
     * 手动push
     * @param openId
     * @return
     */
    @GetMapping("/wx/push/{openId}/{constellation}")
    public ResponseApi<String> wxPush(@PathVariable("openId") String openId,@PathVariable("constellation") String constellation) {
        int flag = jiTangMsgService.pushBySelf(openId,constellation);
        if(flag == 0) {
            return ResponseApi.error("推送失败");
        }
        return ResponseApi.success("推送成功");
    }

}
