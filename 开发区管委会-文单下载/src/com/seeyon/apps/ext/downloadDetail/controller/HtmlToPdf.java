package com.seeyon.apps.ext.downloadDetail.controller;

import org.zefer.pd4ml.PD4Constants;
import org.zefer.pd4ml.PD4ML;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

public class HtmlToPdf {

    public static byte[] toPdf(String info) throws IOException {
        StringReader sr = new StringReader(info);
        ByteArrayOutputStream baas = new ByteArrayOutputStream();
        PD4ML pd4ML = new PD4ML();
        pd4ML.setPageInsets(new Insets(5, 5, 5, 5));
        pd4ML.setHtmlWidth(900);
        pd4ML.setPageSize(PD4Constants.A4);
        try {
            pd4ML.useTTF("java:fonts", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pd4ML.setDefaultTTFs("SimHei", "YouYuan", "SimSun");
        pd4ML.enableDebugInfo();
        pd4ML.render(sr, baas);
        return baas.toByteArray();
    }


}
