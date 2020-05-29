import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

//实现Runnable接口
public class HttpServer implements Runnable {
    private static Map<String, String> userData;
    private Socket socket;

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
                if (url.equals("/login")) {
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
                    }
                }
                else{
                    int statusCode=Integer.parseInt(url.substring(1));
                    switch(statusCode){
                        case 301:
                            //TODO
                            //写自己的方法
                            break;
                        case 302:
                            //TODO
                            //写自己的方法
                            break;
                        case 304:
                            //TODO
                            //写自己的方法
                            break;
                        case 404:
                            //TODO
                            //写自己的方法
                            break;
                        case 405:
                            //TODO
                            //写自己的方法
                            break;
                        case 500:
                            //TODO
                            //写自己的方法
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
        //writer.println("ContentLength: "+new File("login.html").length());
        writer.println("Host: localhost");
        writer.println("Method: " + method);
        writer.println();//这个就是写空行，为了和内容相区分开
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
    private boolean checkNameAndPwd(String str){
        return !str.contains("=") && !str.contains("&");
    }
    //参数就传run里面的writer和outputStream,一个负责响应头，一个负责响应内容
    // 别忘了两个最后要flush以及writer最后flush前还要写进一行空行（重要），具体写法参见run内部和responseHeadAction内容
    private void Status_301(PrintWriter writer,BufferedOutputStream outputStream){}
    private void Status_302(PrintWriter writer,BufferedOutputStream outputStream){}
    private void Status_304(PrintWriter writer,BufferedOutputStream outputStream){}
    private void Status_404(PrintWriter writer,BufferedOutputStream outputStream){}
    private void Status_405(PrintWriter writer,BufferedOutputStream outputStream){}
    private void Status_500(PrintWriter writer,BufferedOutputStream outputStream){}

}
