package com.example.apistandardscan;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;

/**
 * Swagger3规范校验工具（与业务代码完全分离）
 * 运行main方法即可扫描项目，输出不符合规范的内容
 */
public class Swagger3SpecCheckTool {

    // 替换为你的项目基础包路径
    private static final String BASE_PACKAGE = "com.example.apistandardscan.module.business";
    public static final String BUSINESS_PACKAGE = "com.example.apistandardscan.module.business";
    private static final String CONTROLLER_PACKAGE = BASE_PACKAGE + ".controller";
    private static final String DTO_PACKAGE = BASE_PACKAGE + ".dto";
    private static final String VO_PACKAGE = BASE_PACKAGE + ".vo";
    // 已扫描过的类（避免循环引用重复扫描）
    private static final Set<Class<?>> SCANNED_CLASSES = new HashSet<>();
    // 数值类型校验注解
    private static final Set<Class<? extends Annotation>> NUMBER_VALID_ANNOS = new HashSet<>(Arrays.asList(
            Min.class, Max.class, Digits.class, Positive.class, Negative.class
    ));
    // 基本类型集合
    private static final Set<Class<?>> BASIC_TYPES = new HashSet<>(Arrays.asList(
            int.class, long.class, short.class, byte.class, float.class, double.class, boolean.class, char.class
    ));
    // 必须包含的响应码
    private static final Set<String> REQUIRED_CODES = new HashSet<>(Arrays.asList("200", "400", "404", "500"));

    public static void main(String[] args) {
        System.out.println("========== 开始校验Swagger3规范 ==========");
        checkControllerTag();       // 校验@Tag注解
        checkControllerMethod();    // 校验@Operation/@ApiResponse
        checkDtoSchemaValidation(); // 校验DTO的@Schema和校验注解
        System.out.println("========== 校验结束 ==========");
    }

