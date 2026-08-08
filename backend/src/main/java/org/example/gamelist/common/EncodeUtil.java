package org.example.gamelist.common;

import org.mindrot.jbcrypt.BCrypt;

public class EncodeUtil {
//    加密密码
    public static String encode(String plainPassword){
        return BCrypt.hashpw(plainPassword,BCrypt.gensalt());
    }
//    校验密码
    public static boolean matches(String plainPassword,String hashedPassword){
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
