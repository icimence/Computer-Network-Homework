

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 重定向服务器
 * @author zzj
 * @date Jul 15, 2016 10:58:15 AM
 */
public class RedirectorServer {

    private int port;
    private String newSite;

    public static void main(String[] args) {
        new RedirectorServer(203, "http//:www.baidu.com").start();
    }

    public void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ServerSocket socketServer = null;
        try {
            socketServer = new ServerSocket(port);
            while (true) {
                try {
                    Socket connection = socketServer.accept();
                    executorService.submit(new RedirectorThread(connection));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * @param port
     * @param newSite
     */
    public RedirectorServer(int port, String newSite) {
        super();
        this.port = port;
        this.newSite = newSite;
    }

    class RedirectorThread implements Callable<Boolean> {
        Socket connection;

        /**
         * @param connection
         */
        public RedirectorThread(Socket connection) {
            this.connection = connection;
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public Boolean call() throws Exception {
            try {
                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "US-ASCII"));
                Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //读取第一行
                //只读取第一行 包含请求的方法 请求的资源 协议等
                StringBuilder request = new StringBuilder();
                while (true) {
                    int c = reader.read();
                    if (c == '\r' || c == '\n' || c == -1) {
                        break;
                    }
                    request.append((char) c);
                }
                System.out.println(request.toString());
                String getString = request.toString();
                String[] pieces = getString.split("\\w");
                String theFile = pieces[1];
                if (request.toString().indexOf("HTTP") != -1) {
                    writer.write("HTTP1.0 302 FOUND\r\n");
                    Date date = new Date();
                    writer.write("Date:" + date + "\r\n");
                    writer.write("Server:Redirector 1.1\r\n");
                    writer.write("Location:" + newSite + theFile + "\r\n");
                    writer.write("Content-type:text/html\r\n\r\n");
                    writer.flush();
                }
                //并不是所有服务器都支持重定向，所以下面给出手动跳转的操作
                StringBuilder html = new StringBuilder();
                html.append("<html><head><title>Document Moved</title></head>");
                html.append("<body>");
                html.append("The site has moved,please update your bookmarket,and click the hyper link <a href='" + newSite + "'>" + theFile + "</a>!");
                html.append("</body></html>");
                writer.write(html.toString());
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

    }
}













