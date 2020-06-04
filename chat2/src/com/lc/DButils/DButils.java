package com.lc.DButils;

import java.sql.*;
import java.util.ResourceBundle;

/**
 * 数据库封装的工具类
 */
public class DButils {

    //工具类的构造方法
    private DButils(){}

    // 静态代码块的编写，静态代码块在静态类被调用的时候，会执行
    static {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        String driver = bundle.getString("driver");
        //在这里获取类编译
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建连接方法
     * @return 构造好的连接
     * @throws SQLException
     */public static Connection getConnection () throws SQLException {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        String url = bundle.getString("url");
        String user = bundle.getString("dateUser");
        String password = bundle.getString("datePassword");
        //这里上抛异常，因为原函数在调用时捕捉异常，这里上抛处理
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * 释放资源的方法
     * @param resultSet 结果集
     * @param statement 获取数据库操作的对象
     * @param connection 连接
     */
    public static void close(ResultSet resultSet, Statement statement, Connection connection){
        if (resultSet != null){
            try {
                resultSet.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }if (statement != null){
            try {
                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }if (connection != null){
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
