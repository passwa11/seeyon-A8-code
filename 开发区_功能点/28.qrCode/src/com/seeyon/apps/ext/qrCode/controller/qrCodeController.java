package com.seeyon.apps.ext.qrCode.controller;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.controller.BaseController;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public class qrCodeController extends BaseController {

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return new ModelAndView("apps/ext/qrCode/index");
    }

    /**
     * 在这里写一个方法记录一下上传的图片，这个图片是二维码背景图。
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ModelAndView saveBjPic(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String createdate = request.getParameter("createdate");
        String fileUrl = request.getParameter("fileUrl");
        String filename = request.getParameter("filename");
        String mimeType = request.getParameter("mimeType");
        System.out.println(createdate);
        return null;
    }
}