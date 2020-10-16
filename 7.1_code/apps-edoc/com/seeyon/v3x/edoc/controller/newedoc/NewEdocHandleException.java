package com.seeyon.v3x.edoc.controller.newedoc;

@SuppressWarnings("serial")
public class NewEdocHandleException extends Exception{
    public final static int REDIRECT_CODE = 1;
    public final static int PRINT_CODE = 2;
    
    private String msg;
    private int code; //备用属性，用来在NewEdocHandle类中判断从子类中抛出的异常
    
    public NewEdocHandleException(String msg){
        this.msg = msg;
    }
    
    public NewEdocHandleException(int code){
        this.code = code;
    }
    
    public NewEdocHandleException(String msg,int code){
        this.msg = msg;
        this.code = code;
    }
    
    public String getMsg(){
        return msg;
    }
    
    public int getCode(){
        return code;
    }
}
