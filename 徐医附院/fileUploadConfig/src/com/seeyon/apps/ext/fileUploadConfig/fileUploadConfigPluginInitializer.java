package com.seeyon.apps.ext.fileUploadConfig;

import com.seeyon.ctp.common.AbstractSystemInitializer;

public class fileUploadConfigPluginInitializer extends AbstractSystemInitializer {

    @Override
    public void initialize() {
        System.out.println("启动fileUploadConfig插件");
    }

    @Override
    public void destroy() {
        System.out.println("销毁fileUploadConfig插件");
    }
}
