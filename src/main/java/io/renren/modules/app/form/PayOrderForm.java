package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 查询订单
 * @author kevin
 * @version 1.0
 * @date 2021-01-28 10:09
 */
@Data
@ApiModel(value = "订单付款表单")
public class PayOrderForm {

    @ApiModelProperty(value = "订单id")
    @Min(1)
    private Integer orderId;
}
