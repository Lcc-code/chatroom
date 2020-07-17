package com.lc;

import com.lc.DButils.DButils;


import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 客户端
 */
public class ClientChat extends JFrame {

    // 声明一个显示纯文本的多行区域
    private JTextArea tA = new JTextArea(10,20);
    private JPanel jPanel2 = new JPanel(new FlowLayout(30));
    // 编辑单行文本框
    private JTextField tF = new JTextField(20);

    // 文本区加滚动条
    private JScrollPane sP = new JScrollPane(tA);
    // 文件选择按钮
    private JButton chooseBt = new JButton("选择图片");
    private JFileChooser chooser = new JFileChooser();
    private JPanel jPanel = new JPanel(new FlowLayout(30));

    // 暂时连接本机回环地址
    private static final String CONNSTR = "127.0.0.1";
    // 使用8888端口
    private static final int PORT = 8888;
    private  Socket socket = null;

    private boolean isConn = false;
    private boolean isRight = false;

    private DataOutputStream oS = null;
    private JLabel jBP = new JLabel();
    private static final String CONFIG = "config";



    // 构建初始化方法
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
                init(clientLogin.getUserLoginInfo().get("name"));
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

        // 当聊天窗口关闭，
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                logout(clientLogin.getUserLoginInfo().get("name"));
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

