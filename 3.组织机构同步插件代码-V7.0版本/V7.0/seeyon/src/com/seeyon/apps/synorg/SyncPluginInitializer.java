package com.seeyon.apps.synorg;

import com.seeyon.apps.synorg.util.ExcuteSqlFileUtil;
import com.seeyon.ctp.common.AbstractSystemInitializer;

/**
 * 插件初始化
 * @author Yang.Yinghai
 * @date 2015-8-18下午5:57:36
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SyncPluginInitializer extends AbstractSystemInitializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        System.out.println("◆初始化组织同步插件");
        ExcuteSqlFileUtil.initSqlFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        System.out.println("◆销毁组织机构同步插件");
    }
}
