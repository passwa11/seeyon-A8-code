package com.seeyon.v3x.edoc.exception;

import com.seeyon.ctp.util.UUIDLong;

public class EdocMarkHistoryExistException extends EdocException {

	private static final long serialVersionUID = UUIDLong.longUUID();
	
	public EdocMarkHistoryExistException() {
		super("edoc.docmark.exist","");
	}
}
