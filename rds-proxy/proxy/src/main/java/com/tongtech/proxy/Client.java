package com.tongtech.proxy;

import com.tongtech.proxy.core.cli.*;
import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    private static volatile String PROMPT = "$ > ";

    private static String host = "localhost";
    private static int port = 6379;
    private static boolean is_secure = false;
    private static boolean redis_wrapper = false;
    private static boolean cluster = false;
    private static String username = null;
    private static String passwd = null;
    private static volatile Thread[] SocketReader = null;

    private static final Socket[] ClusterSlots = new Socket[16 * 1024];
    private static volatile String lastSended;
    private static final ConcurrentHashMap<Socket, Object> Sockets = new ConcurrentHashMap<>();

    private static final HashSet<String> AllNodesCmd = new HashSet<String>() {
        {
            this.add("keys");
            this.add("flushdb");
            this.add("flushall");
            this.add("dbsize");
        }
    };

    private static final HashSet<String> UnExcapteCommands = new HashSet<String>() {
        {
            this.add("info");
            this.add("cluster");
        }
    };

    private static final HashSet<String> FirstNodesCmd = new HashSet<String>() {
        {
            this.add("script");
        }
    };

    private static volatile boolean NeedEscape = true;

    private static void Usage() {
        System.out.println("\nClient [<-h|--host> host] [<-p|--port> port] <-s|--secure> <-r|--redis> <-c|--cluster> [<-u|--user> username] [-a password] [command]\n");
        System.out.println("For example:\n");
        System.out.println("Client.sh -r -p 6379\n");
        System.out.println("    or:\n");
        System.out.println("Client.sh -r -p 6379 -a 123456 set key1 value1\n");
        System.exit(1);
    }

    public static void main(String[] argv) throws Exception {

//        Socket socket1=createConnection("localhost",6379);
//        PackageInputStream pis1 = new PackageInputStream(socket1.getInputStream());
//        sendMessage(socket1,"cluster slots");
//        Object o1=Protocol.read(pis1);
//
//
//        host = "192.168.0.90";
//        port = 6379;
//        redis_wrapper = true;
//        cluster = true;

//        argv = new String[]{"get", "aaa"};

        List<String> commands = new ArrayList<>();

        for (int i = 0; i < argv.length; i++) {
            if ("-h".equalsIgnoreCase(argv[i])
                    || "--host".equalsIgnoreCase(argv[i])) {
                if (i + 1 < argv.length) {
                    host = argv[++i];
                } else {
                    System.out.println("invalid parameter -h");
                    Usage();
                }
            } else if ("-p".equalsIgnoreCase(argv[i])
                    || "--port".equalsIgnoreCase(argv[i])) {
                try {
                    port = Integer.parseInt(argv[++i]);
                } catch (Exception e) {
                    System.out.println("invalid parameter -p: " + e.getMessage());
                    Usage();
                }
            } else if ("-u".equalsIgnoreCase(argv[i])
                    || "--user".equalsIgnoreCase(argv[i])) {
                if (i + 1 < argv.length) {
                    username = argv[++i];
                } else {
                    System.out.println("invalid parameter -u");
                    Usage();
                }
            } else if ("-a".equalsIgnoreCase(argv[i])) {
                if (i + 1 < argv.length) {
                    passwd = argv[++i];
                } else {
                    System.out.println("invalid parameter -a");
                    Usage();
                }
            } else if ("-s".equalsIgnoreCase(argv[i])
                    || "--secure".equalsIgnoreCase(argv[i])) {
                is_secure = true;
            } else if ("-r".equalsIgnoreCase(argv[i])
                    || "--redis".equalsIgnoreCase(argv[i])) {
                redis_wrapper = true;
            } else if ("-c".equalsIgnoreCase(argv[i])
                    || "--cluster".equalsIgnoreCase(argv[i])) {
                redis_wrapper = true;
                cluster = true;
            } else if ("--help".equalsIgnoreCase(argv[i])) {
                Usage();
            } else {
                if (!argv[i].startsWith("-")) {
                    commands.add(argv[i]);
                } else {
                    System.out.println("Unknow parament: " + argv[i]);
                    Usage();
                }
            }
        }

        final Socket socket = createConnection(host, port);
        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
        PROMPT = address.getHostString() + ":" + address.getPort() + " > ";

        // 命令行模式
        if (commands.size() > 0) {
            OutputStream os = socket.getOutputStream();
            PackageInputStream pis = new PackageInputStream(socket.getInputStream());
            if (redis_wrapper) {
                sendMessage(socket, commands);
                Object resp = Protocol.read(pis);
                if (resp instanceof ErrorString) {
                    String msg = resp.toString();
                    if (msg.startsWith("MOVED")) {
                        // cluster data moved
                        String[] args = msg.split(" ");
                        String[] h_p = args[2].split(":");
                        int p = Integer.parseInt(h_p[1]);
                        Socket movedSocket = createConnection(h_p[0], p);
                        sendMessage(movedSocket, commands);
                        pis = new PackageInputStream(movedSocket.getInputStream());
                        resp = Protocol.read(pis);
                    }
                }
                printResp(resp, false, null);
            } else {
                for (String s : commands) {
                    os.write(s.getBytes(StandardCharsets.UTF_8));
                    os.write(' ');
                }
                os.write("\r\n".getBytes(StandardCharsets.UTF_8));
                String resp = pis.readLine();
                System.out.println(resp);
            }
            socket.close();
            System.exit(0);
        }

        if (is_secure) {
            System.out.println("secure connection.");
        }
        if (redis_wrapper) {
            System.out.println("Redis protocol enabled.");
        } else {
            System.out.println("Command line protocol enabled.");
        }

        final Socket finalSocket;
        if (cluster) {
            initClusterNodes(socket);
            finalSocket = ClusterSlots[0];
            System.out.println("\nCluster connected.");
            System.out.print(PROMPT);
            System.out.flush();
        } else {
            Thread reader = new ServerReader(socket);
            reader.setDaemon(true);
            reader.start();
            finalSocket = socket;
            System.out.println("Node connection.");
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String msg = null;
        try {
            while ((msg = input.readLine()) != null) {
                System.out.print(PROMPT);
                System.out.flush();
//                if (redis_wrapper) {
//                    List<String> data = splitString(msg);
//                    StringBuilder buf = new StringBuilder();
//                    buf.append('*').append(data.size()).append("\r\n");
//                    for (String s : data) {
//                        int len = -1;
//                        if (s != null) {
//                            if (s.length() == 0) {
//                                len = 0;
//                            } else {
//                                len = s.getBytes(StandardCharsets.UTF_8).length;
//                            }
//                        }
//                        buf.append('$').append(len).append("\r\n");
//                        if (len >= 0) {
//                            buf.append(s).append("\r\n");
//                        }
//                    }
//                    msg = buf.toString();
//                } else {
//                    msg = msg + "\n";
//                }
//                socket.getOutputStream().write(msg.getBytes());
                sendMessage(finalSocket, msg);
            }
        } catch (Exception e) {
        } finally {
            try {
                input.close();
            } catch (Throwable t) {
            }
        }
        System.out.println("Connection is broken.\n");
        System.out.flush();
        System.exit(0);
    }

    /**
     * Parses command line arguments into a Map.
     * Supports options like -h, --host, etc.
     *
     * @param args The command line arguments.
     * @return A Map containing the parsed options and their values.
     */
    public static Map<String, String> parseArgv(String[] args) {
        Map<String, String> params = new LinkedHashMap<>();
        String optionName;

        optionName = null;
        for (String arg : args) {
            // Check if the argument is an option (starts with '-' or '--')
            if (arg.startsWith("--")) {
                // Short option
                optionName = arg.substring(2);
            } else if (arg.startsWith("-")) {
                // Long option
                optionName = arg.substring(1);
            } else if (optionName != null) {
                // Argument value
                params.put(optionName, arg);
                optionName = null;
            }
        }

        return params;
    }

    private static final Vector<String> splitString1(String src) {
        Vector<String> res = new Vector<>(1);
        if (src.equals(null) || src.equals("")){
            return res;
        }
        //用来存取到的字符
        char c1;
        //定义一个变量来储存分隔符
        char sep = ' ';
        //用来临时存储取到的字符
        String str = new String();
        //判断是否有单双引号
        boolean flagdan = false;
        boolean flagshuang = false;
        for (int i = 0;i<src.length();i++){
            c1 = src.charAt(i);
            if (c1 == '\'' && flagshuang == false){
                flagdan = !flagdan;
            } else if (c1 == '"' && flagdan == false) {
                flagshuang = !flagshuang;
            }

            if (c1 == sep){
                //判断这个空格是否在单双引号内
                if (flagdan == true || flagshuang == true){
                    str += Character.toString(c1);
                }else {
                    res.add(str);
                    str = "";
                }
            }else {
                str += Character.toString(c1);
            }
        }
        res.add(str);
//        System.out.println(res);

        return res;
    }

    private static final Vector<String> splitString(String src) {
        Vector<String> res = new Vector<>(1);
        if (src.equals(null) || src.equals("")){
            return res;
        }
        //用来存取到的字符
        char c1;
        //定义一个变量来储存分隔符
        char sep = ' ';
        //用来临时存储取到的字符
        String str = new String();
        //判断是否有单双引号
        boolean flagdan = false;
        boolean flagshuang = false;
        for (int i = 0;i<src.length();i++){
            c1 = src.charAt(i);
            if (c1 == '\'' && flagshuang == false){
                flagdan = !flagdan;
            } else if (c1 == '"' && flagdan == false) {
                flagshuang = !flagshuang;
            }

            if (c1 == sep){
                //判断这个空格是否在单双引号内
                if (flagdan == true || flagshuang == true){
                    str += Character.toString(c1);
                }else {
                    res.add(str);
                    str = "";
                }
            }else {
                str += Character.toString(c1);
            }
        }
        res.add(str);

        return res;
    }

    /**
     * 创建指定连接
     *
     * @param host
     * @param port
     * @return
     * @throws IOException
     */
    private static Socket createConnection(String host, int port) {
        Socket socket = null;
        try {
            if (is_secure) {
                SSLContext ssc = BogusSSLContextFactory.getInstance(false);
                SSLSocket sslSocket = (SSLSocket) ssc.getSocketFactory().createSocket();
                sslSocket.setTcpNoDelay(true);
                sslSocket.setSoTimeout(0);
                sslSocket.connect(new InetSocketAddress(host, port), 2000);
                sslSocket.startHandshake();
                socket = sslSocket;
            } else {
                socket = new Socket();
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(0);
                socket.connect(new InetSocketAddress(host, port), 2000);
            }
        } catch (Exception e) {
            System.err.println("Error occur when connect to "
                    + host + ":" + port + "  : " + e.getMessage());
            try {
                socket.close();
            } catch (Exception e2) {
            }
            Usage();
        }

        if (passwd != null) {
            try {
                if (redis_wrapper) {
                    if (username != null) {
                        byte[] user = username.getBytes(StandardCharsets.UTF_8);
                        byte[] pass = passwd.getBytes(StandardCharsets.UTF_8);
                        socket.getOutputStream().write(("*3\r\n$4\r\nauth\r\n$" + user.length + "\r\n" + username + "\r\n$" + pass.length + "\r\n" + passwd + "\r\n").getBytes(StandardCharsets.UTF_8));
                    } else {
                        byte[] pass = passwd.getBytes(StandardCharsets.UTF_8);
                        socket.getOutputStream().write(("*2\r\n$4\r\nauth\r\n$" + pass.length + "\r\n" + passwd + "\r\n").getBytes(StandardCharsets.UTF_8));
                    }
                    socket.getOutputStream().flush();
                    PackageInputStream pis = new PackageInputStream(socket.getInputStream());
                    Object o = Protocol.read(pis);
                    if (o instanceof ErrorString) {
                        throw new IOException(o.toString());
                    }
                } else {
                    if (username != null) {
                        socket.getOutputStream().write(("auth " + username + " " + passwd + "\r\n").getBytes(StandardCharsets.UTF_8));
                    } else {
                        socket.getOutputStream().write(("auth " + passwd + "\r\n").getBytes(StandardCharsets.UTF_8));
                    }
                    socket.getOutputStream().flush();
                }
            } catch (Exception e) {
                System.err.println("Error occur when connect to "
                        + host + ":" + port + "  : " + e.getMessage());
                try {
                    socket.close();
                } catch (Exception e2) {
                }
                Usage();
            }
        }

        return socket;
    }

    private static synchronized void initClusterNodes(Socket socket) throws IOException {
        PackageInputStream pis = new PackageInputStream(socket.getInputStream());
        sendMessage(socket, "cluster slots");
        Object o = Protocol.read(pis);
        if (o instanceof List) {
            List<List> slots = (List) o;
            for (List slot : slots) {
                int start = ((Long) slot.get(0)).intValue();
                int stop = ((Long) slot.get(1)).intValue();
                List master = (List) slot.get(2);
                String h = ((BulkString) master.get(0)).getString();
                int p = ((Long) master.get(1)).intValue();
                Socket s = createConnection(h, p);
                Thread reader = new ServerReader(s);
                reader.setDaemon(true);
                reader.start();
                for (int i = start; i < stop; ++i) {
                    ClusterSlots[i] = s;
                }
                Sockets.put(s, s);
                System.out.println("Load cluster master node " + s + " ok.");
            }
        } else {
            System.out.println("\nError occur when init cluster: " + o + "\n");
            System.exit(3);
        }
    }

    /**
     * 命令行模式调用的发送函数
     *
     * @param socket
     * @param commands
     * @throws IOException
     */
    private static void sendMessage(Socket socket, List<String> commands) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(("*" + commands.size() + "\r\n").getBytes(StandardCharsets.UTF_8));
        for (String s : commands) {
            byte[] b = s.getBytes(StandardCharsets.UTF_8);
            os.write(("$" + b.length + "\r\n").getBytes(StandardCharsets.UTF_8));
            os.write(b);
            os.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 交互模式调用的发送函数
     *
     * @param socket
     * @param msg
     * @throws IOException
     */
    private static void sendMessage(Socket socket, String msg) throws IOException {
        lastSended = msg;
        boolean isClustercmd = false;
        boolean isFirstCmd = false;
        List<String> data = splitString(msg.trim());
        if (data.size() > 0) {
            String cmd = data.get(0).toLowerCase(Locale.ROOT);
            if (AllNodesCmd.contains(cmd)) {
                isClustercmd = true;
            }
            if (FirstNodesCmd.contains(cmd)) {
                isFirstCmd = true;
            }
            if (UnExcapteCommands.contains(cmd)) {
                NeedEscape = false;
            } else {
                NeedEscape = true;
            }
        }
        if (redis_wrapper) {
            StringBuilder buf = new StringBuilder();
            buf.append('*').append(data.size()).append("\r\n");
            for (String s : data) {
                int len = -1;
                if (s != null) {
                    if (s.length() == 0) {
                        len = 0;
                    } else {
                        len = s.getBytes(StandardCharsets.UTF_8).length;
                    }
                }
                buf.append('$').append(len).append("\r\n");
                if (len >= 0) {
                    buf.append(s).append("\r\n");
                }
            }
            msg = buf.toString();
        } else {
            if (msg != null) {
                if (msg.indexOf("\\r") >= 0) {
                    msg = msg.replace("\\r", "\r");
                }
                msg = msg.trim() + "\n";
            } else {
                msg = "\\n";
            }
        }
        if (cluster && isClustercmd) {
            for (Socket s : Sockets.keySet()) {
                s.getOutputStream().write(msg.getBytes());
            }
        } else if (cluster && isFirstCmd) {
            ClusterSlots[0].getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));
        } else {
            socket.getOutputStream().write(msg.getBytes());
            InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
            PROMPT = address.getHostString() + ":" + address.getPort() + " > ";
        }
    }

    private static synchronized void printResp(Object resp, boolean prompt, String head) {
        if (head != null) {
            System.out.print(head);
        }
        if (resp == null) {
            System.out.println("(nil)");
        } else if (resp instanceof SimpleString) {
            System.out.println(resp);
        } else if (resp instanceof ErrorString) {
            System.out.println("(error) " + resp);
        } else if (resp instanceof Long) {
            System.out.println("(integer) " + resp);
        } else if (resp instanceof List) {
            List list = (List) resp;
            if (list.size() == 0) {
                System.out.println("(nil)");
                if (prompt) {
                    System.out.print(PROMPT);
                }
                return;
            } else {
                StringBuilder buf = new StringBuilder();
                int len = head != null ? head.length() : 0;
                for (int i = 0; i < len; ++i) {
                    buf.append(' ');
                }
                buf.append(' ');
                for (int i = 0; i < list.size(); ++i) {
                    if (i == 0) {
                        printResp(list.get(i), prompt, " " + (i + 1) + ") ");
                    } else {
                        printResp(list.get(i), prompt, buf.toString() + (i + 1) + ") ");
                    }
                }
            }
            return;
        } else/* if (resp instanceof String)*/ {
            if (resp instanceof BulkString) {
                ((BulkString) resp).setEscape(NeedEscape);
            }
            String str = resp.toString();

            // 每一个回车后面加上提示符
            str = str.replace("\n", "\n" + PROMPT);

            System.out.println(str);
        }

        if (prompt) {
            System.out.print(PROMPT);
        }
    }

    static class ServerReader extends Thread {
        private final Socket socket;

        public ServerReader(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                PackageInputStream pis = new PackageInputStream(socket.getInputStream());
                if (!cluster) {
                    System.out.println("\nClient is connected to "
                            + ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString()
                            + " : "
                            + ((InetSocketAddress) socket.getRemoteSocketAddress()).getPort());
                    System.out.print(PROMPT);
                    System.out.flush();
                }
                if (redis_wrapper) {
                    while (true) {
                        Object resp = Protocol.read(pis);

                        // cluster data moved
                        if (resp instanceof ErrorString) {
                            String msg = resp.toString();
                            if (msg.startsWith("MOVED")) {
                                // cluster data moved
                                String[] args = msg.split(" ");
                                int slot = Integer.parseInt(args[1]);
                                Socket movedSocket = ClusterSlots[slot];
                                if (movedSocket != null && lastSended != null) {
                                    sendMessage(movedSocket, lastSended);
                                    lastSended = null;
                                    continue;
                                }
                            }
                        }

                        printResp(resp, true, null);
//                            System.out.print(PROMPT);
                        System.out.flush();
                    }
                } else {
                    String msg = null;
                    while ((msg = pis.readLine1()) != null) {
                        System.out.println(msg);
                        System.out.print(PROMPT);
                        System.out.flush();
                    }
                }
            } catch (IOException e) {
//                e.printStackTrace();
            } finally {
                try {
                    this.socket.close();
                } catch (Throwable t) {
                }
            }
            System.out.println("Connection is closed by remote.\n");
            System.out.flush();
            System.exit(0);
        }
    }
}
