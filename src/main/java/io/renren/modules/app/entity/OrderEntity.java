package io.renren.modules.app.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单表
 * @author kevin
 * @version 1.0
 * @date 2021-01-27 17:54
 */
@Data
@TableName("tb_order")
public class OrderEntity implements Serializable {

    @TableId
    private Integer id;

    private String code;

    private Integer userId;

    private BigDecimal amount;

    private Integer paymentType;

    private Integer status;

    private Date createTime;

    /**
     * 订单支付Id
     */
    private String prepayId;
}
