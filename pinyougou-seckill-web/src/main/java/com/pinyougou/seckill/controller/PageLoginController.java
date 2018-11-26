package com.pinyougou.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageLoginController {

    @RequestMapping("/page/login")
    public String gotoBack(String url){
        return "redirect:"+url;
    }

}
