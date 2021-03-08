package com.mycompany.sso;

public class PasswordRegex {
    public static void main(String[] args) {
        System.out.println(check("1qaz@WSX"));
    }
    
    public static boolean check(String password) {
        /*
        1. ^$
        2. ^()()()()$
        3. ^()()()().{8,64}$
        4. ^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[!@#$%^&*()]).{8,64}$
        */
        // 用正規表示式驗證用戶建立的密碼是否符合
        String regex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[!@#$%^&*()]).{8,64}$";
        return password.matches(regex);

    }
    
}
