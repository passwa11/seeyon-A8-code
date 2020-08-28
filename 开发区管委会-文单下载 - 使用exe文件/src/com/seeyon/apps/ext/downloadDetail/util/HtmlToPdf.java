package com.seeyon.apps.ext.downloadDetail.util;

import com.itextpdf.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class HtmlToPdf {

    public static void parseHtmlToPdf() throws Exception {
// step 1
        String outputFile = "F:/htmlToPdf2.pdf";
        String inputFile = "F:/q.html";
        String url = new File(inputFile).toURI().toURL().toString();
        // step 2
        OutputStream os = new FileOutputStream(outputFile);
        org.xhtmlrenderer.pdf.ITextRenderer renderer = new ITextRenderer();
        renderer.setDocument(url);

        // step 3 解决中文支持
        ITextFontResolver fontResolver = renderer.getFontResolver();
        fontResolver.addFont("c:/Windows/Fonts/simsun.ttc",
                BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

        // 解决图片的相对路径问题
        // renderer.getSharedContext().setBaseURL("file:/F:/teste/html/");
        renderer.layout();
        renderer.createPDF(os);
        os.close();
    }

    public static void main(String[] args) throws Exception{
        parseHtmlToPdf();
    }

}
