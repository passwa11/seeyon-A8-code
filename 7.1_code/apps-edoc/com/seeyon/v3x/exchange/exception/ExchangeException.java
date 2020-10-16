package com.seeyon.v3x.exchange.exception;

import com.seeyon.ctp.common.exceptions.BusinessException;

public class ExchangeException extends BusinessException{
	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7533303283038303366L;

	public ExchangeException() {
		super();
	}

	public ExchangeException(String message,Throwable cause){
		super(message, cause);
	}

	public ExchangeException(String message) {
		super(message);
	}

	public ExchangeException(Throwable cause) {
		super(cause);
	}
}
