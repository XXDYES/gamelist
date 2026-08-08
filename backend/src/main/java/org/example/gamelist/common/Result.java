package org.example.gamelist.common;


import lombok.Data;

/**
 * 封装通用返回类
 */
@Data
public class Result<T> {
    //定义两个常量，成功的code是200，失败的是-1
    private static final String SUCCESS_CODE = "200";
    private static final String ERROR_CODE = "-1";

    private String code;//code：接口的响应结果
    private T data;//data：数据
    private String msg;//msg：存放错误信息

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        return result;
    }

    // 有参数的成功方法（泛型版本）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setData(data);
        return result;
    }

    // 失败方法
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMsg(msg);
        return result;
    }

    // 失败方法（带错误码）
    public static <T> Result<T> error(String code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}

