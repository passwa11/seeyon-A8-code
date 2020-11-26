package com.seeyon.apps.collaboration.manager;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;

public class FromDocFilingEnable extends CollDocFilingEnable {
    public Integer getAppEnumKey() {
        return ApplicationCategoryEnum.form.getKey();
    }
}
