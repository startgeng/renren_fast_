package io.renren.modules.app.controller;

import io.renren.common.utils.R;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.app.annotation.Login;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.form.UserOrderForm;
import io.renren.modules.app.service.OrderService;
import io.renren.modules.app.utils.JwtUtils;
import io.swagger.annotations.Api;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * 订单的控制器
 * @author kevin
 * @version 1.0
 * @date 2021-01-27 18:15
 */
@RestController
@RequestMapping("/app/order")
@Api("订单业务接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取订单列表
     * @param userOrderForm
     * @param header
     * @return
     */
    @Login
    @PostMapping("/searchUserOrderList")
    public R searchUserOrderList(@RequestBody UserOrderForm userOrderForm, @RequestHeader HashMap header){
        ValidatorUtils.validateEntity(userOrderForm);
        String token = (String) header.get("token");
        int userId = Integer.parseInt(jwtUtils.getClaimByToken(token).getSubject());
        @Min(1) Integer page = userOrderForm.getPage();
        @Range(min = 1, max = 50) Integer length = userOrderForm.getLength();
        Integer start = (page - 1) * length;
        HashMap map = new HashMap();
        map.put("userId",userId);
        map.put("start",start);
        map.put("length",length);
        LinkedList<OrderEntity> list = orderService.searchUserOrderList(map);
        return R.ok().put("list",list);
    }
}
