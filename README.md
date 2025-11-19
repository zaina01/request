# request

#### 介绍
一款便捷发送HTTP请求的工具，简化了在Java项目中发送网络请求的过程。

#### 软件架构
- 使用Java语言编写
- 使用JDK的`HttpClient`实现网络请求
- 支持同步和异步请求
- 支持多种JSON解析库（Fastjson、Fastjson2、Jackson）

#### 功能特性
- 支持GET、POST、PUT、DELETE等常见HTTP方法
- 支持请求参数、JSON请求体、表单提交
- 支持自定义结果处理器
- 支持接口式声明请求（通过注解）
- 支持多种JSON序列化/反序列化工具

#### 使用说明

1. 在接口上使用 `@Request("请求地址")` 注解指定请求的基础URL。
2. 在接口方法上使用 `@Get`、`@Post`、`@Put`、`@Delete` 等注解指定请求方式。
3. 方法参数可以使用 `@RequestJson`、`@Param`、`@RequestForm`、`@Headers` 等注解指定参数类型。
4. 接口返回值自动适配处理。
5. 创建 `Configuration` 实例并注册请求接口：
   ```java
   Configuration config = new Configuration();
   config.addRequest(接口.class);
   ```
6. 获取接口的实现类：
   ```java
   接口实例 = config.getRequest(接口.class);
   ```
7. 调用接口方法即可发送请求。

#### 示例代码

```java
@Request("https://api.example.com")
public interface MyApi {
    @Get
    String getData(@Param("id") int id);
    
    @Post
    String postData(@RequestJson User user);
}

public class Main {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.addRequest(MyApi.class);
        MyApi api = config.getRequest(MyApi.class);
        
        String data = api.getData(1);
        System.out.println(data);
        
        User user = new User();
        user.setId(1L);
        user.setName("test");
        String result = api.postData(user);
        System.out.println(result);
    }
}
```

#### 支持的注解
| 注解名 | 用途 |
|--------|------|
| `@Request` | 标注在接口上，指定请求的基础URL |
| `@Get` | 标注在方法上，表示使用GET请求 |
| `@Post` | 标注在方法上，表示使用POST请求 |
| `@Put` | 标注在方法上，表示使用PUT请求 |
| `@Delete` | 标注在方法上，表示使用DELETE请求 |
| `@RequestJson` | 标注在方法参数上，表示该参数为JSON请求体 |
| `@Param` | 标注在方法参数上，表示该参数为URL查询参数 |
| `@RequestForm` | 标注在方法参数上，表示该参数为表单请求体 |
| `@Headers` | 标注在方法参数上，表示该参数为请求头 |
| `@RequestAsync` | 标注在方法上，表示该请求为异步请求 |

#### JSON处理
支持以下JSON库：
- Fastjson
- Fastjson2
- Jackson

#### 异步请求
通过 `@RequestAsync` 注解可以轻松实现异步请求。

#### 自定义结果处理
可以通过实现 `ResultHandler` 接口来自定义结果处理逻辑。

#### 依赖
- JDK 11+
- Fastjson、Fastjson2 或 Jackson（可选）

#### 许可证
MIT License