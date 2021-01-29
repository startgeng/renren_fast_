package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.dao.OrderDao;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * 订单实现类
 * @author kevin
 * @version 1.0
 * @date 2021-01-27 18:09
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private OrderDao orderDao;

    /**
     *  查询订单列表
     * @param map
     * @return
     */
    @Override
    public LinkedList<OrderEntity> searchUserOrderList(HashMap map) {
        return orderDao.searchUserOrderList(map);
    }
}
