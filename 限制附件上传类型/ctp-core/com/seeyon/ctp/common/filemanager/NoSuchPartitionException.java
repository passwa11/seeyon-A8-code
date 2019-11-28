/**
 * 
 */
package com.seeyon.ctp.common.filemanager;

import java.util.Date;

import com.seeyon.ctp.common.exceptions.BusinessException;

/**
 *
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2008-2-19
 */
public class NoSuchPartitionException extends BusinessException {

    private static final long serialVersionUID = -1557470117236445951L;

    public NoSuchPartitionException(Date date) {
        super("fileupload.document.nosuchpartition", date);
    }

}
