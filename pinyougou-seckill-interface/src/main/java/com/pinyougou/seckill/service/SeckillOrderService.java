package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillOrder;

public interface SeckillOrderService {

    /**
     *
     * @param id       下单商品id
     * @param userId   用户id
     */
    public void addOrder(Long id,String userId);

    //判断是否生产订单
    public TbSeckillOrder getFromRedis(String userId);

    //判断是否在排队
    public Boolean isFlag(String userId);
}
