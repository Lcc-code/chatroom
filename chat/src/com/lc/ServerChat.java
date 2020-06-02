package com.lc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class ServerChat extends JFrame {
    //
    JTextArea serverTA = new JTextArea();
    //文本区加滚动条
    private JScrollPane serverSP = new JScrollPane(serverTA);
    //按钮控制
    private JPanel btnTool = new JPanel();
    private JButton startBt = new JButton("启动");
    private JButton stopBt = new JButton("关闭");

    //服务器Socket
    private static ServerSocket serverSocket = null;

    private Socket socket = null;
    //服务器固定端口号
    private static final int PORT = 8888;
    private static final String CONNSTR = "127.0.0.1";

    //内部连接class的容器
    private ArrayList<ClientConn> connArrayList = new ArrayList<>();

    //服务器启动标志
    private boolean isStart = false;

    //用户信息存储文件
    public static final String FILENAME = "userInfo.properties";

    //构造或者初始。
    public ServerChat() throws HeadlessException {
        this.setTitle("服务器端");
        //服务器窗口配置
        //放置区域，按钮
        this.add(serverSP, BorderLayout.CENTER);

        btnTool.add(startBt);
        btnTool.add(stopBt);
        this.add(btnTool, BorderLayout.SOUTH);
        //设置大小
        this.setBounds(0,0,500,500);
        /*if (isStart){
            serverTA.append("服务器已经启动！" + "\n");
        }else serverTA.append("服务器未启动" + "\n");
        */


        stopBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isStart = false;
                if (socket != null){
                    try {
                        socket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                if (serverSocket != null){
                    try {
                        serverSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                serverTA.append("服务器关闭");
                System.exit(0);
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isStart = false;
                if (socket != null){
                    try {
                        socket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                if (serverSocket != null){
                    try {
                        serverSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                serverTA.append("服务器关闭");
                System.exit(0);
            }
        });

        startBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket == null){
                        serverSocket = new ServerSocket(PORT);
                    }
                    isStart = true;
                    serverTA.append("服务器已经启动！" + "\n");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }





            }
        });
        serverTA.setEditable(false);
        this.setVisible(true);
        startServer();


    }
    //服务器启动的方法
    public void startServer(){

        //当点击服务器启动时，while接收连接
        try {
            if (serverSocket == null){
                serverSocket = new ServerSocket(PORT);
            }
            isStart = true;
            while (isStart){
                System.out.println("ssss");
                //接收
                socket = serverSocket.accept();
                System.out.println("一个客户端连接服务器" + socket.getInetAddress() +
                        "/" + socket.getPort());
                serverTA.append("一个客户端连接服务器" + socket.getInetAddress() +
                        "/" + socket.getPort() + "\n");
                connArrayList.add(new ClientConn(socket));

            }
        }catch (SocketException e){
            System.out.println("服务器下线了");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //搞一个内部类作为服务器端的连接对象
    class ClientConn implements Runnable{
        Socket socket = null;

        public ClientConn(Socket socket) {
            this.socket = socket;
            //把线程启动加入构造方法中
            (new Thread(this)).start();

        }

        //同时接收客户端信息
        //将接收信息的方法写到线程方法里
        @Override
        public void run() {
            //创建输入字节
            try {
                DataInputStream dIS = new DataInputStream(socket.getInputStream());
                //持续接收
                while (isStart){
                    String str = dIS.readUTF();
                    System.out.println(str);
                    serverTA.append( socket.getInetAddress() + ":" +
                            socket.getPort() + ":" + str + "\n");
                    String string1 = socket.getInetAddress() + ":" + str + "\n";
                    //遍历集合，调用send方法,在客户端接收信息多线程接收
                    Iterator<ClientConn> iterator = connArrayList.iterator();
                    while (iterator.hasNext()){
                        ClientConn clientConn = iterator.next();
                        clientConn.send(string1);
                    }


                }


            } catch (SocketException e){
                System.out.println("一个客户端下线");
                serverTA.append("一个客户端下线" +
                        socket.getInetAddress() + ":" +
                        socket.getPort() + "\n");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        //每个连接对象发送数据的方法
        public void  send(String string){
            try {
                DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
                dos.writeUTF(string);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }






    public static void main(String[] args) throws IOException {

        ServerChat serverChat = new ServerChat();





    }
}
