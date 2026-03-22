# api-standard-scan
扫描API的Swagger 注解是否符合规范

# swagger3规
| 规范类别                 | 核心要求                                                                                                                                                                                                                                                                                                                                 |
|--------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 接口类与方法注解规范     | 1. 接口类必须添加`@Tag`注解，且注解中包含`name`（接口组名）和`description`（接口组描述）字段；<br>2. 接口方法必须添加`@Operation`注解，且注解中指定`summary`（接口简短描述）、`description`（接口详细描述）字段；<br>3. 每个接口方法至少添加4个`@ApiResponse`注解（覆盖200/400/404/500响应码），且每个注解需包含`responseCode`（响应码）、`description`（响应说明）、`content`（响应体类型）字段。 |
| 接口参数注解规范         | 1. `@PathVariable`参数：<br>   - 必须添加`@Parameter`注解（可直接标注在参数上，或在`@Operation`的`parameters`中统一声明）；<br>   - `@Parameter`的`name`需与`@PathVariable`的参数名完全一致；<br>   - `@Parameter`的`description`为必填字段，`example`/`defaultValue`建议填写；<br>   - 包装类型参数需添加`@NotNull/@NotBlank`校验注解，基本类型参数禁用`@NotNull`；<br>2. `@RequestParam`参数：<br>   - 必须添加`@Parameter`注解；<br>   - `@Parameter`的`required`属性需与`@RequestParam`的`required`属性保持一致；<br>   - 数值类型参数建议添加`@Min/@Max/@Digits`等范围校验注解；<br>   - 包装类型必填参数强制添加`@NotNull`注解，基本类型参数禁用`@NotNull`注解。 |
| 请求/返回体DTO/VO注解规范 | 1. 类级别必须添加`@Schema`注解，且注解中的`description`字段不能为空；<br>2. 字段级别必须添加`@Schema`注解，其中`description`为必填字段，`example`/`defaultValue`建议填写；<br>3. 请求体DTO的字段需添加`jakarta.validation.constraints`系列校验注解（如`@NotBlank`、`@Min`、`@Email`等）；<br>4. 嵌套类（包括静态内部类、多层嵌套类、集合/数组嵌套类）需遵循与顶级类一致的`@Schema`注解和校验注解规范。 |
# 扫描结果示例
========== 开始校验Swagger3规范 ==========

--- 校验Controller@Tag注解 ---
23:21:12.711 [main] INFO org.reflections.Reflections -- Reflections took 75 ms to scan 1 urls, producing 7 keys and 12 values
扫描到的控制器数量: 1
找到控制器: com.example.apistandardscan.module.business.controller.UserController

--- 校验Controller方法注解 ---
❌ com.example.apistandardscan.module.business.controller.UserController 未添加@Tag注解
23:21:12.759 [main] INFO org.reflections.Reflections -- Reflections took 10 ms to scan 1 urls, producing 7 keys and 12 values
扫描到的控制器数量: 1
找到控制器: com.example.apistandardscan.module.business.controller.UserController
❌ com.example.apistandardscan.module.business.controller.UserController#testParam 参数[1]:pageSize (@RequestParam) 未添加@Parameter注解（参数上或@Operation中）
❌ com.example.apistandardscan.module.business.controller.UserController#testParam 参数[2]:status @Parameter缺少description
⚠️ com.example.apistandardscan.module.business.controller.UserController#testParam 参数[2]:status @Parameter缺少example（建议添加）
⚠️ com.example.apistandardscan.module.business.controller.UserController#testParam 参数[2]:status @Parameter.schema缺少defaultValue（建议添加）
❌ com.example.apistandardscan.module.business.controller.UserController#testParam 参数[3]:enabled (@RequestParam) 未添加@Parameter注解（参数上或@Operation中）

--- 校验DTO/VO注解 ---
23:21:12.932 [main] INFO org.reflections.Reflections -- Reflections took 12 ms to scan 1 urls, producing 3 keys and 16 values
❌ com.example.apistandardscan.ApiStandardScanApplication 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.BASE_PACKAGE 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.BUSINESS_PACKAGE 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.CONTROLLER_PACKAGE 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.DTO_PACKAGE 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.VO_PACKAGE 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.SCANNED_CLASSES 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.NUMBER_VALID_ANNOS 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.BASIC_TYPES 未添加@Schema注解
❌ com.example.apistandardscan.Swagger3SpecCheckTool.REQUIRED_CODES 未添加@Schema注解
❌ com.example.apistandardscan.config.Swagger3Config 未添加@Schema注解
⚠️ com.example.apistandardscan.module.business.vo.CommonResponseVO.data @Schema缺少example（建议添加）
❌ com.example.apistandardscan.module.business.dto.UserCreateRequestDTO.phone 未添加@Schema注解
⚠️ com.example.apistandardscan.module.business.dto.UserCreateRequestDTO.testModel @Schema缺少example（建议添加）
❌ com.example.apistandardscan.module.business.controller.UserController 未添加@Schema注解
========== 校验结束 ==========
