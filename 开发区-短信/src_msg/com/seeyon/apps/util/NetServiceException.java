package com.seeyon.apps.util;


/**
 * @author
 * @version
 * 调用异常
 */
public class NetServiceException extends Exception {

	private static final long serialVersionUID = 1L;

	public NetServiceException(String error){
		super(error);
	}

	public NetServiceException(String error, Throwable cause){
		super(error,cause);
	}

}
