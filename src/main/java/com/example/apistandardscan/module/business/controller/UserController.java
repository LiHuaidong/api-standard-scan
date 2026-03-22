package com.example.apistandardscan.module.business.controller;

import com.example.apistandardscan.module.business.dto.UserCreateRequestDTO;
import com.example.apistandardscan.module.business.vo.CommonResponseVO;
import com.example.apistandardscan.module.business.vo.UserResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller示例：满足@Tag、@Operation、多@ApiResponse要求
 */
@RestController
@RequestMapping("/api/user")
//@Tag(name = "用户管理接口", description = "用户的创建、查询、修改、删除等操作接口")
public class UserController {

    /**
     * 创建用户接口：满足所有注解规范
     */
    @PostMapping("/create")
    @Operation(
            summary = "创建用户",
            description = "根据用户名、年龄、邮箱等信息创建新用户，用户名不能为空且长度2-20，年龄1-120"
    )
    // 200响应
    @ApiResponse(
            responseCode = "200",
            description = "创建成功，返回用户信息",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponseVO.class)
            )
    )
    // 400响应
    @ApiResponse(
            responseCode = "400",
            description = "请求参数错误（用户名空/年龄超限/邮箱格式错误等）",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponseVO.class)
            )
    )
    // 404响应
    @ApiResponse(
            responseCode = "404",
            description = "关联资源不存在（如角色ID不存在）",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponseVO.class)
            )
    )
    // 500响应
    @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误（数据库操作失败等）",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CommonResponseVO.class)
            )
    )
    public CommonResponseVO<UserResponseVO> createUser(@Valid @RequestBody UserCreateRequestDTO requestDTO) {
        // 模拟业务逻辑
        UserResponseVO vo = new UserResponseVO();
        vo.setId(1001L);
        vo.setUsername(requestDTO.getUsername());
        vo.setAge(requestDTO.getAge());
        vo.setCreateTime(LocalDateTime.now());
        vo.setEnabled(true);
        return CommonResponseVO.success(vo);
    }

    @GetMapping("/test/param/{userId}")
    @Operation(
            summary = "测试参数注解",
            description = "测试PathVariable和基本类型RequestParam的Swagger注解",
            parameters = {
                    @Parameter(name = "status", description = "用户状态", required = true, example = "1", schema = @Schema(defaultValue = "1"))
            }
    )
    @ApiResponse(responseCode = "200", description = "成功", content = @Content(schema = @Schema(implementation = CommonResponseVO.class)))
    @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(schema = @Schema(implementation = CommonResponseVO.class)))
    @ApiResponse(responseCode = "404", description = "不存在", content = @Content(schema = @Schema(implementation = CommonResponseVO.class)))
    @ApiResponse(responseCode = "500", description = "服务器错误", content = @Content(schema = @Schema(implementation = CommonResponseVO.class)))
    public CommonResponseVO<String> testParam(
            @Parameter(name = "userId", description = "用户ID", required = true, example = "1001", schema = @Schema(defaultValue = "1001"))
            @PathVariable("userId")
            @NotBlank(message = "用户ID不能为空")
            String userId,

            @RequestParam(required = true, defaultValue = "10")
            @Min(value = 1, message = "页大小≥1")
            int pageSize,

            @RequestParam(required = true)
            @NotNull(message = "状态不能为空")
            @Min(value = 0, message = "状态0/1")
            Integer status,

            @RequestParam(required = false)
            Boolean enabled
    ) {
        return CommonResponseVO.success("参数校验通过");
    }
}