package com.lc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

/*
    客户端
* */
public class ClientChat extends JFrame {

    //声明一个显示纯文本的多行区域
    private JTextArea tA = new JTextArea(10,20);
    //编辑单行文本框
    private JTextField tF = new JTextField(20);

    //文本区加滚动条
    private JScrollPane sP = new JScrollPane(tA);

    //默认名字
    private static final String  TITLE = "reboot";

    //
    private static final String CONNSTR = "127.0.0.1";
    //使用8888端口
    private static final int PORT = 8888;
    private  Socket socket = null;

    private boolean isConn = false;
    private boolean isRight = false;

    private DataOutputStream oS = null;
    private JLabel jBP = new JLabel();
    private static final String CONFIG = "config";



    //构建初始化方法
    public ClientChat( ) throws HeadlessException {
        ClientLogin clientLogin = new ClientLogin();

        clientLogin.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                init(clientLogin.getUser());
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }
            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        //当聊天窗口关闭，
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                logout(clientLogin.getUser());
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

    }
    private void init(String user){

        this.setTitle(user + "的窗口");
        //将文本区域添加到中间位置
        //this.add(tA, BorderLayout.CENTER);
        //将文本框添加
        this.add(tF,BorderLayout.SOUTH);
        //设置区域大小
        this.setBounds(300,300,400,400);
        //设置显示框区域无法输入
        tA.setEditable(false);
        //设置光标聚焦
        tF.requestFocus(true);
        //加入滚动条和文本区域
        this.add(sP,BorderLayout.CENTER);
        //读取path,
        jBP.setIcon(new ImageIcon(readPath(CONFIG,"BackgroundPicture2")));
        this.add(jBP,BorderLayout.NORTH);

        //TODO 将连接封装
        this.setVisible(true);

        (new Thread(new Receive())).start();

        /*
            文本框输入事件监听
                将文本框输入至文本区域tA
        * */
        tF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String strSend = tF.getText();
                if (strSend.trim().length() == 0){
                    return;
                }
                //上传\
                send(user,strSend);

                //判断输入字符长度

                tF.setText(" ");

            }
        });

        connect();
        //关闭操作设置
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
    public void connect(){
        try {

            socket = new Socket(CONNSTR,PORT);
            //连上服务器
            isConn = true;
        }catch (ConnectException e){
            tA.append("服务器下线没开着");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //发送消息到服务器
    public void send(String user,String string){
        try {
            //创建输出字节流
            oS = new DataOutputStream(socket.getOutputStream());
            //写入字节
            oS.writeUTF( user+ ":" + string);
            oS.flush();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //多线程的类，实现Runnable接口
    class Receive implements Runnable{
        @Override
        public void run(){
            try {
                while (isConn) {
                    DataInputStream dIS = new DataInputStream(socket.getInputStream());
                    //接收信息
                    String string = dIS.readUTF();

                    tA.append(string);
                    }
                }catch (SocketException e){
                System.out.println("服务器中断");
                }
                catch (EOFException e){
                System.out.println("服务器中断");
                tA.append("服务器中断！");
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
    public String readPath(String s,String key){
        //使用资源绑定器,获取config配置
        ResourceBundle bundle = ResourceBundle.getBundle(s);
        return Thread.currentThread().getContextClassLoader()
                .getResource(bundle.getString(key))
                .getPath();

    }
    private void logout(String name){
        System.out.println(name);
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        //从配置文件获取url，用户,密码，驱动
        ResourceBundle bundle = ResourceBundle.getBundle(CONFIG);
        String driver = bundle.getString("driver");
        String url = bundle.getString("url");
        String dataUser = bundle.getString("dateUser");
        String datePassword = bundle.getString("datePassword");
        try {
            //建立连接
            Class.forName(driver);
            connection = DriverManager.getConnection(url, dataUser, datePassword);
            statement = connection.createStatement();
            String sqlSelect = "select name, password, status from t_user";
            resultSet = statement.executeQuery(sqlSelect);
            //递归判断是否正确
            String sqlSetStatus = "update t_user set status = 0 where name =" + "'" + name+"'";
            statement.executeUpdate(sqlSetStatus);
            isRight = false;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (resultSet != null){
                try {
                    resultSet.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (statement != null){
                try {
                    statement.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }

    }
    //内部登录类
    //TODO 将登录认证变成访问数据库认证。
    private class ClientLogin extends JFrame{
        private JButton loginBt = new JButton("登录");
        private JButton stopBt = new JButton("关闭");

        //账号输入框
        private JTextField uTF = new JTextField(20);
        //密码输入框
        private JPasswordField pTF1 = new JPasswordField(20);
        JLabel interestLabel = new JLabel("账   号:");
        JLabel interestLabe2 = new JLabel("密   码：");
        private JTextField jTextField1 = new JTextField();
        private JTextField jTextField2 = new JTextField();
        private JPanel jPanel = new JPanel();





        private String user = null;
        private String passwd = null;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(String passwd) {
            this.passwd = passwd;
        }

        private JLabel jBP = new JLabel();


        public ClientLogin() throws HeadlessException {

            init();
            //监听
            loginBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setUser(jTextField1.getText());
                    setPasswd(jTextField2.getText());

                    if (passwd == null || user ==null) {
                        System.out.println("输入不能为空");
                        return;
                    }

                    login(user,passwd);

                    System.out.println("用户名：" + user + ":" + passwd);
                }
            });
            stopBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

        }
        //初始化内部类
        private void init(){
            this.setTitle("登录界面");
            this.setBounds(0,0,500,500);

            //读取path,
            jBP.setIcon(new ImageIcon(readPath(CONFIG,"BackgroundPicture")));
            this.add(jBP,BorderLayout.NORTH);


            //面板对象

            jPanel.setLayout(null);
            //设置弹出位置，及大小


            this.add(jPanel);

            loginBt.setBounds(150,130,60,20);
            stopBt.setBounds(220,130,60,20);

            //放置
            jPanel.add(loginBt);
            jPanel.add(stopBt);

            jTextField1.setBounds(160,10,150,25);
            interestLabel.setBounds(100,10,50,25);

            jTextField2.setBounds(160,40,150,25);
            interestLabe2.setBounds(100,40,50,25);



            jPanel.add(jTextField1);
            jPanel.add(interestLabel);

            jPanel.add(jTextField2);
            jPanel.add(interestLabe2);

            jPanel.setVisible(true);

            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setVisible(true);






        }
        private void readPassword(String name,String passwd){
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            //从配置文件获取url，用户,密码，驱动
            ResourceBundle bundle = ResourceBundle.getBundle(CONFIG);
            String driver = bundle.getString("driver");
            String url = bundle.getString("url");
            String dataUser = bundle.getString("dateUser");
            String datePassword = bundle.getString("datePassword");
            String tableUser = bundle.getString("tableUser");
            String tablePassword = bundle.getString("tablePassword");
            String tableStatus = bundle.getString("tableStatus");


            try {
                //建立连接
                Class.forName(driver);
                connection = DriverManager.getConnection(url, dataUser, datePassword);
                statement = connection.createStatement();
                String sqlSelect = "select name, password, status from t_user";
                resultSet = statement.executeQuery(sqlSelect);
                //递归判断是否正确
                while (resultSet.next()){
                    if (name.equals(resultSet.getString(tableUser))
                        && passwd.equals(resultSet.getString(tablePassword))
                    ){
                        //判断是否在线
                        if (resultSet.getInt(tableStatus) == 0){
                            //然后修改status值
                            String sqlSetStatus = "update t_user set status = 1 where name =" + "'" + name+"'";
                            statement.executeUpdate(sqlSetStatus);
                            isRight = true;
                            return;
                        }

                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                if (resultSet != null){
                    try {
                        resultSet.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                if (statement != null){
                    try {
                        statement.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                if (connection != null){
                    try {
                        connection.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }


        }


        private void login(String user,String passwd){
            readPassword(user,passwd);
            if (isRight){
                this.dispose();
            }else {
                System.out.println("该用户已经登录，请重新登录");
            }


        }
    }


    public static void main(String[] args) {


        ClientChat clientChat = new ClientChat( );






    }
}
