package io.renren.modules.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.app.entity.OrderEntity;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * 订单接口
 * @author kevin
 * @version 1.0
 * @date 2021-01-27 18:08
 */
public interface OrderService extends IService<OrderEntity> {

    LinkedList<OrderEntity> searchUserOrderList(HashMap map);
}
