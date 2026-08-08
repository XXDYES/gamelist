package org.example.gamelist.exception;

public class BusinessException extends RuntimeException{
    private Integer code; // 错误码（可选）
    private String msg;   // 错误信息

    public BusinessException(String msg) {
        this.msg = msg;
    }

    // getter/setter
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
}
