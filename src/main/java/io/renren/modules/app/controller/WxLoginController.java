package io.renren.modules.app.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.wxpay.sdk.MyWXPayConfig;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import io.renren.common.utils.R;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.app.annotation.Login;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.form.PayOrderForm;
import io.renren.modules.app.form.WxLoginForm;
import io.renren.modules.app.service.OrderService;
import io.renren.modules.app.service.UserService;
import io.renren.modules.app.utils.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Date;
import java.util.*;

/**
 * 微信小程序登陆的 web层
 * @author kevin
 * @version 1.0
 * @date 2021-01-27 13:21
 */
@Api("微信登陆")
@RestController
@RequestMapping("/app/wx")
@Slf4j
public class WxLoginController {

    @Value("${application.app-id}")
    private String appId;

    @Value("${application.app-secret}")
    private String appSecret;

    @Value(("${application.key}"))
    private String key;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MyWXPayConfig myWXPayConfig;

    @PostMapping("login")
    @ApiOperation("登陆")
    public R login(@RequestBody WxLoginForm form){
        ValidatorUtils.validateEntity(form);
        //微信支付接口
        String url = "https://api.weixin.qq.com/sns/jscode2session";
//        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap<String,Object> map = new HashMap<>();
        //封装appid appsecret grant_type js_code
        map.put("appid",appId);
        map.put("secret",appSecret);
        map.put("js_code",form.getCode());
        map.put("grant_type","authorization_code");
        String response = HttpUtil.post(url, map);
        JSONObject json = JSONUtil.parseObj(response);
        String openid = json.getStr("openid");
        if (StringUtils.isBlank(openid)){
            return R.error("临时登陆凭证错误");
        }
        UserEntity user = new UserEntity();
        user.setOpenId(openid);
        QueryWrapper queryWrapper = new QueryWrapper(user);
        int count = userService.count(queryWrapper);
        if (count == 0){
            user.setNickname(form.getNickName());
            user.setPhoto(form.getPhoto());
            user.setType(2);
            user.setCreateTime(new Date());
            userService.save(user);
        }
        user = new UserEntity();
        user.setOpenId(openid);
        queryWrapper = new QueryWrapper(user);
        UserEntity entity = userService.getOne(queryWrapper);
        Long id = entity.getUserId();
        String token = jwtUtils.generateToken(id);
        Map<String,Object> result = new HashMap<>();
        result.put("token",token);
        result.put("expire",jwtUtils.getExpire());
        log.info("openId->{}",openid);
        return R.ok(result);
    }


    /**
     * 小程序付款接口
     * @param form
     * @param header
     * @return
     */
    @Login
    @PostMapping("/microAppPayOrder")
    @ApiOperation("小程序付款")
    public R microAppPayOrder(@RequestBody PayOrderForm form, @RequestHeader HashMap header){
        ValidatorUtils.validateEntity(form);
        String token = (String) header.get("token");
        Long userId = Long.parseLong(jwtUtils.getClaimByToken(token).getSubject());
        @Min(1) Integer orderId = form.getOrderId();
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        QueryWrapper wrapper = new QueryWrapper(user);
        int count = userService.count(wrapper);
        if (count == 0){
            return R.error("用户不存在");
        }
        //获取openid
        String openId = userService.getOne(wrapper).getOpenId();
        OrderEntity order = new OrderEntity();
        order.setId(orderId);
        order.setUserId(userId.intValue());
        wrapper = new QueryWrapper(order);
        count = orderService.count(wrapper);
        if (count == 0){
            return R.error("不是有效的订单");
        }
        //获取到订单实体
        OrderEntity orderOne = orderService.getOne(wrapper);

        //返回一个订单数据
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId.intValue());
        wrapper = new QueryWrapper();
        OrderEntity entity = orderService.getOne(wrapper);

        //向微信平台发出请求  微信数据库里面存的是分,但是发送给微信平台的是元
        String amount = orderOne.getAmount().multiply(new BigDecimal("100")).intValue()+"";
        try {
            WXPay wxPay = new WXPay(myWXPayConfig);
            HashMap map = new HashMap();
            map.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
            map.put("body", "订单备注");
            map.put("out_trade_no", entity.getCode()); //商品订单流水号
            map.put("total_fee", amount); //订单金额
            map.put("spbill_create_ip", "127.0.0.1"); //客户端IP
            map.put("notify_url", "http://66931e50.nat1.nsloop.com/renren-fast/app/wx/recieveMessage"); //通知回调地址
            map.put("trade_type", "JSAPI");  //调用接口类型
            map.put("openid", openId); //用户授权
            Map<String, String> result = wxPay.unifiedOrder(map); //创建支付订单
            //微信支付id
            String prepayId = (String) result.get("prepay_id");
            log.info("prepareId->{}",prepayId);
            if (StringUtils.isNotBlank(prepayId)){
                OrderEntity updateOrder = new OrderEntity();
                updateOrder.setId(entity.getId());
                updateOrder.setPrepayId(prepayId);
                UpdateWrapper updateWrapper = new UpdateWrapper();
                orderService.update(updateOrder,updateWrapper);
                //返回给小程序端
                map.clear();
                map.put("appId",appId);
                String timeStamp = new Date().getTime()+"";
                map.put("timeStamp",timeStamp);
                String nonceStr = WXPayUtil.generateNonceStr();
                //微信工具生成的随机数
                map.put("nonceStr",nonceStr);
                //返回的微信订单id
                map.put("package","prepay_id="+prepayId);
                map.put("signType","MD5");
                //将参数加密起来,以防止网络传输的时候数据丢失
                String paySign = WXPayUtil.generateSignature(map, key);
                //返回的参数有三个   一个随机数  一个订单号   一个加密的东西
                return R.ok().put("package","prepay_id="+prepayId)
                        .put("timeStamp",timeStamp)
                        .put("nonceStr",nonceStr)
                        .put("paySign",paySign);
            }else {
                return R.error("支付订单创建失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("小程序支付模块错误");
        }
    }

    //支付信息回调
    @PostMapping("/recieveMessage")
    public void recieveMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setCharacterEncoding("utf-8");
        Reader reader = request.getReader();
        BufferedReader buffer = new BufferedReader(reader);
        String line = buffer.readLine();
        StringBuffer temp = new StringBuffer();
        while (line != null) {
            temp.append(line);
            line = buffer.readLine();
        }
        buffer.close();
        reader.close();
         Map<String, String> map = WXPayUtil.xmlToMap(temp.toString());
        String resultCode = map.get("result_code");
        String returnCode = map.get("return_code");
        if ("SUCCESS".equalsIgnoreCase(resultCode) && "SUCCESS".equalsIgnoreCase(returnCode)){
            //商户收到通知,开始校验   流水号
            String outTradeNo = map.get("out_trade_no");
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("code",outTradeNo);
            updateWrapper.set("status",2);
            orderService.update(updateWrapper);
            response.setContentType("application/xml");
            response.setCharacterEncoding("utf-8");
            Writer writer = response.getWriter();
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            //写回成功信息给商户平台
            bufferedWriter.write("<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>");
            bufferedWriter.close();
            writer.close();
        }
    }
}
