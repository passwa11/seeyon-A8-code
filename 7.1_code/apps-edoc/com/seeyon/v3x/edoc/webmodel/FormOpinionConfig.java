/**
 * Author : xuqw
 *   Date : 2014年11月12日 下午2:51:24
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.webmodel;

import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDateFormatSetEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDateModelSetEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDisplaySetEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionShowNameTypeEnum;

/**
 * <p>Title       : F7</p>
 * <p>
 * Description : 用于公文单配置edoc_form_extend_info表的OPTIONFORMATSET字段<br/>
 *               相对应的JSON数据配置<br/>
 *               说明：配置项, 每个配置项需要进行初始化设置，这样就可以不用考虑升级
 * </p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 */
public class FormOpinionConfig {
    
    /*******************************
     * 
     * 公文单配置在页面上有分组概念, 直接使用JAVA体现分组又兼容扩展情况暂时无法实现，
     * 这里使用一层结构进行保存，配置的层次关系，使用代码逻辑判断
     * 
     * 特别说明：本类中，所有的字段都会保存到数据库中，请不要添加非设置属性的字段
     * 
     ********************************/
    
    /*&&&&&&&& 意见保留设置  start &&&&&&&&*/
    
    /** 意见显示：默认显示全部 **/
    private String opinionType = OpinionDisplaySetEnum.DISPLAY_ALL.getValue();
    
    /*&&&&&&&&* 意见保留设置  end &&&&&&&&*/
    
    
    /*@@@@@@@@@ 系统落款 start @@@@@@@@@*/
    
    /**落款是否显示处理人单位信息, 默认不显示**/
    private boolean showUnit = false;
    
    /**落款是否显示处理人部门信息，默认显示**/
    private boolean showDept = true;
    
    
    /**落款是否显示处理人姓名信息，默认显示**/
    private boolean showName = true;
    
    /**默认普通方式**/
    private String showNameType = OpinionShowNameTypeEnum.COMMON.getValue();
    
    
    /**文单签批后不显示系统落款: 默认不勾选 **/
    private boolean hideInscriber = false;
    
    /**落款是否换行显示：默认不换行**/
    private boolean inscriberNewLine = false;
    
    /**处理人姓名与处理时间换行显示**/
    private boolean nameAndDateNotInline = false;
    
    /*@@@@@@@@@ 系统落款 end @@@@@@@@@*/
    
    
    /*######### 处理时间显示格式  start #########*/
    
    /**处理时间显示格式 : 默认显示日期时间**/
    private String showDateType = OpinionDateFormatSetEnum.DATETIME.getValue();

    /*######### 处理时间显示格式  end #########*/
    
    
    /*######### 处理时间显示样式  start #########*/
    /**处理时间显示样式 : 默认显示简称**/
    private String showDateModel = OpinionDateModelSetEnum.SIMPLE.getValue();
    /*######### 处理时间显示样式  end #########*/
    
    
    /*######### 处理附件及关联文档显示  start #########*/
    /**处理附件是否显示：默认不显示 **/ 
    private boolean showAtt = true;
    /*######### 处理附件及关联文档显示  end #########*/
   
    
    /**默认配置(不会被转换到到JSON串中)**/
    private static String defualt = null;
    
    
    /**
     * 
     * @Description : 获取默认配置信息
     * @Author      : xuqiangwei
     * @Date        : 2014年11月13日下午1:38:48
     * @return
     */
    public static String getDefualtConfig(){
        if(defualt == null){
            FormOpinionConfig formConfig = new FormOpinionConfig();
            defualt = JSONUtil.toJSONString(formConfig);
        }
        
        return defualt;
    }
    
    
    public String getOpinionType() {
        return opinionType;
    }

    public void setOpinionType(String opinionType) {
        this.opinionType = opinionType;
    }

    public boolean isShowUnit() {
        return showUnit;
    }

    public void setShowUnit(boolean showUnit) {
        this.showUnit = showUnit;
    }

    public boolean isShowDept() {
        return showDept;
    }

    public void setShowDept(boolean showDept) {
        this.showDept = showDept;
    }

    public boolean isShowName() {
        return showName;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public String getShowNameType() {
        return showNameType;
    }

    public void setShowNameType(String showNameType) {
        this.showNameType = showNameType;
    }

    public String getShowDateType() {
        return showDateType;
    }

    public void setShowDateType(String showDateType) {
        this.showDateType = showDateType;
    }

    public void setInscriberNewLine(boolean inscriberNewLine) {
        this.inscriberNewLine = inscriberNewLine;
    }

    public boolean isInscriberNewLine() {
        return inscriberNewLine;
    }


    public boolean isHideInscriber() {
        return hideInscriber;
    }


    public void setHideInscriber(boolean hideInscriber) {
        this.hideInscriber = hideInscriber;
    }


	public String getShowDateModel() {
		return showDateModel;
	}


	public void setShowDateModel(String showDateModel) {
		this.showDateModel = showDateModel;
	}

	public boolean isShowAtt() {
		return showAtt;
	}

	public void setShowAtt(boolean showAtt) {
		this.showAtt = showAtt;
	}
	
	public boolean isNameAndDateNotInline() {
		return nameAndDateNotInline;
	}
	
	public void setNameAndDateNotInline(boolean nameAndDateNotInline) {
		this.nameAndDateNotInline = nameAndDateNotInline;
	}
       
    
}
