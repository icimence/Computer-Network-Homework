import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//实现Runnable接口
public class HttpServer implements Runnable {
    private static Map<String, String> userData;
    private Socket socket;
    private static String LastModified = "";
    private static String Etag = "";
    private static int port;

    //    static int DEFAULT_PORT=8080;
    public HttpServer(Socket s) {
        socket = s;
    }

    public static void main(String[] args) {
        int port;
        userData = new HashMap<>();
        ServerSocket serverSocket;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("port=8080 (default)");
            port = 8080;
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening for a connection on port: " + port + "......");
            while (true) {//通过死循环建立长连接，不断监听客户端传来的消息
                Socket client = serverSocket.accept();

                HttpServer httpServer = new HttpServer(client);
                Thread thread = new Thread(httpServer);//为连接的客户端开一个线程
                thread.start();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void run() {

        //在这里处理client端发来的request
        BufferedReader bufferedReader = null;
        PrintWriter writer = null;
        BufferedOutputStream outputStream = null;
        String result;
        try {
            //处理输入流
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //处理发送给客户端的输出流
            //header部分
            writer = new PrintWriter(socket.getOutputStream());
            //数据部分
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            //获取第一行
            try {
                String firstLine = bufferedReader.readLine();
                StringTokenizer tokenizer = new StringTokenizer(firstLine);
                //获取方法名
                String Method = tokenizer.nextToken().toUpperCase();
                //获取url
                String url = tokenizer.nextToken().toLowerCase();
                //该服务器只支持GET和POST方法
                int port = this.socket.getLocalPort();
                if (!Method.equals("GET") && !Method.equals("POST")) {
                    Status_405(writer, outputStream);
                } else {
                    if (url.equals("/")) {
                        responseHeadAction(writer, Method);
                        byte[] data = readFile("p.html");
                        outputStream.write(data);
                        outputStream.flush();
                    } else if (url.equals("/login")) {
                        if (Method.equals("GET")) {
                            //向客户端传送header
                            responseHeadAction(writer, Method);

                            byte[] data = readFile("login.html");
                            outputStream.write(data);
                            outputStream.flush();
                        } else if (Method.equals("POST")) {
                            //TODO
                            responseHeadAction(writer, Method);

                            String data = bufferedReader.readLine();
                            while (!data.equals("") && data != null) {
                                data = bufferedReader.readLine();
                            }
                            char[] source = new char[1000];
                            bufferedReader.read(source);
                            System.out.println(source);
                            String inputData = String.valueOf(source);
                            String userName = inputData.substring(inputData.indexOf('=') + 1, inputData.indexOf('&'));
                            String password = inputData.substring(inputData.lastIndexOf('=') + 1, inputData.indexOf('\0'));
                            if (userData.get(userName) == null) {
                                result = "User not found, try register first";
                            } else {
                                if (userData.get(userName).equals(password)) {
                                    result = "Login Success";
                                } else {
                                    result = "Wrong Password";
                                }
                            }
                            System.out.println(result);
                            outputStream.write(result.getBytes());
                            outputStream.flush();
                        }
                    } else if (url.equals("/register")) {
                        if (Method.equals("GET")) {
                            //向客户端传送header
                            responseHeadAction(writer, Method);

                            byte[] data = readFile("register.html");
                            outputStream.write(data);

                            outputStream.flush();
                        } else if (Method.equals("POST")) {
                            //TODO


                            String data = bufferedReader.readLine();
                            while (!data.equals("") && data != null) {
                                data = bufferedReader.readLine();
                            }
                            char[] source = new char[1000];
                            bufferedReader.read(source);
                            System.out.println(source);
                            String inputData = String.valueOf(source);
                            String userName = inputData.substring(inputData.indexOf('=') + 1, inputData.indexOf('&'));
                            String password = inputData.substring(inputData.lastIndexOf('=') + 1, inputData.indexOf('\0'));
                            if (userName.length() <= 8) {
                                responseHeadAction(writer, Method);
                                if (userData.get(userName) != null) {
                                    result = "User already exist";
                                    System.out.println(result);
                                } else {
                                    userData.put(userName, password);
                                    result = "Register Success";
                                    System.out.println(result);
                                }
                                outputStream.write(result.getBytes());
                                outputStream.flush();
                            } else {
                                Status_500(writer, outputStream);
                            }
                        }
                    } else if (url.equals("/favicon.ico")) {
                        responseHeadAction(writer, Method);
                        byte[] data = readFile("小龙虾.jpeg");
                        outputStream.write(data);
                        outputStream.flush();
                    } else {
                        int statusCode = Integer.parseInt(url.substring(1));
                        switch (statusCode) {
                            case 301:
                                //TODO
                                //写自己的方法
                                Status_301(writer, outputStream);
//                                if(Method.equals("GET")){
//
//                                }
//                                else if (Method.equals("POST")){
//
//                                }
//                                break;

                            case 302:
                                //TODO
                                Status_302(writer, Method);
                                outputStream.write("Hello World!".getBytes());
                                outputStream.flush();
                                //写自己的方法
                                break;
                            case 304:
                                //TODO
                                if (Method.equals("GET")) {
                                    //向客户端传送header
                                    String ifModifiedSince = "";
                                    String ifNoneMatch = "";
                                    ArrayList<String> requestInfo = new ArrayList<>();
                                    String str = null;
                                    str = bufferedReader.readLine();
                                    while (!str.equals("") && str != null) {
                                        str = bufferedReader.readLine();
                                        requestInfo.add(str);
                                    }
                                    for (String s : requestInfo) {
                                        if (s.startsWith("If-None-Match")) {
                                            ifNoneMatch = s.substring(15);
                                            break;
                                        }
                                    }
                                    for (String s : requestInfo) {
                                        if (s.startsWith("If-Modified-Since")) {
                                            ifModifiedSince = s.substring(19);
                                            break;
                                        }
                                    }
                                    //outputStream.write(("LastModified: " + LastModified + "\n").getBytes());
                                    //outputStream.write(("ifModifiedSince: " + ifModifiedSince + "\n").getBytes());
                                    //outputStream.write(("Etag: " + Etag + "\n").getBytes());
                                    //outputStream.write(("ifNoneMatch: " + ifNoneMatch + "\n").getBytes());
                                    if (LastModified.equals("")) {
                                        responseHeadAction(writer, Method);
                                    } else if (LastModified.equals(ifModifiedSince)) {
                                        Status_304(writer, Method);
                                    } else {
                                        responseHeadAction(writer, Method);
                                    }
                                    outputStream.write("Hello World!".getBytes());
                                    outputStream.flush();
                                } else if (Method.equals("POST")) {
                                    //TODO
                                    responseHeadAction(writer, Method);
                                    outputStream.write("Hello World!".getBytes());
                                    outputStream.flush();
                                }
                                break;
                            case 404:
                                Status_404(writer,outputStream);
                                //写自己的方法
                                break;
                            case 500:
                                //TODO
                                //写自己的方法
                                break;
                        }

                    }
                }
            } catch (NullPointerException npe) {
                npe.getMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                writer.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void responseHeadAction(PrintWriter writer, String method) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        LastModified = toGMTString(new Date());
        writer.println("Last-Modified: " + LastModified);
        Etag = LastModified.replace(" ", "");
        Etag = Etag.replace(",", "");
        Etag = Etag.replace(":", "");
        writer.println("Etag: " + Etag);
        //writer.println("ContentLength: "+new File("login.html").length());
        writer.println("Host: localhost");
        writer.println("Method: " + method);
        writer.println();
        writer.flush();
    }

    //读取文件，转成字节码
    private byte[] readFile(String f) throws FileNotFoundException {
        File file = new File(f);
        int length = new Long(file.length()).intValue();
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] res = new byte[length];
        try {
            fileInputStream.read(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String toGMTString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
        df.setTimeZone(new java.util.SimpleTimeZone(0, "GMT"));
        return df.format(date);
    }

    //参数就传run里面的writer和outputStream,一个负责响应头，一个负责响应内容
    // 别忘了两个最后要flush以及writer最后flush前还要写进一行空行（重要），具体写法参见run内部和responseHeadAction内容
    private void Status_301(PrintWriter writer, BufferedOutputStream outputStream) {
        //TODO
        writer.println("HTTP/1.1 301 Permanently Moved");
//        writer.println("Status: 301 Permanently Moved to Port 8000");
        int newPort = socket.getPort();
        writer.println("Status: 301 Permanently Moved to Port" + String.valueOf(newPort));
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
//        writer.println("Allow: GET,POST");
        writer.println("Location: http://www.baidu.com");
//        writer.println("Location: https://www.pornhub.com");
        writer.println();
        writer.flush();
        try {
            outputStream.write("Hello World".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Status_302(PrintWriter writer, String method) {
        writer.println("HTTP/1.1 302 Found");
        writer.println("Server: HttpServer");
        writer.println("Location:http://www.baidu.com");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        LastModified = toGMTString(new Date());
        writer.println("Last-Modified: " + LastModified);
        Etag = LastModified.replace(" ", "");
        Etag = Etag.replace(",", "");
        Etag = Etag.replace(":", "");
        writer.println("Etag: " + Etag);
        //writer.println("ContentLength: "+new File("login.html").length());
        writer.println("Host: localhost");
        writer.println("Method: " + method);
        //并不是所有服务器都支持重定向，所以下面给出手动跳转的操作
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Document Moved</title></head>");
        html.append("<body>");
        String newSite = "http://www.baidu.com";
        html.append("The site has moved,please update your bookmarket,and click the hyper link <a href='" + newSite + "'>" + "新地址" + "</a>!");
        html.append("</body></html>");
        writer.write(html.toString());
        writer.println();
        writer.flush();
    }


    private void Status_304(PrintWriter writer, String method) {
        writer.println("HTTP/1.1 304 Not Modified");
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        LastModified = toGMTString(new Date());
        writer.println("Last-Modified: " + LastModified);
        Etag = LastModified.replace(" ", "");
        Etag = Etag.replace(",", "");
        Etag = Etag.replace(":", "");
        writer.println("Etag: " + Etag);
        //writer.println("ContentLength: "+new File("login.html").length());
        writer.println("Host: localhost");
        writer.println("Method: " + method);
        writer.println();
        writer.flush();
    }

    private void Status_404(PrintWriter writer, BufferedOutputStream outputStream) {
        writer.println("HTTP/1.1 404 Page not Found");
        writer.println("Status: 404 Page notFound");
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        writer.println();
        writer.flush();
        try{
            byte[] data = readFile("404.jpg");
            outputStream.write(data);
            outputStream.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void Status_405(PrintWriter writer, BufferedOutputStream outputStream) {
        writer.println("HTTP/1.1 405 Method not allowed");
        writer.println("Status: 405 Method not allowed");
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        writer.println("Allow: GET,POST");
        writer.println();
        writer.flush();
        try {
            outputStream.write("Hello World".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Status_500(PrintWriter writer, BufferedOutputStream outputStream) {
        writer.println("HTTP/1.1 500 Server Error");
        writer.println("Status: 500 Server Error");
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        writer.println();
        writer.flush();
        try {
            outputStream.write("UserName or PassWord unavailable".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
