# request

#### 介绍
一款便捷发送HTTP请求的工具，简化了在Java项目中发送网络请求的过程。

#### 软件架构
- 使用Java语言编写
- 使用JDK的`HttpClient`实现网络请求
- 支持同步和异步请求
- 支持多种JSON解析库（Fastjson2、Jackson）

#### 功能特性
- 支持GET、POST、PUT、DELETE等常见HTTP方法
- 支持请求参数、JSON请求体、表单提交
- 支持自定义结果处理器
- 支持接口式声明请求（通过注解）
- 支持多种JSON序列化/反序列化工具
- 支持接口返回值为嵌套复杂泛型
- 支持定义默认请求头信息

#### 使用说明

1. 在接口上使用 `@Request("请求地址")` 注解指定请求的基础URL。
2. 在接口方法上使用 `@Get`、`@Post`、`@Put`、`@Delete` 等注解指定请求方式。
3. 方法参数可以使用 `@RequestBody`、`@Param`、`@PathVariable`、`@RequestForm`、`@Headers` 等注解指定参数类型。
4. 接口返回值自动适配处理。
5. 创建 `Configuration` 实例并注册请求接口：

   ```java
   Configuration config = new Configuration();
   config.addRequest(TestHttp.class);
   ```
6. 获取接口的实现类：
   ```java
   TestHttp testHttp = config.getRequest(TestHttp.class);
   ```
7. 调用接口方法即可发送请求。

#### 示例代码

```java
@Request("https://api.example.com")
public interface MyApi {
    @Get("/user")
    User getData(@Param("id") int id);

    @Get("/list/{pageNum}")
    List<User> getDataList(@PathVariable("pageNum") Integer pageNum);
    
    @Post
    Result postData(@RequestBody User user);
    
    //支持嵌套复杂泛型返回值
    @Get("/Users")
    Result<List<User>> getUser();
    
}

public class Main {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.addRequest(MyApi.class);
        MyApi api = config.getRequest(MyApi.class);

        User data = api.getData(1);
        System.out.println(data);
        
        User user = new User();
        user.setId(1L);
        user.setName("test");
        Result result = api.postData(user);
        System.out.println(result);
    }
}
```
#### 默认请求头定义
接口中定义静态方法返回一个HttpHeaders对象

```java
@Request("https://xxxx.com")
public interface TestHttp {
    static HttpHeaders header() {
        return HttpHeaders.newBuilder()
                .setHeader("sign","xxxxxxxxxxxxxxxx")
                .setHeader("content-type","application/json");
    }
    @Get
    String get(@Param("query") String query);
}
```

#### 支持的注解
| 注解名 | 用途                                                |
|--------|---------------------------------------------------|
| `@Request` | 标注在接口上，指定请求的基础URL                                 |
| `@Get` | 标注在方法上，表示使用GET请求                                  |
| `@Post` | 标注在方法上，表示使用POST请求                                 |
| `@Put` | 标注在方法上，表示使用PUT请求                                  |
| `@Delete` | 标注在方法上，表示使用DELETE请求                               |
| `@RequestBody` | 标注在方法参数上，自动识别ofString(json),ofByteArray,ofFile等   |
| `@Param` | 标注在方法参数上，表示该参数为URL查询参数(启用编译选项 -parameters后可省略该注解) |
| `@PathVariable` | 标注在方法参数上，表示该参数为URL路径参数(启用编译选项 -parameters后可不填属性值) |
| `@RequestForm` | 标注在方法参数上，表示该参数为表单请求体                              |
| `@Headers` | 标注在方法参数上，表示该参数为请求头(可省略该注解)                        |
| `@RequestAsync` | 标注在方法上，表示该请求为异步请求                                 |

```java
@Post
User postData(@RequestBody User user, @Headers HttpHeaders headers);
@Post                                 //@Headers注解省略可不写
User postData(@RequestBody User user, HttpHeaders headers);

@Get("/user")
User getData(@Param("id") int id);
@Get("/user")   //启用编译选项 -parameters后@Param("id")注解可省略不写 
User getData(int id);

@Get("/list/{pageNum}")
List<User> getDataList(@PathVariable("pageNum") Integer pageNum);
@Get("/list/{pageNum}")   //启用编译选项 -parameters后@PathVariable("pageNum")注解内属性值"pageNum"可省略不写 
List<User> getDataList(@PathVariable Integer pageNum);


```

#### JSON处理
支持以下JSON库：
- Fastjson2
- Jackson

#### 异步请求
通过 `@RequestAsync` 注解可以轻松实现异步请求。

#### 自定义结果处理
可以通过实现 `ResultHandler` 接口来自定义结果处理逻辑。
创建类实现ResultHandler接口
```java
public class LoginResultHandler implements ResultHandler<byte[]> {
    @Override
    public Object handle(HttpResponse<byte[]> httpResponse) {
        if (httpResponse.statusCode() == 200) {
            byte[] body = httpResponse.body();
            String contentEncoding = httpResponse.headers()
                    .firstValue("Content-Encoding")
                    .orElse("");
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                try(GZIPInputStream gzipIs =new GZIPInputStream(new ByteArrayInputStream(body)); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int len;
                    // 读取解压后的数据
                    while ((len = gzipIs.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                    body=bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("Gzip解压失败", e);
                }

            }
                    //解密数据
            return xxx.decrypt(body);
        }
        return null;
    }

    @Override
    public HttpResponse.BodyHandler<byte[]> bodyHandler() {
        return HttpResponse.BodyHandlers.ofByteArray();
    }
}


//使用自定义ResultHandler
@Post(handler = LoginResultHandler.class)
Result<Login> login(HttpHeaders httpHeaders,@RequestBody User user);
```

#### 依赖
- JDK 11+
- Fastjson2 或 Jackson

#### 许可证
AGPL-3.0 license