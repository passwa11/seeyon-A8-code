package com.seeyon.apps.ext.qrCode;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class qrCodePluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("����qrCode���");
    }

    @Override
    public void destroy() {
        System.out.println("����qrCode���");
    }
}