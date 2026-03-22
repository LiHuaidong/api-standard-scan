package com.example.apistandardscan.module.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 返回体VO示例：满足@Schema含description/example/defaultValue要求
 */
@Data
@Schema(description = "用户信息返回结果")
public class UserResponseVO {

    @Schema(description = "用户ID", example = "1001", defaultValue = "0")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan", defaultValue = "")
    private String username;

    @Schema(description = "用户年龄", example = "25", defaultValue = "18")
    private Integer age;

    @Schema(description = "创建时间", example = "2026-03-21 10:00:00", defaultValue = "")
    private LocalDateTime createTime;

    @Schema(description = "是否启用", example = "true", defaultValue = "true")
    private Boolean enabled;
}