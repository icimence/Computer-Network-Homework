import com.sun.xml.internal.bind.v2.runtime.output.StAXExStreamWriterOutput;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.DoubleToIntFunction;

//实现Runnable接口
public class HttpServer implements Runnable{
    private Socket socket;
    private static Map<String,String> usrData;
    static final int DEFAULT_PORT=8080;
    //构造方法
    public HttpServer(Socket s){socket = s;}

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("Server is listening for a connection on port: "+DEFAULT_PORT+"......");
            while(true){//不断监听客户端传来的消息，此过程中server始终开启
                Socket client = serverSocket.accept();//接收客户端的连接
                System.out.println("Server connected.");
                HttpServer httpServer = new HttpServer(client);
                Thread thread = new Thread(httpServer);//为连接的客户端开一个线程
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run(){
        //在这里处理client端发来的request
        BufferedReader bufferedReader=null;
        PrintWriter writer=null;
        BufferedOutputStream outputStream=null;
        try {
            //处理输入流
            bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //处理发送给客户端的输出流
            //header部分
            writer = new PrintWriter(socket.getOutputStream());
            //数据部分
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            //获取第一行
            String firstLine = bufferedReader.readLine();
            StringTokenizer tokenizer = new StringTokenizer(firstLine);
            //获取方法名
            String Method = tokenizer.nextToken().toUpperCase();
            //获取url
            String url = tokenizer.nextToken().toLowerCase();
            //该服务器只支持GET和POST方法
            if(Method.equals("GET")){
                //向客户端传送header
                writer.println("HTTP/1.1 200 OK");
                writer.println("Server: HttpServer");
                writer.println("Accept: */*");
                writer.println("Accept-language: zh-cn");
                writer.println("Connection: keep-alive");
                writer.println("Host: localhost");
                writer.println();
                writer.flush();
                //额...暂时没写数据
                outputStream.flush();
            }
            else if(Method.equals("POST")){
                //TODO
            }
            else{
                System.out.println("Method not supported.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                bufferedReader.close();
                writer.close();
                outputStream.close();
                socket.close();
                System.out.println("Connection closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
}
