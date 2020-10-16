package com.seeyon.apps.trustdo.exceptions;

public class XRDUserException extends Exception {

	private static final long serialVersionUID = 1L;
	//用来创建无参数对象
	public XRDUserException()  {}                
	//用来创建指定参数对象
	public XRDUserException(String message) {        
		//调用超类构造器
		super(message);                             
    }
}
