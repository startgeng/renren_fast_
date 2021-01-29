package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 封装类
 * @author kevin
 * @version 1.0
 * @date 2021-01-27 18:12
 */
@Data
@ApiModel
public class UserOrderForm {

    @ApiModelProperty(value = "页数")
    @NotNull(message = "页数不能为空")
    @Min(1)
    private Integer page;

    @ApiModelProperty(value = "每页记录数")
    @NotNull(message = "每页记录数不能为空")
    @Range(min = 1,max = 50)
    private Integer length;
}
