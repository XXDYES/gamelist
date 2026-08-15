package org.example.gamelist.common;

public class UserContext {
    private static final ThreadLocal<Integer> CURRENT_USERID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();
    public static void setCurrentUser(Integer userid,String username){
        CURRENT_USERID.set(userid);
        CURRENT_USERNAME.set(username);
    }
    public static Integer getCurrentId(){
        return CURRENT_USERID.get();
    }
    public static String getCurrentUsername() {
        return CURRENT_USERNAME.get();
    }
    public static void clear(){
        CURRENT_USERNAME.remove();
        CURRENT_USERID.remove();
    }
}
