

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private static Map<String,Integer> u_p= new HashMap<>();


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
            System.out.println("server is running on port:" + serverSocket.getLocalPort());
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    System.out.println("biuld a new tcp link with client,the cient address:" +
                            socket.getInetAddress() + ":" + socket.getPort());
                    service(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static void service(Socket socket) throws Exception {
        //read http information
        InputStream socketIn = socket.getInputStream();
        Thread.sleep(500);
        int size = socketIn.available();
        byte[] buffer = new byte[size];
        socketIn.read(buffer);
        String request = new String(buffer);
        System.out.println(request);

        //
        //get http request first line
        String firstLineOfRequest = request.substring(0, request.indexOf("\r\n"));
        String info = firstLineOfRequest.substring(firstLineOfRequest.indexOf(" /"),firstLineOfRequest.indexOf(" H"));
        firstLineOfRequest = firstLineOfRequest.substring(0,firstLineOfRequest.indexOf(" /"))+firstLineOfRequest.substring(firstLineOfRequest.indexOf(" H"));
        String name = info.substring(info.indexOf("=")+1,info.indexOf("&"));
        int pwd = Integer.parseInt(info.substring(info.lastIndexOf("=")+1));
        u_p.put(name,pwd);
        String[] parts = firstLineOfRequest.split(" ");
        String uri = parts[1];
        //mime
        String contentType;
        if (uri.indexOf("html") != -1 || uri.indexOf("htm") != -1)
            contentType = "text/html";
        else {
            contentType = "application/octet-stream";
        }
        //create http response
        //the first line
        String responseFirstLine = "HTTP/1.1 200 OK\r\n";
        //http response head
        String responseHeade = "Content-Type:" + contentType + "\r\n";
        //send http response result
        OutputStream socketOut = socket.getOutputStream();
        //send http response first line
        socketOut.write(responseFirstLine.getBytes());
        //send http response heade
        socketOut.write(responseHeade.getBytes());

        InputStream in = HttpServer.class.getResourceAsStream("root/" + uri);
        //send content
        socketOut.write("\r\n".getBytes());
        int len = 0;
        buffer = new byte[128];
        if (in != null) {
            while ((len = in.read(buffer)) != -1) {
                System.out.println(new String(buffer));
                socketOut.write(buffer, 0, len);
            }
        }

        Thread.sleep(1000);
        //close tcp link
        socket.close();
    }

}
