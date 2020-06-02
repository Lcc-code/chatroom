package com.lc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import java.sql.*;
import java.util.ResourceBundle;

public class ClientRegister extends JFrame {

    private JButton regiBt = new JButton("注册");
    private JButton stopBt = new JButton("关闭");

    //账号输入框
    private JTextField uTF = new JTextField(20);
    //密码输入框
    private JPasswordField pTF1 = new JPasswordField(20);
    //重新确认输入框
    private JPasswordField pTF2 = new JPasswordField(20);
    JLabel interestLabel = new JLabel("账   号:");
    JLabel interestLabe2 = new JLabel("密   码：");
    JLabel interestLabe3 = new JLabel("重复密码：");

    private JTextField jTextField1 = new JTextField();
    private JTextField jTextField2 = new JTextField();
    private JTextField jTextField3 = new JTextField();

    private JPanel jPanel = new JPanel();

    private static final String CONFIG = "config";
    private boolean isExists = false;


    private JLabel jBP = new JLabel();
    public ClientRegister() throws HeadlessException {
        //初始化
        init();
        //监听
        regiBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String string1 = jTextField1.getText();
                String string2 = jTextField2.getText();
                String string3 = jTextField3.getText();
                if (string2 == null) {
                    System.out.println("输入不能为空");
                    return;
                }
                if (!string2.equals(string3))
                {
                    System.out.println("输入密码不一致");
                    return;
                }
                System.out.println(string1 + string2);
                //写入数据库内
                register(string1, string2);

            }
        });
        stopBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

    }

    private void init(){
        this.setTitle("注册界面");
        this.setBounds(0,0,500,500);

        //读取path,
        jBP.setIcon(new ImageIcon(readPath(CONFIG,"BackgroundPicture")));
        this.add(jBP,BorderLayout.NORTH);


        //面板对象

        jPanel.setLayout(null);
        //设置弹出位置，及大小


        this.add(jPanel);

        regiBt.setBounds(150,130,60,20);
        stopBt.setBounds(220,130,60,20);

        //放置
        jPanel.add(regiBt);
        jPanel.add(stopBt);



        jTextField1.setBounds(160,10,150,25);
        interestLabel.setBounds(100,10,50,25);

        jTextField2.setBounds(160,40,150,25);

        interestLabe2.setBounds(100,40,50,25);

        jTextField3.setBounds(160,70,150,25);

        interestLabe3.setBounds(100,70,60,25);


        jPanel.add(jTextField1);
        jPanel.add(interestLabel);

        jPanel.add(jTextField2);
        jPanel.add(interestLabe2);

        jPanel.add(jTextField3);
        jPanel.add(interestLabe3);

        jPanel.setVisible(true);



        this.setVisible(true);





    }
    private void register(String name,String password){
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


        try {
            //建立连接
            Class.forName(driver);
            connection = DriverManager.getConnection(url, dataUser, datePassword);
            statement = connection.createStatement();
            String sqlSelect = "select name from t_user";
            resultSet = statement.executeQuery(sqlSelect);
            //递归判断是否正确
            while (resultSet.next()) {
                if (name.equals(resultSet.getString(tableUser))) {
                    System.out.println("该用户已存在");
                    isExists = true;
                    return;
                }
            }
            if (!isExists){
                String sqlRegister = "insert into t_user (name, password) value(" + "'" + name+"','" + password + "')";
                int count = statement.executeUpdate(sqlRegister);
                System.out.println(count == 1? "注册成功！":"注册失败！");


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
    private String readPath(String s,String key){
       //使用资源绑定器,获取config配置
        ResourceBundle bundle = ResourceBundle.getBundle(s);
        System.out.println(bundle.getString(key));
        return Thread.currentThread().getContextClassLoader()
                .getResource(bundle.getString(key))
                .getPath();

    }

    public static void main(String[] args) {
        //测试
        ClientRegister clientRegister = new ClientRegister();
        clientRegister.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
