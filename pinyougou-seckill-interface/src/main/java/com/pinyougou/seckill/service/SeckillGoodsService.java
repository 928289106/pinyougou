package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    public List<TbSeckillGoods> findList();

    public TbSeckillGoods findOne(Long id);

}
