# request

#### 介绍
一款便捷发送http请求的工具

#### 软件架构
java 



#### 使用说明

1.  接口上使用@Request("此处填网址")
2.  接口方法使用对应请求注解@Get @Post @Put等 如果有请求参数或请求表单json等 可以在方法参数上使用对应@RequestJson @Param  @RequestForm 注解
3.  接口返回值自适应
4.  创建 Configuration config = new Configuration(); 
5.  使用 config.addRequest(接口.class);
6.  获取接实现类 config.getRequest(接口.class);
7.  使用接实现类调用方法即可