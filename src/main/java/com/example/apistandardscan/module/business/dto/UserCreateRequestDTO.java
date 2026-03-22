package com.example.apistandardscan.module.business.dto;

import com.example.apistandardscan.module.business.model.TestModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 请求体DTO示例：满足@Schema + jakarta.validation校验要求
 */
@Data
@Schema(description = "用户创建请求参数")
public class UserCreateRequestDTO {

    @Schema(description = "用户名", example = "zhangsan", defaultValue = "zhangsan", required = true)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20之间")
    private String username;

    @Schema(description = "用户年龄", example = "25", defaultValue = "18", required = true)
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄不能小于1")
    @Max(value = 120, message = "年龄不能大于120")
    private Integer age;

    @Schema(description = "用户邮箱", example = "zhangsan@example.com", defaultValue = "")
    @Email(message = "邮箱格式不正确")
    private String email;

//    @Schema(description = "手机号", example = "13800138000", defaultValue = "")
//    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Schema(description = "测试模型", defaultValue = "")
    @NotNull(message = "测试模型不能为空")
    private TestModel testModel;
}