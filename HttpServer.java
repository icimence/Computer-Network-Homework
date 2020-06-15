import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

//实现Runnable接口
public class HttpServer implements Runnable {
    private static Map<String, String> userData;
    private static int userNum = 0;
    private final Socket socket;
    private static String LastModified = "";
    private static String Etag = "";

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
            System.out.println("Visit http://localhost:8080 to have a try");
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
                if (!Method.equals("GET") && !Method.equals("POST")) {
                    Status_405(writer, outputStream);
                } else {
                    switch (url) {
                        case "/": {
                            responseHeadAction(writer, Method);
                            byte[] data = readFile("assets/html/p.html");
                            outputStream.write(data);
                            outputStream.flush();
                            break;
                        }
                        case "/login":
                            if (Method.equals("GET")) {
                                //向客户端传送header
                                ArrayList<String> requestInfo = new ArrayList<>();
                                String str = null;
                                String user = "";
                                str = bufferedReader.readLine();
                                while (!str.equals("") && str != null) {
                                    str = bufferedReader.readLine();
                                    requestInfo.add(str);
                                }
                                for (String s : requestInfo) {
                                    if (s.startsWith("Cookie")) {
                                        user = s.substring(8);
                                        break;
                                    }
                                }
                                if(!user.equals("")) {
                                    responseHeadAction(writer, Method);
                                    byte[] data = readFile("assets/html/login_success.html");
                                    outputStream.write(data);
                                    outputStream.flush();
                                } else {
                                    responseHeadAction(writer, Method);
                                    byte[] data = readFile("assets/html/login.html");
                                    outputStream.write(data);
                                    outputStream.flush();
                                }
                            } else if (Method.equals("POST")) {
                                String[] getData = handleUserNameAndPassword(bufferedReader);
                                if (userData.get(getData[0]) == null) {
                                    responseHeadAction(writer, Method);
                                    outputStream.write(readFile("assets/html/user_not_found.html"));

                                } else {
                                    if (userData.get(getData[0]).equals(getData[1])) {
                                        StringBuilder cookie = new StringBuilder();
                                        for(int i = 0; i < getData[0].length(); i++) {
                                            cookie.append(getData[0].charAt(i) + i + 6);
                                        }
                                        for(int i = 0; i < getData[1].length(); i++) {
                                            cookie.append(getData[1].charAt(i) - i - 6);
                                        }
                                        userNum++;
                                        String name = "User" + userNum;
                                        responseHeadActionWithCookie(writer, Method, name, cookie.toString());
                                        outputStream.write(readFile("assets/html/login_success.html"));
                                    } else {
                                        responseHeadAction(writer, Method);
                                        outputStream.write(readFile("assets/html/wrong_password.html"));
                                    }
                                }
                                outputStream.flush();
                            }
                            break;
                        case "/register":
                            if (Method.equals("GET")) {
                                //向客户端传送header
                                responseHeadAction(writer, Method);
                                byte[] data = readFile("assets/html/register.html");
                                outputStream.write(data);
                                outputStream.flush();
                            } else if (Method.equals("POST")) {
                                String[] getData = handleUserNameAndPassword(bufferedReader);
                                if (getData[0].length() <= 8 && !getData[1].isEmpty() && !getData[0].isEmpty()) {
                                    responseHeadAction(writer, Method);
                                    if (userData.get(getData[0]) != null) {
                                        outputStream.write(readFile("assets/html/user_already_exist.html"));
                                    } else {
                                        userData.put(getData[0], getData[1]);
                                        outputStream.write(readFile("assets/html/register_success.html"));
                                    }
                                    outputStream.flush();
                                } else {
                                    Status_500(writer, outputStream);
                                }
                            }
                            break;
                        case "/favicon.ico": {
                            responseHeadAction(writer, Method);
                            byte[] data = readFile("assets/img/logo.png");
                            outputStream.write(data);
                            outputStream.flush();
                            break;
                        }
                        case "/img":
                            writer.println("HTTP/1.1 200 OK");
                            writer.println("Server: HttpServer");
                            writer.println("Accept: */*");
                            writer.println("Accept-language: zh-cn");
                            writer.println("Connection: keep-alive");
                            writer.println("ContentType: image/jpeg");
                            writer.println("ContentLength: " + new File("assets/img/mime_jpeg.jpg").length());
                            LastModified = toGMTString(new Date());
                            writer.println("Last-Modified: " + LastModified);
                            Etag = LastModified.replace(" ", "");
                            Etag = Etag.replace(",", "");
                            Etag = Etag.replace(":", "");
                            writer.println("Etag: " + Etag);
                            writer.println("Host: localhost");
                            writer.println("Method: " + Method);
                            writer.println();
                            writer.flush();
                            outputStream.write(readFile("assets/img/mime_jpeg.jpg"));
                            outputStream.flush();
                            break;
                        case "/txt":
                            writer.println("HTTP/1.1 200 OK");
                            writer.println("Server: HttpServer");
                            writer.println("Accept: */*");
                            writer.println("Accept-language: zh-cn");
                            writer.println("Connection: keep-alive");
                            writer.println("ContentType: text/plain");
                            writer.println("ContentLength: " + new File("assets/txt/mime_txt.txt").length());
                            LastModified = toGMTString(new Date());
                            writer.println("Last-Modified: " + LastModified);
                            Etag = LastModified.replace(" ", "");
                            Etag = Etag.replace(",", "");
                            Etag = Etag.replace(":", "");
                            writer.println("Etag: " + Etag);
                            writer.println("Host: localhost");
                            writer.println("Method: " + Method);
                            writer.println();
                            writer.flush();
                            outputStream.write(readFile("assets/txt/mime_txt.txt"));
                            outputStream.flush();
                            break;
                        case "/301":
                            Status_301(writer, outputStream);
                        case "/404":
                            Status_404(writer, outputStream);
                            break;
                        case "/304":
                            if (Method.equals("GET")) {
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
                                responseHeadAction(writer, Method);
                                outputStream.write("Hello World!".getBytes());
                                outputStream.flush();
                            }
                            break;
                        case "/302":
                            Status_302(writer, Method);
                            outputStream.write("Status 302".getBytes());
                            outputStream.flush();
                            break;
                        default:
                            break;
                    }
                }
            } catch (NullPointerException npe) {
                npe.getMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert bufferedReader != null;
                bufferedReader.close();
                assert writer != null;
                writer.close();
                assert outputStream != null;
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] handleUserNameAndPassword(BufferedReader bufferedReader) throws IOException {
        String data = bufferedReader.readLine();
        while (!data.equals("") && data != null) {
            data = bufferedReader.readLine();
        }
        char[] source = new char[1000];
        bufferedReader.read(source);
        String inputData = String.valueOf(source);
        String userName = inputData.substring(inputData.indexOf('=') + 1, inputData.indexOf('&'));
        String password = inputData.substring(inputData.lastIndexOf('=') + 1, inputData.indexOf('\0'));
        return new String[]{userName, password};
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

    private void responseHeadActionWithCookie(PrintWriter writer, String method, String name, String value) {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        writer.println("Set-Cookie: " + name + "=" + value + "; " + "max-age=20");
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

    private void Status_301(PrintWriter writer, BufferedOutputStream outputStream) {
        writer.println("HTTP/1.1 301 Permanently Moved");
        int newPort = socket.getPort();
        writer.println("Status: 301 Permanently Moved to Port" + String.valueOf(newPort));
        writer.println("Server: HttpServer");
        writer.println("Accept: */*");
        writer.println("Accept-language: zh-cn");
        writer.println("Connection: keep-alive");
        writer.println("ContentType: text/html");
        writer.println("Location: http://www.baidu.com");
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
        try {
            byte[] data = readFile("assets/img/404.jpg");
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
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
            outputStream.write("UserName should be less than 8 Character or PassWord should not be empty".getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
