package com.example.apistandardscan.module.business.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 请求体DTO 引用其他实体类，同样也要增加校验
 */
@Data
@Schema(description = "测试模型")
public class TestModel {
}
