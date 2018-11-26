package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

    @Reference
    private SeckillGoodsService seckillGoodsService;

    @RequestMapping("/findList")
    public List<TbSeckillGoods> findList(){
        List<TbSeckillGoods> list = seckillGoodsService.findList();
        return list;
    }

    @RequestMapping("/findOne")
    public TbSeckillGoods findOne(Long id){
        TbSeckillGoods seckillGoods = seckillGoodsService.findOne(id);
        return seckillGoods;
    }
}
