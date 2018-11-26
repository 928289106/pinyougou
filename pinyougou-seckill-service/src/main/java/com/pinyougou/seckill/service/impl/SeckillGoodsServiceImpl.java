package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<TbSeckillGoods> findList() {

        List list = redisTemplate.boundHashOps("seckillGoods").values();
        return list;
    }

    @Override
    public TbSeckillGoods findOne(Long id) {

        return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
    }
}