    /**
     * 校验Controller类上的@Tag注解（必须有name+description）
     */
    private static void checkControllerTag() {
        System.out.println("\n--- 校验Controller@Tag注解 ---");

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addUrls(ClasspathHelper.forPackage(CONTROLLER_PACKAGE)));

        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);
        System.out.println("扫描到的控制器数量: " + controllers.size());
        for (Class<?> clazz : controllers) {
            System.out.println("找到控制器: " + clazz.getName());
        }

        for (Class<?> clazz : controllers) {
            Tag tag = clazz.getAnnotation(Tag.class);
            if (tag == null) {
                System.err.println("❌ " + clazz.getName() + " 未添加@Tag注解");
                continue;
            }
            if (tag.name().trim().isEmpty()) {
                System.err.println("❌ " + clazz.getName() + " @Tag缺少name字段");
            }
            if (tag.description().trim().isEmpty()) {
                System.err.println("❌ " + clazz.getName() + " @Tag缺少description字段");
            }
        }
    }

    /**
     * 校验Controller方法的@Operation和@ApiResponse
     */
    private static void checkControllerMethod() {
        System.out.println("\n--- 校验Controller方法注解 ---");

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addUrls(ClasspathHelper.forPackage(BUSINESS_PACKAGE)));

        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(RestController.class);
        System.out.println("扫描到的控制器数量: " + controllers.size());
        for (Class<?> clazz : controllers) {
            System.out.println("找到控制器: " + clazz.getName());
        }

        for (Class<?> clazz : controllers) {
            for (Method method : clazz.getDeclaredMethods()) {
                // 校验@Operation
                Operation operation = method.getAnnotation(Operation.class);
                if (operation == null) {
                    System.err.println("❌ " + clazz.getName() + "#" + method.getName() + " 未添加@Operation注解");
                    continue;
                }
                if (operation.summary().trim().isEmpty()) {
                    System.err.println("❌ " + clazz.getName() + "#" + method.getName() + " @Operation缺少summary");
                }
                if (operation.description().trim().isEmpty()) {
                    System.err.println("❌ " + clazz.getName() + "#" + method.getName() + " @Operation缺少description");
                }

                // 校验@ApiResponse
                ApiResponse[] responses = method.getAnnotationsByType(ApiResponse.class);
                if (responses.length < 4) {
                    System.err.println("❌ " + clazz.getName() + "#" + method.getName() + " @ApiResponse数量不足4个");
                } else {
                    Set<String> codes = new HashSet<>();
                    for (ApiResponse res : responses) {
                        codes.add(res.responseCode());
                        if (res.description().trim().isEmpty()) {
                            System.err.println("❌ " + clazz.getName() + "#" + method.getName() + " @ApiResponse(" + res.responseCode() + ") 缺少description");
                        }
                        if (res.content().length == 0) {
                            System.err.println("❌ " + clazz.getName() + "#" + method.getName() + " @ApiResponse(" + res.responseCode() + ") 缺少content");
                        }
                    }
                    // 检查是否包含所有必需响应码
                    for (String code : REQUIRED_CODES) {
                        if (!codes.contains(code)) {
                            System.err.println("❌ " + clazz.getName() + "#" + method.getName() + " 缺少响应码" + code + "的@ApiResponse");
                        }
                    }
                }
                checkMethodParameters(clazz, method, operation);
            }
        }
    }

    /**
     * 校验接口方法参数的Swagger3注解（PathVariable/基本类型RequestParam）
     */
    private static void checkMethodParameters(Class<?> controllerClazz, Method method, Operation operation) {
        String methodKey = controllerClazz.getName() + "#" + method.getName();
        // 收集@Operation中声明的参数名（用于校验统一声明场景）
        Set<String> operationParamNames = new HashSet<>();
        if (operation != null && operation.parameters() != null) {
            for (Parameter opParam : operation.parameters()) {
                operationParamNames.add(opParam.name().trim());
            }
        }

        // 遍历方法参数，逐个校验
        java.lang.reflect.Parameter [] methodParams = method.getParameters();
        for (int i = 0; i < methodParams.length; i++) {
            java.lang.reflect.Parameter  param = methodParams[i];
            String paramName = param.getName();
            Class<?> paramType = param.getType();
            String paramFullKey = methodKey + " 参数[" + i + "]:" + paramName;

            // 3.1 校验@PathVariable参数
            PathVariable pathVarAnno = param.getAnnotation(PathVariable.class);
            if (pathVarAnno != null) {
                checkPathVariableParam(paramFullKey, param, pathVarAnno, operationParamNames);
                continue;
            }

            // 3.2 校验@RequestParam参数（基本类型/包装类型）
            RequestParam requestParamAnno = param.getAnnotation(RequestParam.class);
            if (requestParamAnno != null) {
                checkRequestParamParam(paramFullKey, param, requestParamAnno, operationParamNames);
            }
        }
    }

    /**
     * 校验@PathVariable参数的Swagger3注解
     */
    private static void checkPathVariableParam(String paramFullKey, java.lang.reflect.Parameter param, PathVariable pathVarAnno, Set<String> operationParamNames) {
        // 获取@PathVariable的实际参数名
        String pathVarName = pathVarAnno.value().trim().isEmpty() ? param.getName() : pathVarAnno.value().trim();

        // 检查参数上是否有@Parameter注解
        Parameter paramAnno = param.getAnnotation(Parameter.class);
        boolean hasParamAnno = paramAnno != null;
        boolean hasOperationParam = operationParamNames.contains(pathVarName);

        // 必须有@Parameter（参数上或@Operation中）
        if (!hasParamAnno && !hasOperationParam) {
            System.err.println("❌ " + paramFullKey + " (@PathVariable) 未添加@Parameter注解（参数上或@Operation中）");
            return;
        }

        // 优先取参数上的@Parameter，否则取@Operation中的
        Parameter checkParamAnno = paramAnno;
        if (!hasParamAnno && hasOperationParam) {
            // 从@Operation中匹配参数名
            for (Parameter opParam : operationParamNames.stream()
                    .map(name -> new Parameter() {
                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return Parameter.class;
                        }

                        @Override
                        public String name() {
                            return name;
                        }

                        @Override
                        public ParameterIn in() {
                            return null;
                        }

                        @Override
                        public String description() {
                            return "";
                        }

                        @Override
                        public boolean required() {
                            return true;
                        }

                        @Override
                        public boolean deprecated() {
                            return false;
                        }

                        @Override
                        public boolean allowEmptyValue() {
                            return false;
                        }

                        @Override
                        public ParameterStyle style() {
                            return null;
                        }

                        @Override
                        public Explode explode() {
                            return null;
                        }

                        @Override
                        public boolean allowReserved() {
                            return false;
                        }

                        @Override
                        public String example() {
                            return "";
                        }

                        @Override
                        public Extension[] extensions() {
                            return new Extension[0];
                        }

                        @Override
                        public String ref() {
                            return "";
                        }

                        @Override
                        public Schema schema() {
                            return new Schema() {
                                @Override
                                public Class<? extends Annotation> annotationType() {
                                    return Schema.class;
                                }

                                @Override
                                public Class<?> implementation() {
                                    return null;
                                }

                                @Override
                                public Class<?> not() {
                                    return null;
                                }

                                @Override
                                public Class<?>[] oneOf() {
                                    return new Class[0];
                                }

                                @Override
                                public Class<?>[] anyOf() {
                                    return new Class[0];
                                }

                                @Override
                                public Class<?>[] allOf() {
                                    return new Class[0];
                                }

                                @Override
                                public String name() {
                                    return "";
                                }

                                @Override
                                public String title() {
                                    return "";
                                }

                                @Override
                                public double multipleOf() {
                                    return 0;
                                }

                                @Override
                                public String maximum() {
                                    return "";
                                }

                                @Override
                                public boolean exclusiveMaximum() {
                                    return false;
                                }

                                @Override
                                public String minimum() {
                                    return "";
                                }

                                @Override
                                public boolean exclusiveMinimum() {
                                    return false;
                                }

                                @Override
                                public int maxLength() {
                                    return 0;
                                }

                                @Override
                                public int minLength() {
                                    return 0;
                                }

                                @Override
                                public String pattern() {
                                    return "";
                                }

                                @Override
                                public int maxProperties() {
                                    return 0;
                                }

                                @Override
                                public int minProperties() {
                                    return 0;
                                }

                                @Override
                                public String[] requiredProperties() {
                                    return new String[0];
                                }

                                @Override
                                public boolean required() {
                                    return false;
                                }

                                @Override
                                public RequiredMode requiredMode() {
                                    return null;
                                }

                                @Override
                                public String description() {
                                    return "";
                                }

                                @Override
                                public String format() {
                                    return "";
                                }

                                @Override
                                public String ref() {
                                    return "";
                                }

                                @Override
                                public boolean nullable() {
                                    return false;
                                }

                                @Override
                                public boolean readOnly() {
                                    return false;
                                }

                                @Override
                                public boolean writeOnly() {
                                    return false;
                                }

                                @Override
                                public AccessMode accessMode() {
                                    return null;
                                }

                                @Override
                                public String example() {
                                    return "";
                                }

                                @Override
                                public ExternalDocumentation externalDocs() {
                                    return null;
                                }

                                @Override
                                public boolean deprecated() {
                                    return false;
                                }

                                @Override
                                public String type() {
                                    return "";
                                }

                                @Override
                                public String[] allowableValues() {
                                    return new String[0];
                                }

                                @Override
                                public String defaultValue() {
                                    return "";
                                }

                                @Override
                                public String discriminatorProperty() {
                                    return "";
                                }

                                @Override
                                public DiscriminatorMapping[] discriminatorMapping() {
                                    return new DiscriminatorMapping[0];
                                }

                                @Override
                                public boolean hidden() {
                                    return false;
                                }

                                @Override
                                public boolean enumAsRef() {
                                    return false;
                                }

                                @Override
                                public Class<?>[] subTypes() {
                                    return new Class[0];
                                }

                                @Override
                                public Extension[] extensions() {
                                    return new Extension[0];
                                }

                                @Override
                                public Class<?>[] prefixItems() {
                                    return new Class[0];
                                }

                                @Override
                                public String[] types() {
                                    return new String[0];
                                }

                                @Override
                                public int exclusiveMaximumValue() {
                                    return 0;
                                }

                                @Override
                                public int exclusiveMinimumValue() {
                                    return 0;
                                }

                                @Override
                                public Class<?> contains() {
                                    return null;
                                }

                                @Override
                                public String $id() {
                                    return "";
                                }

                                @Override
                                public String $schema() {
                                    return "";
                                }

                                @Override
                                public String $anchor() {
                                    return "";
                                }

                                @Override
                                public String $vocabulary() {
                                    return "";
                                }

                                @Override
                                public String $dynamicAnchor() {
                                    return "";
                                }

                                @Override
                                public String contentEncoding() {
                                    return "";
                                }

                                @Override
                                public String contentMediaType() {
                                    return "";
                                }

                                @Override
                                public Class<?> contentSchema() {
                                    return null;
                                }

                                @Override
                                public Class<?> propertyNames() {
                                    return null;
                                }

                                @Override
                                public int maxContains() {
                                    return 0;
                                }

                                @Override
                                public int minContains() {
                                    return 0;
                                }

                                @Override
                                public Class<?> additionalItems() {
                                    return null;
                                }

                                @Override
                                public Class<?> unevaluatedItems() {
                                    return null;
                                }

                                @Override
                                public Class<?> _if() {
                                    return null;
                                }

                                @Override
                                public Class<?> _else() {
                                    return null;
                                }

                                @Override
                                public Class<?> then() {
                                    return null;
                                }

                                @Override
                                public String $comment() {
                                    return "";
                                }

                                @Override
                                public Class<?>[] exampleClasses() {
                                    return new Class[0];
                                }

                                @Override
                                public AdditionalPropertiesValue additionalProperties() {
                                    return null;
                                }

                                @Override
                                public DependentRequired[] dependentRequiredMap() {
                                    return new DependentRequired[0];
                                }

                                @Override
                                public StringToClassMapItem[] dependentSchemas() {
                                    return new StringToClassMapItem[0];
                                }

                                @Override
                                public StringToClassMapItem[] patternProperties() {
                                    return new StringToClassMapItem[0];
                                }

                                @Override
                                public StringToClassMapItem[] properties() {
                                    return new StringToClassMapItem[0];
                                }

                                @Override
                                public Class<?> unevaluatedProperties() {
                                    return null;
                                }

                                @Override
                                public Class<?> additionalPropertiesSchema() {
                                    return null;
                                }

                                @Override
                                public String[] examples() {
                                    return new String[0];
                                }

                                @Override
                                public String _const() {
                                    return "";
                                }
                            };
                        }

                        @Override
                        public ArraySchema array() {
                            return null;
                        }

                        @Override
                        public Content[] content() {
                            return new Content[0];
                        }

                        @Override
                        public boolean hidden() {
                            return false;
                        }

                        @Override
                        public ExampleObject[] examples() {
                            return new ExampleObject[0];
                        }
                    })
                    .filter(opParam -> opParam.name().equals(pathVarName))
                    .toList()) {
                checkParamAnno = opParam;
            }
        }

        // 校验@Parameter核心字段
        if (checkParamAnno != null) {
            // 检查name是否与@PathVariable一致
            if (!checkParamAnno.name().trim().equals(pathVarName)) {
                System.err.println("❌ " + paramFullKey + " @Parameter.name(" + checkParamAnno.name() + ") 与@PathVariable.name(" + pathVarName + ") 不一致");
            }
            // 检查description
            if (checkParamAnno.description().trim().isEmpty()) {
                System.err.println("❌ " + paramFullKey + " @Parameter缺少description");
            }
            // 检查example
            if (checkParamAnno.example().trim().isEmpty()) {
                System.err.println("⚠️ " + paramFullKey + " @Parameter缺少example（建议添加）");
            }
            // 检查defaultValue
            if (checkParamAnno.schema().defaultValue().trim().isEmpty()) {
                System.err.println("⚠️ " + paramFullKey + " @Parameter.schema缺少defaultValue（建议添加）");
            }
            // 检查required（@PathVariable默认必填）
            if (!checkParamAnno.required()) {
                System.err.println("⚠️ " + paramFullKey + " @PathVariable参数@Parameter.required建议设为true");
            }
        }

        // 校验校验注解
        checkParamValidationAnno(paramFullKey, param, true);
    }

    /**
     * 校验@RequestParam参数的Swagger3注解（基本类型/包装类型）
     */
    private static void checkRequestParamParam(String paramFullKey, java.lang.reflect.Parameter  param, RequestParam requestParamAnno, Set<String> operationParamNames) {
        // 获取@RequestParam的实际参数名
        String reqParamName = requestParamAnno.value().trim().isEmpty() ? param.getName() : requestParamAnno.value().trim();
        boolean isRequired = requestParamAnno.required();
        Class<?> paramType = param.getType();
        boolean isBasicType = BASIC_TYPES.contains(paramType);
        boolean isNumberType = Number.class.isAssignableFrom(paramType) || BASIC_TYPES.stream().anyMatch(t -> t.isAssignableFrom(paramType));

        // 检查参数上是否有@Parameter注解
        Parameter paramAnno = param.getAnnotation(Parameter.class);
        boolean hasParamAnno = paramAnno != null;
        boolean hasOperationParam = operationParamNames.contains(reqParamName);

        // 必须有@Parameter（参数上或@Operation中）
        if (!hasParamAnno && !hasOperationParam) {
            System.err.println("❌ " + paramFullKey + " (@RequestParam) 未添加@Parameter注解（参数上或@Operation中）");
            return;
        }

        // 优先取参数上的@Parameter，否则取@Operation中的
        Parameter checkParamAnno = paramAnno;
        if (!hasParamAnno && hasOperationParam) {
            for (Parameter opParam : operationParamNames.stream()
                    .map(name -> new Parameter() {
                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return Parameter.class;
                        }

                        @Override
                        public String name() {
                            return name;
                        }

                        @Override
                        public ParameterIn in() {
                            return null;
                        }

                        @Override
                        public String description() {
                            return "";
                        }

                        @Override
                        public boolean required() {
                            return isRequired;
                        }

                        @Override
                        public boolean deprecated() {
                            return false;
                        }

                        @Override
                        public boolean allowEmptyValue() {
                            return false;
                        }

                        @Override
                        public ParameterStyle style() {
                            return null;
                        }

                        @Override
                        public Explode explode() {
                            return null;
                        }

                        @Override
                        public boolean allowReserved() {
                            return false;
                        }

                        @Override
                        public String example() {
                            return "";
                        }

                        @Override
                        public Extension[] extensions() {
                            return new Extension[0];
                        }

                        @Override
                        public String ref() {
                            return "";
                        }

                        @Override
                        public Schema schema() {
                            return new Schema() {
                                @Override
                                public Class<? extends Annotation> annotationType() {
                                    return Schema.class;
                                }

                                @Override
                                public Class<?> implementation() {
                                    return null;
                                }

                                @Override
                                public Class<?> not() {
                                    return null;
                                }

                                @Override
                                public Class<?>[] oneOf() {
                                    return new Class[0];
                                }

                                @Override
                                public Class<?>[] anyOf() {
                                    return new Class[0];
                                }

                                @Override
                                public Class<?>[] allOf() {
                                    return new Class[0];
                                }

                                @Override
                                public String name() {
                                    return "";
                                }

                                @Override
                                public String title() {
                                    return "";
                                }

                                @Override
                                public double multipleOf() {
                                    return 0;
                                }

                                @Override
                                public String maximum() {
                                    return "";
                                }

                                @Override
                                public boolean exclusiveMaximum() {
                                    return false;
                                }

                                @Override
                                public String minimum() {
                                    return "";
                                }

                                @Override
                                public boolean exclusiveMinimum() {
                                    return false;
                                }

                                @Override
                                public int maxLength() {
                                    return 0;
                                }

                                @Override
                                public int minLength() {
                                    return 0;
                                }

                                @Override
                                public String pattern() {
                                    return "";
                                }

                                @Override
                                public int maxProperties() {
                                    return 0;
                                }

                                @Override
                                public int minProperties() {
                                    return 0;
                                }

                                @Override
                                public String[] requiredProperties() {
                                    return new String[0];
                                }

                                @Override
                                public boolean required() {
                                    return false;
                                }

                                @Override
                                public RequiredMode requiredMode() {
                                    return null;
                                }

                                @Override
                                public String description() {
                                    return "";
                                }

                                @Override
                                public String format() {
                                    return "";
                                }

                                @Override
                                public String ref() {
                                    return "";
                                }

                                @Override
                                public boolean nullable() {
                                    return false;
                                }

                                @Override
                                public boolean readOnly() {
                                    return false;
                                }

                                @Override
                                public boolean writeOnly() {
                                    return false;
                                }

                                @Override
                                public AccessMode accessMode() {
                                    return null;
                                }

                                @Override
                                public String example() {
                                    return "";
                                }

                                @Override
                                public ExternalDocumentation externalDocs() {
                                    return null;
                                }

                                @Override
                                public boolean deprecated() {
                                    return false;
                                }

                                @Override
                                public String type() {
                                    return "";
                                }

                                @Override
                                public String[] allowableValues() {
                                    return new String[0];
                                }

                                @Override
                                public String defaultValue() {
                                    return "";
                                }

                                @Override
                                public String discriminatorProperty() {
                                    return "";
                                }

                                @Override
                                public DiscriminatorMapping[] discriminatorMapping() {
                                    return new DiscriminatorMapping[0];
                                }

                                @Override
                                public boolean hidden() {
                                    return false;
                                }

                                @Override
                                public boolean enumAsRef() {
                                    return false;
                                }

                                @Override
                                public Class<?>[] subTypes() {
                                    return new Class[0];
                                }

                                @Override
                                public Extension[] extensions() {
                                    return new Extension[0];
                                }

                                @Override
                                public Class<?>[] prefixItems() {
                                    return new Class[0];
                                }

                                @Override
                                public String[] types() {
                                    return new String[0];
                                }

                                @Override
                                public int exclusiveMaximumValue() {
                                    return 0;
                                }

                                @Override
                                public int exclusiveMinimumValue() {
                                    return 0;
                                }

                                @Override
                                public Class<?> contains() {
                                    return null;
                                }

                                @Override
                                public String $id() {
                                    return "";
                                }

                                @Override
                                public String $schema() {
                                    return "";
                                }

                                @Override
                                public String $anchor() {
                                    return "";
                                }

                                @Override
                                public String $vocabulary() {
                                    return "";
                                }

                                @Override
                                public String $dynamicAnchor() {
                                    return "";
                                }

                                @Override
                                public String contentEncoding() {
                                    return "";
                                }

                                @Override
                                public String contentMediaType() {
                                    return "";
                                }

                                @Override
                                public Class<?> contentSchema() {
                                    return null;
                                }

                                @Override
                                public Class<?> propertyNames() {
                                    return null;
                                }

                                @Override
                                public int maxContains() {
                                    return 0;
                                }

                                @Override
                                public int minContains() {
                                    return 0;
                                }

                                @Override
                                public Class<?> additionalItems() {
                                    return null;
                                }

                                @Override
                                public Class<?> unevaluatedItems() {
                                    return null;
                                }

                                @Override
                                public Class<?> _if() {
                                    return null;
                                }

                                @Override
                                public Class<?> _else() {
                                    return null;
                                }

                                @Override
                                public Class<?> then() {
                                    return null;
                                }

                                @Override
                                public String $comment() {
                                    return "";
                                }

                                @Override
                                public Class<?>[] exampleClasses() {
                                    return new Class[0];
                                }

                                @Override
                                public AdditionalPropertiesValue additionalProperties() {
                                    return null;
                                }

                                @Override
                                public DependentRequired[] dependentRequiredMap() {
                                    return new DependentRequired[0];
                                }

                                @Override
                                public StringToClassMapItem[] dependentSchemas() {
                                    return new StringToClassMapItem[0];
                                }

                                @Override
                                public StringToClassMapItem[] patternProperties() {
                                    return new StringToClassMapItem[0];
                                }

                                @Override
                                public StringToClassMapItem[] properties() {
                                    return new StringToClassMapItem[0];
                                }

                                @Override
                                public Class<?> unevaluatedProperties() {
                                    return null;
                                }

                                @Override
                                public Class<?> additionalPropertiesSchema() {
                                    return null;
                                }

                                @Override
                                public String[] examples() {
                                    return new String[0];
                                }

                                @Override
                                public String _const() {
                                    return "";
                                }
                            };
                        }

                        @Override
                        public ArraySchema array() {
                            return null;
                        }

                        @Override
                        public Content[] content() {
                            return new Content[0];
                        }

                        @Override
                        public boolean hidden() {
                            return false;
                        }

                        @Override
                        public ExampleObject[] examples() {
                            return new ExampleObject[0];
                        }
                    })
                    .filter(opParam -> opParam.name().equals(reqParamName))
                    .toList()) {
                checkParamAnno = opParam;
            }
        }

        // 校验@Parameter核心字段
        if (checkParamAnno != null) {
            // 检查name是否与@RequestParam一致
            if (!checkParamAnno.name().trim().equals(reqParamName)) {
                System.err.println("❌ " + paramFullKey + " @Parameter.name(" + checkParamAnno.name() + ") 与@RequestParam.name(" + reqParamName + ") 不一致");
            }
            // 检查description
            if (checkParamAnno.description().trim().isEmpty()) {
                System.err.println("❌ " + paramFullKey + " @Parameter缺少description");
            }
            // 检查example
            if (checkParamAnno.example().trim().isEmpty()) {
                System.err.println("⚠️ " + paramFullKey + " @Parameter缺少example（建议添加）");
            }
            // 检查defaultValue
            if (checkParamAnno.schema().defaultValue().trim().isEmpty()) {
                System.err.println("⚠️ " + paramFullKey + " @Parameter.schema缺少defaultValue（建议添加）");
            }
            // 检查required是否与@RequestParam一致
            if (checkParamAnno.required() != isRequired) {
                System.err.println("❌ " + paramFullKey + " @Parameter.required(" + checkParamAnno.required() + ") 与@RequestParam.required(" + isRequired + ") 不一致");
            }
        }

        // 校验校验注解
        checkParamValidationAnno(paramFullKey, param, isBasicType);

        // 数值类型额外校验：必须有范围注解（@Min/@Max等）
        if (isNumberType && !hasAnyAnnotation(param, NUMBER_VALID_ANNOS)) {
            System.err.println("⚠️ " + paramFullKey + " 数值类型参数建议添加@Min/@Max/@Digits等范围校验注解");
        }
    }

    /**
     * 校验参数的校验注解（@NotNull/@Min等）
     */
    private static void checkParamValidationAnno(String paramFullKey, java.lang.reflect.Parameter param, boolean isBasicType) {
        // 基本类型不能有@NotNull（默认值存在，校验无效）
        if (isBasicType && param.isAnnotationPresent(NotNull.class)) {
            System.err.println("❌ " + paramFullKey + " 基本类型参数不能添加@NotNull注解（无意义）");
        }

        // 包装类型必填时必须有@NotNull
        if (!isBasicType && param.isAnnotationPresent(RequestParam.class)) {
            RequestParam reqAnno = param.getAnnotation(RequestParam.class);
            if (reqAnno.required() && !param.isAnnotationPresent(NotNull.class)) {
                System.err.println("❌ " + paramFullKey + " 包装类型必填参数必须添加@NotNull注解");
            }
        }

        // @PathVariable参数必须有非空校验（@NotNull/@NotBlank）
        if (param.isAnnotationPresent(PathVariable.class) && !isBasicType && !param.isAnnotationPresent(NotNull.class) && !param.isAnnotationPresent(NotBlank.class)) {
            System.err.println("❌ " + paramFullKey + " @PathVariable包装类型参数必须添加@NotNull/@NotBlank注解");
        }
    }

    /**
     * 判断参数是否有指定注解中的任意一个
     */
    private static boolean hasAnyAnnotation(java.lang.reflect.Parameter param, Set<Class<? extends Annotation>> annoClasses) {
        for (Class<? extends Annotation> annoClass : annoClasses) {
            if (param.isAnnotationPresent(annoClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验DTO/VO的@Schema和校验注解
     */
    private static void checkDtoSchemaValidation() {
        System.out.println("\n--- 校验DTO/VO注解 ---");

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
                .addUrls(ClasspathHelper.forPackage(DTO_PACKAGE))
                .filterInputsBy(input -> input != null && input.endsWith(".class")));

        Set<Class<?>> dtos = reflections.getSubTypesOf(Object.class);
        dtos.removeIf(clazz -> clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) || clazz.getName().contains("$"));

        // 校验注解类型
        Set<Class<? extends Annotation>> validAnnos = new HashSet<>(Arrays.asList(
                NotBlank.class, NotNull.class, Size.class, Min.class, Max.class, Email.class
        ));

        for (Class<?> clazz : dtos) {
            // 类级别@Schema
            if (clazz.getAnnotation(Schema.class) == null) {
                System.err.println("❌ " + clazz.getName() + " 未添加@Schema注解");
            }

            // 字段级别@Schema和校验注解
            for (Field field : clazz.getDeclaredFields()) {
                Schema schema = field.getAnnotation(Schema.class);
                if (schema == null) {
                    System.err.println("❌ " + clazz.getName() + "." + field.getName() + " 未添加@Schema注解");
                } else {
                    if (schema.description().trim().isEmpty()) {
                        System.err.println("❌ " + clazz.getName() + "." + field.getName() + " @Schema缺少description");
                    }
                    if (schema.example().trim().isEmpty()) {
                        System.err.println("⚠️ " + clazz.getName() + "." + field.getName() + " @Schema缺少example（建议添加）");
                    }
                    if (schema.defaultValue() == null) {
                        System.err.println("⚠️ " + clazz.getName() + "." + field.getName() + " @Schema缺少defaultValue（建议添加）");
                    }
                }

                // 请求体DTO必须有校验注解
                if (clazz.getName().contains("request")) {
                    boolean hasValidAnno = false;
                    for (Class<? extends Annotation> anno : validAnnos) {
                        if (field.isAnnotationPresent(anno)) {
                            hasValidAnno = true;
                            break;
                        }
                    }
                    if (!hasValidAnno) {
                        System.err.println("⚠️ " + clazz.getName() + "." + field.getName() + " 缺少jakarta校验注解（请求体建议添加）");
                    }
                }
            }
        }
    }

    /**
     * 递归扫描类及其所有嵌套类（核心方法）
     */
    private static void scanClassWithNested(Class<?> clazz, Set<Class<? extends Annotation>> validAnnos) {
        // 标记为已扫描，避免循环引用
        if (SCANNED_CLASSES.contains(clazz)) {
            return;
        }
        SCANNED_CLASSES.add(clazz);

        // 1. 校验类级别@Schema
        Schema classSchema = clazz.getAnnotation(Schema.class);
        if (classSchema == null) {
            System.err.println("❌ " + getClassNameWithPath(clazz) + " 未添加@Schema注解");
        } else {
            if (classSchema.description().trim().isEmpty()) {
                System.err.println("❌ " + getClassNameWithPath(clazz) + " @Schema缺少description");
            }
        }

        // 2. 校验字段级别注解
        for (Field field : clazz.getDeclaredFields()) {
            // 2.1 校验字段@Schema
            Schema fieldSchema = field.getAnnotation(Schema.class);
            if (fieldSchema == null) {
                System.err.println("❌ " + getClassNameWithPath(clazz) + "." + field.getName() + " 未添加@Schema注解");
            } else {
                if (fieldSchema.description().trim().isEmpty()) {
                    System.err.println("❌ " + getClassNameWithPath(clazz) + "." + field.getName() + " @Schema缺少description");
                }
                if (fieldSchema.example().trim().isEmpty()) {
                    System.err.println("⚠️ " + getClassNameWithPath(clazz) + "." + field.getName() + " @Schema缺少example（建议添加）");
                }
                if (fieldSchema.defaultValue() == null || fieldSchema.defaultValue().trim().isEmpty()) {
                    System.err.println("⚠️ " + getClassNameWithPath(clazz) + "." + field.getName() + " @Schema缺少defaultValue（建议添加）");
                }
            }

            // 2.2 校验请求体字段的jakarta校验注解
            boolean isRequestDto = clazz.getName().toLowerCase().contains("request")
                    || field.getDeclaringClass().getName().toLowerCase().contains("request");
            if (isRequestDto) {
                boolean hasValidAnno = false;
                for (Class<? extends Annotation> anno : validAnnos) {
                    if (field.isAnnotationPresent(anno)) {
                        hasValidAnno = true;
                        break;
                    }
                }
                if (!hasValidAnno) {
                    System.err.println("⚠️ " + getClassNameWithPath(clazz) + "." + field.getName() + " 缺少jakarta校验注解（请求体建议添加）");
                }
            }

            // 3. 递归扫描字段类型的嵌套类
            Class<?> fieldType = field.getType();
            if (isCustomClass(fieldType)) {
                // 处理集合类型（List/Set）
                if (Collection.class.isAssignableFrom(fieldType)) {
                    try {
                        Class<?> genericType = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        if (isCustomClass(genericType)) {
                            scanClassWithNested(genericType, validAnnos);
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ " + getClassNameWithPath(clazz) + "." + field.getName() + " 泛型类型解析失败，跳过嵌套扫描");
                    }
                }
                // 处理数组类型
                else if (fieldType.isArray()) {
                    Class<?> arrayComponentType = fieldType.getComponentType();
                    if (isCustomClass(arrayComponentType)) {
                        scanClassWithNested(arrayComponentType, validAnnos);
                    }
                }
                // 处理普通自定义类
                else {
                    scanClassWithNested(fieldType, validAnnos);
                }
            }
        }

        // 4. 扫描当前类的直接嵌套类（静态内部类）
        for (Class<?> nestedClazz : clazz.getDeclaredClasses()) {
            if (Modifier.isStatic(nestedClazz.getModifiers()) && !nestedClazz.isInterface()) {
                scanClassWithNested(nestedClazz, validAnnos);
            }
        }
    }

    /**
     * 判断是否为自定义类（非基本类型、非JDK内置类）
     */
    private static boolean isCustomClass(Class<?> clazz) {
        return !clazz.isPrimitive()
                && !clazz.getName().startsWith("java.lang")
                && !clazz.getName().startsWith("java.util")
                && !clazz.getName().startsWith("jakarta")
                && !clazz.getName().startsWith("io.swagger")
                && !clazz.isEnum()
                && !clazz.isAnnotation();
    }

    /**
     * 获取类的完整路径（含嵌套类标识）
     */
    private static String getClassNameWithPath(Class<?> clazz) {
        return clazz.getName().replace("$", ".");
    }
}