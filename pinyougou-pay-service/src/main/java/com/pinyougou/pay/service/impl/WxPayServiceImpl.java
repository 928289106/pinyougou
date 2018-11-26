package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.WxPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class WxPayServiceImpl implements WxPayService {

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    /**
     *
     * @param out_trade_no 订单号
     * @param tatol_fee    金额
     * @return
     */
    //请求微信接口，返回支付url
    @Override
    public Map createNative(String out_trade_no, String tatol_fee) {

        try {
            Map<String,String> resultMap = new HashMap<>();
            //1.组装参数map,设置参数
            Map<String,String> paramMap = new HashMap<>();

            paramMap.put("appid",appid);
            paramMap.put("mch_id",partner);
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("body","品优购");
            paramMap.put("out_trade_no",out_trade_no);
            paramMap.put("total_fee",tatol_fee);
            paramMap.put("spbill_create_ip","127.0.0.1");
            paramMap.put("notify_url","http://a31ef7db.ngrok.io/WeChatPay/WeChatPayNotify");
            paramMap.put("trade_type","NATIVE");

            String xmlParam = WXPayUtil.generateSignedXml(paramMap,partnerkey);

            //2.创建httpclient
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);

            //3.调用下单api接口
            client.post();   //发送请求
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> map = WXPayUtil.xmlToMap(result);

            resultMap.put("out_trade_no",out_trade_no);
            resultMap.put("tatol_fee",tatol_fee);
            resultMap.put("code_url",map.get("code_url"));

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //查询支付结果
    @Override
    public Map queryStatus(String out_trade_no) {

        try {
            Map<String,String> resultMap = new HashMap<>();
            //1.组装参数map,设置参数
            Map<String,String> paramMap = new HashMap<>();

            paramMap.put("appid",appid);
            paramMap.put("mch_id",partner);
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("out_trade_no",out_trade_no);

            //签名不用写，下面方法会自动生成签名
            String xmlParam = WXPayUtil.generateSignedXml(paramMap,partnerkey);

            //2.创建httpclient
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setHttps(true);
            client.setXmlParam(xmlParam);

            //3.调用下单api接口
            client.post();   //发送请求
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> map = WXPayUtil.xmlToMap(result);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