    /**
     * 初始化方法
     * @param user 用户名
     */
    private void init(String user){

        this.setTitle(user + "的窗口");
        // 将文本区域添加到中间位置
        // this.add(tA, BorderLayout.CENTER);
        // 将文本框添加
        jPanel.add(tF);
        jPanel.add(chooseBt);
        /*this.add(tF,BorderLayout.SOUTH);
        this.add(jLabel, BorderLayout.EAST);*/
        this.add(jPanel,BorderLayout.SOUTH);
        // 设置区域大小
        this.setBounds(300,300,400,400);
        // 设置显示框区域无法输入
        tA.setEditable(false);
        // 设置光标聚焦
        tF.requestFocus(true);
        // 加入滚动条和文本区域
        this.add(sP,BorderLayout.CENTER);
        // 读取path,
        jBP.setIcon(new ImageIcon(readPath(CONFIG,"BackgroundPicture2")));
        this.add(jBP,BorderLayout.NORTH);

        this.setVisible(true);

        (new Thread(new Receive())).start();

        /**
         * 事件监听
         */
        tF.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String strSend = tF.getText();
                if (strSend.trim().length() == 0){
                    return;
                }
                // 上传\
                send(user,strSend, null);

                // 判断输入字符长度

                tF.setText(" ");

            }
        });

        chooseBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 文件名过滤器
                FileNameExtensionFilter filter = new FileNameExtensionFilter("图片文件", "jpg"
                        , "png", "jpeg");
                chooser.setFileFilter(filter);

                // 显示对话框
                int ret = chooser.showOpenDialog(null);
                // 获取用户选择的结果
                if ( ret == JFileChooser.APPROVE_OPTION){
                    // 结果为：已经存在一个文件
                    File file = chooser.getSelectedFile();
                    send(user,file.getAbsolutePath(),"picture");
                    System.out.println(file.getAbsolutePath());

                }
            }
        });
        connect();
        // 关闭操作设置
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    /**
     * 连接
     */
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

    /**
     * 发送消息给服务器
     * @param user
     * @param string
     */
    public void send(String user,String string, String type){
        try {
            //创建输出字节流
            oS = new DataOutputStream(socket.getOutputStream());
            if ("picture".equals(type)){
                String imageData = Base64Utils.GetImageStr(string);
                oS.writeUTF( user+ ":" + "|"+ imageData + "&");
            }
            else {
                //写入字节
                oS.writeUTF( user+ ":" + string);
            }
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
                StringBuffer sb = new StringBuffer();
                int imageCount = 0;
                int s ;
                int e = 0;
                while (isConn) {
                    DataInputStream dIS = new DataInputStream(socket.getInputStream());
                    // 接收信息
                    String string = dIS.readUTF();
                    if ((s=string.indexOf("|"))>0 && (e =string.indexOf("&"))>0){
                        String s2 = string.substring(s+ 1,e);
                        System.out.println(s2);
                        Base64Utils.GenerateImage(s2, "C:\\Users\\hp\\Desktop\\picture\\"+imageCount+".png");
                        imageCount ++;
                        tA.append(string.substring(0,s+1) + "：上传了一张图片！" + "\n");
                    }else {
                        tA.append(string);
                    }


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
        // 使用资源绑定器,获取config配置
        ResourceBundle bundle = ResourceBundle.getBundle(s);
        return Thread.currentThread().getContextClassLoader()
                .getResource(bundle.getString(key))
                .getPath();

    }

    /**
     * 关闭类
     * @param name 用户名
     */
    private void logout(String name){
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            // 使用工具类建立连接
            connection = DButils.getConnection();
            String sqllogout = "update t_user set status = ? where name =?";
            ps = connection.prepareStatement(sqllogout);
            ps.setInt(1,0);
            ps.setString(2,name);
            int count = ps.executeUpdate();
            if (count == 0){
                System.out.println("下线失败，请重新尝试！");
            } else {
                isRight = false;
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            DButils.close(resultSet, ps, connection);
        }

    }

    /**
     * 登录类
     */
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




        private Map<String, String> userLoginInfo;
        public Map<String, String> getUserLoginInfo() {

            return userLoginInfo;
        }
        public void setUserLoginInfo(Map<String, String> userLoginInfo) {
            this.userLoginInfo = userLoginInfo;
        }


        private JLabel jBP = new JLabel();


        public ClientLogin() throws HeadlessException {

            init();
            // 监听
            loginBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 使用HashMap接收
                    Map<String, String> infoMap = new HashMap<>();
                    infoMap.put("name", jTextField1.getText());
                    infoMap.put("password", jTextField2.getText());
                    setUserLoginInfo(infoMap);

                    String name = infoMap.get("name");
                    String passwd = infoMap.get("password");
                    if (name == null || passwd ==null) {
                        System.out.println("输入不能为空");
                        return;
                    }

                    login(infoMap);

                    System.out.println("用户名：" + name + ":" + passwd);
                }
            });
            stopBt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

        }

        /**
         * 初始化内部类
         */
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

        /**
         * 查看用户密码是否正确
         * @param userLoginInfo 用户信息Map
         */
        private void readPassword(Map<String, String> userLoginInfo){
            Connection connection = null;
            PreparedStatement ps = null;
            ResultSet resultSet = null;

            try {
                // 使用工具类建立连接
                connection = DButils.getConnection();
                // 修改查询语句为预编译
                String sqlSelect = "select * from t_user where name = ? and password = ?";
                ps = connection.prepareStatement(sqlSelect);
                // 添加元素
                ps.setString(1, userLoginInfo.get("name"));
                ps.setString(2, userLoginInfo.get("password"));
                resultSet = ps.executeQuery();

                // 判断是否正确
                if ( resultSet.next()){
                    int status = resultSet.getInt("status");
                    System.out.println(status);
                    // 查看该用户是否在线
                    if (status == 0) {
                        // 设置在线状态
                        String sqlSetStatus = "update t_user set status = ? where name = ?";
                        ps = connection.prepareStatement(sqlSetStatus);
                        ps.setInt(1,1);
                        ps.setString(2,userLoginInfo.get("name"));
                        int count1 = ps.executeUpdate();
                        System.out.println(count1 == 1 ?userLoginInfo.get("name") + ":上线成功":userLoginInfo.get("name") + ":上线失败");
                        isRight = true;
                        return;
                    }
                    else {
                        System.out.println("该用户已经登录");
                    }
                }
                else {
                    System.out.println("密码错误！");

                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                DButils.close(resultSet, ps, connection);
            }


        }

        /**
         * 登录
         * @param userLoginInfo
         */
        private void login(Map<String, String> userLoginInfo){
            readPassword(userLoginInfo);
            if (isRight){
                this.dispose();
            }else return;


        }
    }


    public static void main(String[] args) {
        ClientChat clientChat = new ClientChat( );

    }
}
