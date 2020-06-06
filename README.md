# Computer-Network-Homework
## 说明： 
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
1. 200：正常执行
2. 301：端口号永久转移，自动跳转百度 **韩茁**
3. 302：端口号临时转义，自动跳转百度**刘瑞麒**
4. 304：网页没有进行改动 **杜铭哲**
5. 404：端口号无法访问，比如请求6000端口，直接返回404，没有任何数据。
6. 405：方法禁用，无论请求什么端口，只能使用get和post **李镔达**
7. 500：服务器内部错误，注册账号的时候用户名不得超过八位，否则会引发服务器内部错误。**戴俊浩**
---
8. MIME: **李镔达**
"User not found, try register first" **李镔达**
"Login Success" **戴俊浩**
"Wrong Password" **韩茁**
"User already exist" **杜铭哲**
"Register Success" **刘瑞麒**
405解决链接问题**李镔达**
门户网站优化**杜铭哲**
set_cookie**杜铭哲**
