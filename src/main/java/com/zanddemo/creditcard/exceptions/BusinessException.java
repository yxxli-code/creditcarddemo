package com.zanddemo.creditcard.exceptions;

public class BusinessException extends RuntimeException {
    static final long serialVersionUID = 1L;
    private String code;
    private Object data;

    public BusinessException(){
        super();
    }
    public BusinessException(String code){
       this.code = code;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String code, Object data){
        this.code = code;
        this.data = data;
    }

    public BusinessException(String code, String message, Object... args) {
        super(String.format(message, args));
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
       return data;
    }
}
