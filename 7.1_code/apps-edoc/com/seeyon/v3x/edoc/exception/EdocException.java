package com.seeyon.v3x.edoc.exception;

import com.seeyon.ctp.common.exceptions.BusinessException;

public class EdocException extends BusinessException {
	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6763140365522383312L;
	private int errNum;
	
	public static enum errNumEnum {
		workflow_not_finish,
        workflow_finish,
        no_access_archives
    };
    
    public void setErrNum(int errNum)
    {
    	this.errNum=errNum;
    }
    public int getErrNum()
    {
    	return errNum;
    }

	public EdocException() {
		super();
	}

	public EdocException(String message, Throwable cause) {
		super(message, cause);
	}

	public EdocException(String message) {
		super(message);
	}
	
	public EdocException(int errNum,String message) {		
		super(message);
		this.errNum=errNum;
	}

	public EdocException(Throwable cause) {
		super(cause);
	}
	
	public EdocException(String errorCode, Object... errorArg) {
		super(errorCode,errorArg);
	}
}
