package com.mycompany.sso.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mycompany.sso.SHA2;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import javax.servlet.http.HttpServlet;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class BaseServlet extends HttpServlet {
    // 建立 connection
    private static Connection conn;
    static {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection("jdbc:derby://localhost:1527/security", "app", "app");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected List<Map<String, Object>> getSalary(String username) {
        //String sql = "SELECT username, money FROM Salary WHERE username='" + username + "'";
        String sql = "SELECT username, money FROM Salary WHERE username=?";
        System.out.println(sql);
        try(PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            BasicRowProcessor convert = new BasicRowProcessor();
            MapListHandler handler = new MapListHandler(convert);
            return handler.handle(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected List<Map<String, Object>> getMember(String username) {
        //String sql = "SELECT username, email FROM Member WHERE username='" + username + "'";
        String sql = "SELECT username, email, cardno FROM Member WHERE username=?";
        try(PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            BasicRowProcessor convert = new BasicRowProcessor();
            MapListHandler handler = new MapListHandler(convert);
            return handler.handle(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // A01, 1qaz@WSX
    protected boolean login(String username, String password) {
        // 驗證 username
        //String sql = "SELECT username, salt FROM Member WHERE username = '" + username + "'";
        String sql = "SELECT username, salt FROM Member WHERE username = ?";
        System.out.println(sql);
        int salt = 0;
        try(PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                salt = rs.getInt("salt"); // 得到 salt
                System.out.println("Salt:" + salt);
            } else {
                System.out.println("無此使用者");
                return false;
            }
        } catch (Exception e) {
        }
        
        // 驗證 password
        password = SHA2.getSHA256(password, salt);
        //sql = "SELECT username, password, salt FROM Member WHERE username = '" + username + "' and password = '" + password + "' and salt = " + salt;
        sql = "SELECT username, password, salt FROM Member WHERE username = ? and password = ? and salt = ?";
        System.out.println(sql);
        try(PreparedStatement stmt = conn.prepareStatement(sql);) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, salt);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                System.out.println("驗證 password 成功");
                return true;
            } else {
                System.out.println("驗證 password 失敗");
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }
    
    protected boolean saveMember(String username, String password, String email, int money) {
        int salt = new Random().nextInt(10_0000);
        password = SHA2.getSHA256(password, salt);
        int bonus = new Random().nextInt(10_0000);
        //String sql_1 = "INSERT INTO Member(username, password, salt, email) values ('" + username + "', '" + password + "', " + salt + ", '" + email + "')";
        //String sql_2 = "INSERT INTO Salary(username, money, bonus) values ('" + username + "', " + money + ", " + bonus + ")";
        String sql_1 = "INSERT INTO Member(username, password, salt, email) values (?, ?, ?, ?)";
        String sql_2 = "INSERT INTO Salary(username, money, bonus) values (?, ?, ?)";
        
        try(PreparedStatement stmt1 = conn.prepareStatement(sql_1);
            PreparedStatement stmt2 = conn.prepareStatement(sql_2);) {
            conn.setAutoCommit(false);
            stmt1.clearBatch();
            stmt1.setString(1, username);
            stmt1.setString(2, password);
            stmt1.setInt(3, salt);
            stmt1.setString(4, email);
            stmt1.addBatch();
            
            stmt2.clearBatch();
            stmt2.setString(1, username);
            stmt2.setInt(2, money);
            stmt2.setInt(3, bonus);
            stmt2.addBatch();
            
            int[] rowscount1 = stmt1.executeBatch();
            int[] rowscount2 = stmt2.executeBatch();
            conn.commit();
            if(Arrays.stream(rowscount1).sum() == 1 && 
               Arrays.stream(rowscount2).sum() == 1) {
                return true;
            } else {
                conn.rollback();
            }
        } catch(Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
    
    protected boolean verifyCaptcha(String grr) throws MalformedURLException, IOException {
        String secret = "6LcOcdYZAAAAAN5W_GOnPPWcZpkHss2qO7Ycmocj";
        String remoteip = "localhost";
        String path = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s";
        path = String.format(path, secret, grr, remoteip);
        String json = new Scanner(new URL(path).openStream(), "UTF-8").useDelimiter("\\A").next();
        // { "success": true, "challenge_ts": "2020-10-16T11:51:14Z", "hostname": "localhost", "score": 0.9, "action": "submit" }
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        System.out.println(json);
        System.out.println(map);
        boolean success = Boolean.parseBoolean(map.get("success").toString());
        if(success) {
            double score = Double.parseDouble(map.get("score").toString());
            boolean check = success && score >= 0.5;
            return check;
        }
        return false;
    }
}