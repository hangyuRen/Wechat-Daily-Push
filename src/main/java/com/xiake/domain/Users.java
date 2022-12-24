package com.xiake.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "info")
public class Users {
    private List<Information> list;
    public  List<Information> getList() {
        return list;
    }

    public void setList(List<Information> list) {
        this.list = list;
    }
}
