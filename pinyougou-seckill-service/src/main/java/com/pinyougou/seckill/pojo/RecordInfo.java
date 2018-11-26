package com.pinyougou.seckill.pojo;

import java.io.Serializable;

public class RecordInfo implements Serializable {

    private String userId;  //用户id
    private Long id;   //秒杀商品id

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
