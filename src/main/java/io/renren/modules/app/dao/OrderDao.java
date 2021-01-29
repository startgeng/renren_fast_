/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.app.entity.OrderEntity;
import io.renren.modules.app.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * 用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    /**
     * 查询订单列表
     * @param map
     * @return
     */
    LinkedList<OrderEntity> searchUserOrderList(HashMap map);
}
