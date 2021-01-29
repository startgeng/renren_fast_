/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录表单
 * kevin 2021-01-27 微信登陆学习
 * @author Mark sunlightcs@gmail.com
 */
@Data
@ApiModel(value = "微信登录表单")
public class WxLoginForm {

    @ApiModelProperty(value = "临时登陆凭证")
    @NotBlank(message="临时登陆凭证")
    private String code;

    @ApiModelProperty(value = "昵称")
    @NotBlank(message="昵称不能为空")
    private String nickName;

    @ApiModelProperty(value = "头像url")
    @NotBlank(message = "头像url")
    private String photo;
}