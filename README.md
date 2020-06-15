# Computer-Network-Homework

## 要求： 
1. 不允许基于netty等框架，完全基于Java Socket API进行编写
2. 不分区使用的IO模型，BIO、NIO和AIO都可以
3. 实现基础的HTTP请求、响应功能，具体要求如下：
    3.1 HTTP客户端可以发送请求报文、呈现响应报文（命令行和GUI都可以）
    3.2 HTTP服务器端支持GET和POST请求
    3.3 HTTP服务器端支持200、301、302、304、404、405、500的状态码
    3.4 HTTP服务器端实现长连接
3.5 MIME至少支持三种类型，包含一种非文本类型
4．基于以上的要求，实现注册，登录功能(数据无需持久化，存在内存中即可，只需要实现注册和登录的接口，可以使用postman等方法模拟请求发送，无需客户端)。
## 参考资料：
1. https://docs.oracle.com/javase/tutorial/networking/sockets/index.html
2. http://www.runoob.com/java/java-networking.html
3. https://tools.ietf.org/html/rfc2616

## 功能说明

**重要：请助教和老师在使用本项目之前清空浏览器缓存，因为使用了cookie，未清空浏览器缓存可能会导致一些异常情况的出现**

### 项目介绍

本项目基于`java Socket API`进行开发，运行`HttpServer`即可。会自动启动系统默认浏览器并打开门户网站，等待用户进行选择，所有页面罗列如下：

| 网址                           | 内容     |
| ------------------------------ | -------- |
| http://localhost:8080          | 门户网站 |
| http://localhost:8080/login    | 登陆     |
| http://localhost:8080/register | 注册     |
| http://localhost:8080/301      | 301      |
| http://localhost:8080/302      | 302      |
| http://localhost:8080/304      | 304      |
| http://localhost:8080/404      | 404      |
| http://localhost:8080/txt      | TXT传输  |
| http://localhost:8080/img      | 图像传输 |

**使用过程中有网站自动跳转逻辑，但是使用自动跳转的都是状态码为200的页面，有以下页面存在跳转逻辑**

* "User not found, try register first"页面会在3秒之后跳转到登陆界面

* "Login Success"页面会在4秒之后跳转到门户网站

* "Wrong Password"页面会在4秒之后跳转到登陆界面

* "User already exist"页面会在3秒之后跳转到注册页面

* "Register Success"页面会在4秒之后跳转到登陆页面

### 各网站介绍

#### 门户网站介绍-李镔达，杜铭哲

#### 200：连接成功-李镔达

#### 301：端口号永久转移-韩茁

自动跳转百度

#### 302：端口号临时转义-刘瑞麒

自动跳转百度

#### 304：网页没有进行改动-杜铭哲

#### 404：端口号无法访问-戴俊浩

会返回一张404的图片，同时告知浏览器404的状态码，在Network中可以进行查看。

#### 405：方法禁用-李镔达

，无论请求什么端口，只能使用get和post 

#### 500：服务器内部错误-戴俊浩

注册账号的时候用户名不得超过八位，用户名不得为空，密码不能为空，否则会引发服务器内部错误。返回的状态码为500

#### MIME-李镔达

#### cookie使用-杜铭哲

## 制作人员列表

"User not found, try register first" **李镔达**

"Login Success" **戴俊浩，李镔达**

"Wrong Password" **韩茁,戴俊浩**

"User already exist" **杜铭哲**

"Register Success" **刘瑞麒，戴俊浩**

门户网站优化**杜铭哲，李镔达**

set_cookie**杜铭哲**
