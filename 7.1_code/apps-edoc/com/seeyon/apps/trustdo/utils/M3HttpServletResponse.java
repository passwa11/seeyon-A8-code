/*
 * $Revision: 4332 $
 * $Date: 2013-05-20 15:43:33 +0800 (周一, 20 五月 2013) $
 * $Id: MHttpServletResponse.java 4332 2013-05-20 07:43:33Z chengc $
 * ====================================================================
 * Copyright © 2012 Beijing seeyon software Co..Ltd..All rights reserved.
 *
 * This software is the proprietary information of Beijing seeyon software Co..Ltd.
 * Use is subject to license terms.
 */
package com.seeyon.apps.trustdo.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 一个可以获取写入内容的Http响应对象
 * @author wangx
 * @since JDK 1.5
 * @version 1.0
 */
public class M3HttpServletResponse extends HttpServletResponseWrapper {
    private StringWriter baseWriter = null;
    private ByteArrayOutputStream baseOut = null;
    
    /**
     * 构建一个可以获取写入内容的Http响应对象
     * @param response 原始的Http响应对象
     */
    public M3HttpServletResponse(HttpServletResponse response) {
        super(response);
    }
    
    @Override
    public PrintWriter getWriter() throws IOException {
        baseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(baseWriter);
        return writer;
    }
    
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        baseOut = new ByteArrayOutputStream();
        ServletOutputStream out = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                baseOut.write(b);
            }

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setWriteListener(WriteListener arg0) {
				// TODO Auto-generated method stub
				
			}

        };
        return out;
    }
    
    /**
     * 获取响应中的内容
     * @return 返回响应中的被写入的内容
     */
    public String getContent() {
        String result = "";
        if(baseOut != null && baseOut.size() > 0) {
            result = baseOut.toString();
        }
        
        if(baseWriter != null) {
            String tmp = baseWriter.toString();
            if(tmp != null) {
                result += baseWriter.toString();
            }
        }
        String contentType = getContentType();
        reset();
        setContentType(contentType);
        setStatus(200);
        return result;
    }
    
    
}
