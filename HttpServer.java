import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;
import java.util.Map;
import java.util.StringTokenizer;

//实现Runnable接口
public class HttpServer implements Runnable{
    private Socket socket;
    private static Map<String,String> usrData;
//    static int DEFAULT_PORT=8080;
    public HttpServer(Socket s){socket = s;}

    public static void main(String[] args) {
        int port;
        ServerSocket serverSocket;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("port=8080 (default)");
            port = 8080;
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is listening for a connection on port: "+port+"......");
            while(true){//通过死循环建立长连接，不断监听客户端传来的消息
                Socket client = serverSocket.accept();

                HttpServer httpServer = new HttpServer(client);
                Thread thread = new Thread(httpServer);//为连接的客户端开一个线程
                thread.start();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
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
            try {
                String firstLine = bufferedReader.readLine();
                StringTokenizer tokenizer = new StringTokenizer(firstLine);
                //获取方法名
                String Method = tokenizer.nextToken().toUpperCase();
                //获取url
                String url = tokenizer.nextToken().toLowerCase();
                //该服务器只支持GET和POST方法
                int port = this.socket.getLocalPort();
                if (Method.equals("GET")) {
                    //向客户端传送header
                    writer.println("HTTP/1.1 200 OK");
                    writer.println("Server: HttpServer");
                    writer.println("Accept: */*");
                    writer.println("Accept-language: zh-cn");
                    writer.println("Connection: keep-alive");
                    writer.println("ContentType: text/html");
                    writer.println("ContentLength: "+new File("test.html").length());
                    writer.println("Host: localhost");
                    writer.println();
                    writer.flush();

                    byte[] data = readFile("test.html");
                    outputStream.write(data);

                    outputStream.flush();
                } else if (Method.equals("POST")) {
                    //TODO
                    writer.println("HTTP/1.1 200 OK");
                    writer.println("Server: HttpServer");
                    writer.println("Accept: */*");
                    writer.println("Accept-language: zh-cn");
                    writer.println("Connection: keep-alive");
                    writer.println("Host: localhost");
                    writer.println();
                    writer.flush();

                    String data = bufferedReader.readLine();
                    while(!data.equals("")&&data!=null){
                        data=bufferedReader.readLine();
                    }
                    char[] source = new char[100];
                    bufferedReader.read(source);
                    System.out.println(source);
                    outputStream.write("Register Success".getBytes());






                } else {
                    System.out.println("Method not supported.");
                }
            }catch(NullPointerException npe){
                npe.getMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
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
    //读取文件，转成字节码
    private byte[] readFile(String f) throws FileNotFoundException {
        File file=new File(f);
        int length = new Long(file.length()).intValue();
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] res = new byte[length];
        try {
            fileInputStream.read(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;

    }
}
