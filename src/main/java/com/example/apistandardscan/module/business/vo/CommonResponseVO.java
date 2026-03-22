package com.example.apistandardscan.module.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通用接口返回结果
 */
@Data
@Schema(description = "通用接口返回结果")
public class CommonResponseVO<T> {

    @Schema(description = "响应码", example = "200", defaultValue = "200")
    private Integer code;

    @Schema(description = "响应信息", example = "操作成功", defaultValue = "操作成功")
    private String message;

    @Schema(description = "响应数据", example = "", defaultValue = "")
    private T data;

    // 静态构造方法
    public static <T> CommonResponseVO<T> success(T data) {
        CommonResponseVO<T> response = new CommonResponseVO<>();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    public static <T> CommonResponseVO<T> fail(Integer code, String message) {
        CommonResponseVO<T> response = new CommonResponseVO<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}