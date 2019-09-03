package com.seeyon.ctp.portal.util;

import java.util.ArrayList;
import java.util.List;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.util.EnumUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;

public class Constants {

	/**
	 * 登录前门户唯一标识
	 */
	public static String preLoginPortalId= "-7779029842361826066";

	public static String PATH_SEPARATOR = "/";

	public static String DOCUMENT_TYPE = ".psml";

	public static String DEFAULT_SPACE_SUBFIX = "_D";

	//默认 是否允许自定义
    public static boolean DEFAULT_Allowdefined = true;

	/**
	 * 空间类型，顺序不可改变
	 */
	public static enum SpaceType {
	    /**
	     * 0 个性化个人空间
	     */
		personal, //0 个人空间  PSML命名：人id.psml

		//修改为协作空间
		/**
		 * 部门空间
		 */
		department, //1 部门空间  PSML命名：部门id.psml
		/**
		 * 单位空间
		 */
		corporation, //2 单位空间  PSML命名：单位id.psml
		/**
		 * 集团空间
		 */
		group, //3 集团空间  PSML命名：1.psml
		/**
		 * 自定义团队空间
		 */
		custom, //4 自定义空间  PSML命名：SpaceFix的id.psml

		//默认的空间
		/**
		 * 默认个人空间
		 */
		Default_personal, //5 默认个人空间
		//@Deprecated
		/**
		 * 默认部门空间
		 */
		Default_department, //6 默认部门空间

		@Deprecated
		Default_custom,  //7 废弃
		/**
		 * 第三方系统
		 */
		thirdparty, //8 第三方页面
		/**
		 * 默认领导空间
		 */
		default_leader,//9 默认的领导空间
		/**
		 * 个性化领导空间
		 */
		leader,//10 领导空间
		/**
		 * 关联系统
		 */
		related_system,//11 关联系统
		/**
		 * 关联项目枚举，关联项目空间请使用related_project_space
		 */
		related_project,//12 关联项目

		//320-增加 空间授权
		/**
		 * 自定义个人空间
		 */
		Default_personal_custom,//13 单位定义的个人空间
		/**
		 * 默认外部人员空间
		 */
		Default_out_personal,//14 单位定义 默认外部人员空间
		/**
		 * 个性化个人自定义空间
		 */
		personal_custom,//15 个人空间
		/**
		 * 个性化外部人员空间
		 */
		outer,//16  外部人员空间
		/**
		 * 自定义单位空间
		 */
		public_custom,//17 公共空间--自定义公共空间--单位
		/**
		 * 自定义集团空间
		 */
		public_custom_group,//18 公共空间-自定义公共空间-集团
		//二级主页空间
		/**
		 * 协同工作
		 */
		cooperation_work,//19 协同工作
		/**
		 * 目标管理
		 */
        objective_manage,//20 目标管理
        /**
         * 公文管理
         */
        edoc_manage,//21 公文管理
        /**
         * 会议管理
         */
        meeting_manage,//22 会议管理
        /**
         * 协同驾驶舱
         */
        performance_analysis,//23 协同驾驶舱
        /**
         * 空间模板的默认空模板类型，不是空间类型，内部使用
         */
        template,//空间模板的默认空模板类型，不作为空间类型使用
        /**
         * 表单应用
         */
        form_application,//25 表单应用
		/**
		 * 关联项目空间
		 */
		related_project_space,//26 关联项目空间，区别于关联项目
        /**
         * V-Join
         */
        vjoinpc, //27，PC空间
        vjoinmobile, //28，移动空间
        vjoinpc_custom, //29，自定义PC空间
        vjoinmobile_custom, //30，自定义移动空间

        /**
         * 移动空间（微协同）---废弃，跟M3合体
         */
        @Deprecated
        weixinmobile, //31，个人信息门户
        @Deprecated
        weixinmobile_custom, //32，自定义个人信息门户
        @Deprecated
        weixinmobile_leader, //33，领导信息门户
        @Deprecated
        weixinmobile_leader_custom, //34，自定义领导信息门户

        /**
         * 移动空间（M3）
         */
        m3mobile, //35，个人信息门户
        m3mobile_custom,//36，自定义个人信息门户
        m3mobile_leader, //37，领导信息门户
        m3mobile_leader_custom, //38，自定义领导信息门户

        /**
         * 大屏
         */
        big_screen ,//39,大屏
        /**
         * 登陆前
         */
        before_login,//40 登陆前

		v_report,//41 报表空间

		m3mobile_corporation; //42，移动单位信息门户

        //end
        public static SpaceType getEnumByKey(int key) {
            for (SpaceType e : SpaceType.values()) {
                if (e.ordinal() == key) {
                    return e;
                }
            }
            throw new IllegalArgumentException("未定义的枚举类型!key=" + key);
        }
	}

	/**
	 * 栏目类型，顺序不可改变
	 */
	public static enum SectionType {
		common, //常用栏目
		timeManagement, //目标管理
		publicInformation, //文化建设
		doc, //知识社区
		formbizconfigs, //表单应用
		forum, //扩展栏目
		meeting,//会议管理
		performanceAnalysis,//协同驾驶舱
		edoc,//公文管理
		collaboration,//协同工作
		vreport, //报表中心
        businessOrderPlatform //应用平台
	}
	/**
	 * 空间授权类型枚举
	 */
	public static enum SecurityType {
        used, //使用权限
        manager, //管理权限
        vistor,//访问权限
    }
	/**
	 * 空间模版授权选人组件页签枚举,只在空间使用
	 */
	public static enum PageSecurityModelShowType{
	    panels,//显示页签
	    selectType,//数据选择
	}
	/**
	 * 所有栏目类型
	 */
	public static List<SectionType> getAllSpaceSectionTypes() {
		List<SectionType> sectionTypes = new ArrayList<SectionType>();
		sectionTypes.add(SectionType.common);//常用
		sectionTypes.add(SectionType.timeManagement);//目标管理
		sectionTypes.add(SectionType.formbizconfigs);//表单应用
		sectionTypes.add(SectionType.meeting);//会议管理
		sectionTypes.add(SectionType.doc);//知识社区
		sectionTypes.add(SectionType.publicInformation);//文化建设
		sectionTypes.add(SectionType.performanceAnalysis);//协同驾驶舱
		sectionTypes.add(SectionType.edoc);//公文管理
		sectionTypes.add(SectionType.forum);//扩展栏目
		sectionTypes.add(SectionType.collaboration);//协同工作
		sectionTypes.add(SectionType.vreport);//报表中心
		return sectionTypes;
	}

    /**
     * 空间－栏目类型
     */
    public static List<SectionType> getSpaceSectionTypes(SpaceType spaceType) {
        User user = AppContext.getCurrentUser();
        List<SectionType> sectionTypes = new ArrayList<SectionType>();
        sectionTypes.add(SectionType.common);//常用

        if (spaceType == SpaceType.related_project_space) {
            return sectionTypes;
        }
        //zhou start
//        if (AppContext.hasPlugin("collaboration") && (!SpaceType.big_screen.equals(spaceType) && !SpaceType.before_login.equals(spaceType) && !SpaceType.v_report.equals(spaceType))) {
//            sectionTypes.add(SectionType.collaboration);//协同工作
//        }
        //zhou end
        if (!user.isGroupAdmin()) {
            if (AppContext.hasPlugin("collaboration") && (!SpaceType.big_screen.equals(spaceType) && !SpaceType.before_login.equals(spaceType) && !SpaceType.v_report.equals(spaceType))) {
                sectionTypes.add(SectionType.collaboration);//协同工作
            }
            sectionTypes.add(SectionType.formbizconfigs);//表单应用
            if (AppContext.hasPlugin("cap4") && !SpaceType.v_report.equals(spaceType) && !SpaceType.before_login.equals(spaceType) && !SpaceType.big_screen.equals(spaceType)) {
                sectionTypes.add(SectionType.businessOrderPlatform);//应用平台
            }
            if (AppContext.hasPlugin("cap4") || AppContext.hasPlugin("excelreport") || AppContext.hasPlugin("filereport") || AppContext.hasPlugin("seeyonreport")) {
                sectionTypes.add(SectionType.vreport);//报表中心
            }
            if (AppContext.hasPlugin("edoc") && (!SpaceType.big_screen.equals(spaceType) && !SpaceType.before_login.equals(spaceType) && !SpaceType.v_report.equals(spaceType))) {
                sectionTypes.add(SectionType.edoc);//公文管理
            }
            if ((AppContext.hasPlugin("project") || AppContext.hasPlugin("taskmanage") || AppContext.hasPlugin("plan") || AppContext.hasPlugin("calendar")) && (!SpaceType.big_screen.equals(spaceType) && !SpaceType.before_login.equals(spaceType) && !SpaceType.v_report.equals(spaceType))) {
                sectionTypes.add(SectionType.timeManagement);//目标管理
            }
            if (AppContext.hasPlugin("meeting") && (!SpaceType.big_screen.equals(spaceType) && !SpaceType.before_login.equals(spaceType) && !SpaceType.v_report.equals(spaceType))) {
                sectionTypes.add(SectionType.meeting);//会议管理
            }
        }
        if (AppContext.hasPlugin("doc") && !SpaceType.v_report.equals(spaceType)) {
            sectionTypes.add(SectionType.doc);//知识社区
        }
        if (AppContext.hasPlugin("news") || AppContext.hasPlugin("bulletin") || AppContext.hasPlugin("bbs") || AppContext.hasPlugin("inquiry") || AppContext.hasPlugin("show")) {
            if (!SpaceType.v_report.equals(spaceType)) {
                sectionTypes.add(SectionType.publicInformation);//文化建设
            }
        }
        boolean showPerformanceAnalysis = "true".equals(SystemProperties.getInstance().getProperty("portal.performanceFlag"));
        if (showPerformanceAnalysis && !SpaceType.before_login.equals(spaceType) && !(SpaceType.big_screen.equals(spaceType) && user.isGroupAdmin())) {
            if (AppContext.hasPlugin("performanceReport") || AppContext.hasPlugin("behavioranalysis") || AppContext.hasPlugin("wfanalysis")) {
                sectionTypes.add(SectionType.performanceAnalysis);// 协同驾驶舱
            }
        }
        if (!SpaceType.vjoinpc.equals(spaceType) && !SpaceType.vjoinmobile.equals(spaceType) && !SpaceType.before_login.equals(spaceType) && !SpaceType.v_report.equals(spaceType)) {
            sectionTypes.add(SectionType.forum);//扩展栏目
        }

        return sectionTypes;
    }

	private static boolean isPersonalSpace(SpaceType spaceType){
        return spaceType == SpaceType.personal || spaceType == SpaceType.personal_custom
                || spaceType == SpaceType.leader || spaceType == SpaceType.outer
                || spaceType == SpaceType.cooperation_work || spaceType == SpaceType.objective_manage
                || spaceType == SpaceType.edoc_manage || spaceType == SpaceType.meeting_manage
                || spaceType == SpaceType.performance_analysis || spaceType == SpaceType.form_application;
	}
	public static enum SpaceTypeClass{
		personal,//个人空间
		corporation,// 协作空间
		public_,//公共空间
	}

	public static boolean isSystem(SpaceType type){
		if(type == null) return false;
		return type != SpaceType.custom && type != SpaceType.Default_personal_custom && type != SpaceType.public_custom && type != SpaceType.public_custom_group;
	}
	/**
	 * 获取各空间个性化的空间前缀
	 */
	public static String getCustomPagePath(SpaceType type){
	    String _pagePath = PERSONAL_FOLDER;
        switch(type){
       //查看 默认个人空间
       case Default_personal:
           break;
       //查看 默认领导空间
       case default_leader:
           _pagePath = LEADER_FOLDER;
           break;
       //查看 默认外部人员空间
       case Default_out_personal:
           _pagePath = OUTER_FOLDER;
           break;
       case outer:
           _pagePath = OUTER_FOLDER;
           break;
       case custom:
           _pagePath = CUSTOM_FOLDER;
           break;
       case public_custom:
           _pagePath = PUBLIC_FOLDER;
           break;
       case public_custom_group:
           _pagePath = PUBLIC_GROUP_FOLDER;
           break;
       //查看 自定义个人空间
       case Default_personal_custom:
           _pagePath = PERSONAL_CUSTOM_FOLDER;
           break;
       case cooperation_work:
           _pagePath = COOPERATION_FOLDER;
           break;
       case performance_analysis:
           _pagePath = PERFORMANCE_FOLDER;
           break;
       case edoc_manage:
           _pagePath = EDOC_FOLDER;
           break;
       case meeting_manage:
           _pagePath = MEETING_FOLDER;
           break;
       case objective_manage:
           _pagePath = OBJECTIVE_FOLDER;
           break;
       case form_application:
           _pagePath = FORMBIZ_FOLDER;
           break;
       case big_screen:
           _pagePath = BIG_SCREEN_PAGE_FOLDER;
           break;
       case before_login:
           _pagePath = BEFORE_LOGIN_PAGE_FOLDER;
           break;
       case m3mobile:
    	   _pagePath = M3MOBILE_FOLDER;
    	   break;
       case m3mobile_corporation:
    	   _pagePath = M3MOBILE_CORPORATION_FOLDER;
    	   break;
       case m3mobile_custom:
    	   _pagePath = M3MOBILE_CUSTOM_FOLDER;
    	   break;
       case m3mobile_leader:
    	   _pagePath = M3MOBILE_LEADER_FOLDER;
    	   break;

       }

        return _pagePath;
	}

    /**
     * 如果是默认空间类型，就转化成对应类型
     * @param defaultSpaceType
     * @return
     */
    public static SpaceType parseDefaultSpaceType(SpaceType defaultSpaceType) {
        switch (defaultSpaceType) {
            case Default_personal:
                return SpaceType.personal;
            case Default_department:
                return SpaceType.department;
            case Default_custom:
                return SpaceType.custom;
            case default_leader:
                return SpaceType.leader;
            case Default_personal_custom:
                return SpaceType.personal_custom;
            case Default_out_personal:
                return SpaceType.outer;
            case m3mobile_custom:
                return SpaceType.m3mobile;
            case m3mobile_leader:
                return SpaceType.m3mobile;
            case m3mobile_leader_custom:
                return SpaceType.m3mobile;
            case m3mobile_corporation:
                return SpaceType.m3mobile;
            case cooperation_work:
                return SpaceType.cooperation_work;
            case performance_analysis:
                return SpaceType.performance_analysis;
            case edoc_manage:
                return SpaceType.edoc_manage;
            case meeting_manage:
                return SpaceType.meeting_manage;
            case objective_manage:
                return SpaceType.objective_manage;
            case form_application:
                return SpaceType.form_application;
            case big_screen:
                return SpaceType.big_screen;
            case before_login:
                return  SpaceType.before_login;
            case vjoinpc_custom:
                return SpaceType.vjoinpc;
            case vjoinmobile_custom:
                return SpaceType.vjoinmobile;
        }
        return defaultSpaceType;
    }

	public static SpaceType getSpaceTypeByClass(String space){
		if(Strings.isBlank(space)){
			return null;
		}
		SpaceTypeClass spaceType = SpaceTypeClass.valueOf(space);
		switch(spaceType){
		case personal:return SpaceType.Default_personal_custom;
		case corporation:return SpaceType.custom;
		case public_:return SpaceType.public_custom;
		}
		return null;
	}
	 public static String getDefaultPagePath(SpaceType type){
		 String _pagePath = DEFAULT_PERSONAL_PAGE_PATH;
		 switch(type){
        //查看 默认个人空间
        case Default_personal:
        	break;
    	//查看 默认领导空间
        case default_leader:
        	_pagePath = DEFAULT_LEADER_PAGE_PATH;
        	break;
    	//查看 默认外部人员空间
        case Default_out_personal:
        	_pagePath = DEFAULT_OUT_PERSONAL_PAGE_PATH;
        	break;
    	//查看 自定义个人空间
        case Default_personal_custom:
        	_pagePath = DEFAULT_CUSTUM_PERSONAL;
        	break;
        case Default_department:
        	_pagePath = DEFAULT_DEPARTMENT_PAGE_PATH;
        	break;
    	//查看 协作空间
        case custom:
        	_pagePath = DEFAULT_CUSTOM_PAGE_PATH;
        	break;
    	//查看 单位空间
        case corporation:
        	_pagePath = DEFAULT_CORPORATION_PAGE_PATH;
        	break;
    	//查看 集团空间
        case group:
        	_pagePath = DEFAULT_GROUP_PAGE_PATH;
        	break;
    	//查看 公共自定义空间
        case public_custom:
        	_pagePath = DEFAULT_PUBLIC_PAGE_PATH;
        	break;
        //查看 大屏空间
        case big_screen:
            _pagePath = DEFAULT_BIG_SCREEN_PAGE_PATH;
            break;
        //查看 登陆前空间
        case before_login:
            _pagePath = DEFAULT_BEFORE_LOGIN_PAGE_PATH;
        case public_custom_group:
        	_pagePath = DEFAULT_PUBLIC_GROUP_PAGE_PATH;
        case v_report:
        	_pagePath = V_REPORT_PAGE_PATH;
        }
		 return _pagePath;
	 }
	/**
	 * 空间状态
	 */
	public static enum SpaceState {
	    normal, // 正常的
		invalidation, // 停用
	}

    public static final String SEEYON_FOLDER                            = PATH_SEPARATOR + "seeyon";

    /**
     * 个人空间文件夹 /seeyon/personal/
     */
    public static final String PERSONAL_FOLDER                          = SEEYON_FOLDER + PATH_SEPARATOR + "personal" + PATH_SEPARATOR;

    /**
     * 部门空间文件夹 /seeyon/department/
     */
    public static final String DEPARTMENT_FOLDER                        = SEEYON_FOLDER + PATH_SEPARATOR + "department" + PATH_SEPARATOR;

    /**
     * 单位空间文件夹 /seeyon/corporation/
     */
    public static final String CORPORATION_FOLDER                       = SEEYON_FOLDER + PATH_SEPARATOR + "corporation" + PATH_SEPARATOR;

    /**
     * 集团空间文件夹 /seeyon/group/
     */
    public static final String GROUP_FOLDER                             = SEEYON_FOLDER + PATH_SEPARATOR + "group" + PATH_SEPARATOR;

    /**
     * 单位自定义空间 /seeyon/public_custom/
     */
    public static final String PUBLIC_FOLDER                            = SEEYON_FOLDER + PATH_SEPARATOR + "public_custom" + PATH_SEPARATOR;
    /**
     * 集团自定义空间 /seeyon/public_custom_group/
     */
    public static final String PUBLIC_GROUP_FOLDER                      = SEEYON_FOLDER + PATH_SEPARATOR + "public_custom_group" + PATH_SEPARATOR;
    /**
     * 自定义空间文件夹 /seeyon/custom/
     */
    public static final String CUSTOM_FOLDER                            = SEEYON_FOLDER + PATH_SEPARATOR + "custom" + PATH_SEPARATOR;

    /**
     * 领导空间文件夹 /seeyon/leader/
     */
    public static final String LEADER_FOLDER                            = SEEYON_FOLDER + PATH_SEPARATOR + "leader" + PATH_SEPARATOR;

    /**
     * 外部人员空间文件夹/seeyon/outer/
     */
    public static final String OUTER_FOLDER                             = SEEYON_FOLDER + PATH_SEPARATOR + "outer" + PATH_SEPARATOR;

    /**
     * 个人自定义空间/seeyon/personal_custom/
     */
    public static final String PERSONAL_CUSTOM_FOLDER                   = SEEYON_FOLDER + PATH_SEPARATOR + "personal_custom" + PATH_SEPARATOR;
    /**
     * 默认的个人空间page path /seeyon/personal/default-page.psml
     */
    public static final String DEFAULT_PERSONAL_PAGE_PATH               = PERSONAL_FOLDER + "default-page.psml";

    /**
     * 默认的部门空间page path /seeyon/department/default-page.psml
     */
    public static final String DEFAULT_DEPARTMENT_PAGE_PATH             = DEPARTMENT_FOLDER + "default-page.psml";

    /**
     * 默认的单位空间page path /seeyon/corporation/default-page.psml
     */
    public static final String DEFAULT_CORPORATION_PAGE_PATH            = CORPORATION_FOLDER + "default-page.psml";

    /**
     * 默认的集团空间page path /seeyon/group/default-page.psml
     */
    public static final String DEFAULT_GROUP_PAGE_PATH                  = GROUP_FOLDER + "default-page.psml";

    /**
     * 默认的自定义空间page path /seeyon/custom/default-page.psml
     */
    public static final String DEFAULT_CUSTOM_PAGE_PATH                 = CUSTOM_FOLDER + "default-page.psml";

    /**
     * 默认的领导空间page path /seeyon/leader/default-page.psml
     */
    public static final String DEFAULT_LEADER_PAGE_PATH                 = LEADER_FOLDER + "default-page.psml";

    /**
     * 默认的外部人员空间 path /seeyon/outer/default-page.psml
     */
    public static final String DEFAULT_OUT_PERSONAL_PAGE_PATH           = OUTER_FOLDER + "default-page.psml";
    /**
     * 默认的个人自定义空间path /seeyon/personal_custom/default-page.psml
     */
    public static final String DEFAULT_CUSTUM_PERSONAL                  = PERSONAL_CUSTOM_FOLDER + "default-page.psml";

    /**
     * 默认的部门主管空间path /seeyon/personal_custom/DeptManager.psml
     */
    public static final String DEFAULT_DEPTMANAGER_PERSONAL             = PERSONAL_CUSTOM_FOLDER + "DeptManager.psml";

    /**
     * 单位自定义空间 path /seeyon/public_custom/default-page.psml
     */
    public static final String DEFAULT_PUBLIC_PAGE_PATH                 = PUBLIC_FOLDER + "default-page.psml";

    /**
     * 集团自定义空间 path /seeyon/group_custom/default-page.psml
     */
    public static final String DEFAULT_PUBLIC_GROUP_PAGE_PATH           = PUBLIC_GROUP_FOLDER + "default-page.psml";

    /**
     * 协同工作文件夹 /seeyon/cooperation
     */
    public static final String COOPERATION_FOLDER                       = SEEYON_FOLDER + PATH_SEPARATOR + "cooperation" + PATH_SEPARATOR;

    /**
     * 表单应用文件夹 /seeyon/formbiz
     */
    public static final String FORMBIZ_FOLDER                           = SEEYON_FOLDER + PATH_SEPARATOR + "formbiz" + PATH_SEPARATOR;

    /**
     * 公文管理文件夹 /seeyon/edoc
     */
    public static final String EDOC_FOLDER                              = SEEYON_FOLDER + PATH_SEPARATOR + "edoc" + PATH_SEPARATOR;

    /**
     * 目标管理文件夹 /seeyon/objective
     */
    public static final String OBJECTIVE_FOLDER                         = SEEYON_FOLDER + PATH_SEPARATOR + "objective" + PATH_SEPARATOR;

    /**
     * 会议管理文件夹 /seeyon/meeting
     */
    public static final String MEETING_FOLDER                           = SEEYON_FOLDER + PATH_SEPARATOR + "meeting" + PATH_SEPARATOR;

    /**
     * 协同驾驶舱文件夹 /seeyon/performance
     */
    public static final String PERFORMANCE_FOLDER                       = SEEYON_FOLDER + PATH_SEPARATOR + "performance" + PATH_SEPARATOR;

    /**
     * V-Join-PC空间文件夹 /seeyon/vjoinpc
     */
    public static final String VJOINPC_FOLDER                           = SEEYON_FOLDER + PATH_SEPARATOR + "vjoinpc" + PATH_SEPARATOR;

    /**
     * V-Join-移动空间文件夹 /seeyon/vjoinmobile
     */
    public static final String VJOINMOBILE_FOLDER                       = SEEYON_FOLDER + PATH_SEPARATOR + "vjoinmobile" + PATH_SEPARATOR;

    /**
     * m3个人空间文件夹 /seeyon/m3mobile
     */
    public static final String M3MOBILE_FOLDER                          = SEEYON_FOLDER + PATH_SEPARATOR + "m3mobile" + PATH_SEPARATOR;

    /**
     * m3个人空间文件夹 /seeyon/m3mobile_custom
     */
    public static final String M3MOBILE_CUSTOM_FOLDER                   = SEEYON_FOLDER + PATH_SEPARATOR + "m3mobile_custom" + PATH_SEPARATOR;

    /**
     * m3领导空间文件夹 /seeyon/m3mobile_leader
     */
    public static final String M3MOBILE_LEADER_FOLDER                   = SEEYON_FOLDER + PATH_SEPARATOR + "m3mobile_leader" + PATH_SEPARATOR;

    /**
     * m3自定义领导空间文件夹 /seeyon/m3mobile_leader_custom
     */
    public static final String M3MOBILE_LEADER_CUSTOM_FOLDER            = SEEYON_FOLDER + PATH_SEPARATOR + "m3mobile_leader_custom" + PATH_SEPARATOR;

    /**
     * m3单位空间文件夹 /seeyon/m3mobile_corporation
     */
    public static final String M3MOBILE_CORPORATION_FOLDER              = SEEYON_FOLDER + PATH_SEPARATOR + "m3mobile_corporation" + PATH_SEPARATOR;

    /**
     * 关联项目应用文件夹 /seeyon/project
     */
    public static final String PROJECT_FOLDER                           = SEEYON_FOLDER + PATH_SEPARATOR + "project" + PATH_SEPARATOR;
    /**
     * 默认的协同工作空间path /seeyon/cooperation/default-page.psml
     */
    public static final String DEFAULT_COOPERATION_PAGE_PATH            = COOPERATION_FOLDER + "default-page.psml";

    /**
     * 默认的表单应用空间path /seeyon/formbiz/default-page.psml
     */
    public static final String DEFAULT_FORMBIZ_PAGE_PATH                = FORMBIZ_FOLDER + "default-page.psml";

    /**
     * 默认的公文管理空间path /seeyon/edoc/default-page.psml
     */
    public static final String DEFAULT_EDOC_PAGE_PATH                   = EDOC_FOLDER + "default-page.psml";

    /**
     * 默认的目标管理空间path /seeyon/objective/default-page.psml
     */
    public static final String DEFAULT_OBJECTIVE_PAGE_PATH              = OBJECTIVE_FOLDER + "default-page.psml";

    /**
     * 默认的会议管理空间path /seeyon/meeting/default-page.psml
     */
    public static final String DEFAULT_MEETING_PAGE_PATH                = MEETING_FOLDER + "default-page.psml";

    /**
     * 默认的协同驾驶舱空间path /seeyon/performance/default-page.psml
     */
    public static final String DEFAULT_PERFORMANCE_PAGE_PATH            = PERFORMANCE_FOLDER + "default-page.psml";

    /**
     * 默认的V-Join-PC空间path /seeyon/vjoinpc/default-page.psml
     */
    public static final String DEFAULT_VJOINPC_PAGE_PATH                = VJOINPC_FOLDER + "default-page.psml";

    /**
     * 默认的V-Join-移动空间path /seeyon/vjoinmobile/default-page.psml
     */
    public static final String DEFAULT_VJOINMOBILE_PAGE_PATH            = VJOINMOBILE_FOLDER + "default-page.psml";

    /**
     * 默认的m3个人空间path /seeyon/m3mobile/default-page.psml
     */
    public static final String DEFAULT_M3MOBILE_PAGE_PATH               = M3MOBILE_FOLDER + "default-page.psml";

    /**
     * 默认的m3自定义个人空间path /seeyon/m3mobile_custom/default-page.psml
     */
    public static final String DEFAULT_M3MOBILE_CUSTOM_PAGE_PATH        = M3MOBILE_CUSTOM_FOLDER + "default-page.psml";

    /**
     * 默认的m3领导空间path /seeyon/m3mobile_leader/default-page.psml
     */
    public static final String DEFAULT_M3MOBILE_LEADER_PAGE_PATH        = M3MOBILE_LEADER_FOLDER + "default-page.psml";

    /**
     * m3自定义领导空间path /seeyon/m3mobile_leader_custom/default-page.psml
     */
    public static final String DEFAULT_M3MOBILE_LEADER_CUSTOM_PAGE_PATH = M3MOBILE_LEADER_CUSTOM_FOLDER + "default-page.psml";
    /**
     * 默认的m3单位空间path /seeyon/m3mobile_corporation/default-page.psml
     */
    public static final String DEFAULT_M3MOBILE_CORPORATION_PAGE_PATH   = M3MOBILE_CORPORATION_FOLDER + "default-page.psml";
    /**
     * 大屏空间path /seeyon/big_screen
     */
    public static final String BIG_SCREEN_PAGE_FOLDER                   = SEEYON_FOLDER + PATH_SEPARATOR + "big_screen" + PATH_SEPARATOR;
    /**
     * 大屏空间path /seeyon/big_screen/default-page.psml
     */
    public static final String DEFAULT_BIG_SCREEN_PAGE_PATH             = BIG_SCREEN_PAGE_FOLDER + "default-page.psml";
    /**
     * 登陆前空间 path /seeyon/before_login/
     */
    public static final String BEFORE_LOGIN_PAGE_FOLDER                 = SEEYON_FOLDER + PATH_SEPARATOR + "before_login" + PATH_SEPARATOR;
    /**
     * 登陆前空间 path /seeyon/before_login/default-page.psml
     */
    public static final String DEFAULT_BEFORE_LOGIN_PAGE_PATH           = BEFORE_LOGIN_PAGE_FOLDER + "default-page.psml";

    /**
     * 报表空间 path /seeyon/v_report/
     */
    public static final String V_REPORT_PAGE_FOLDER                     = SEEYON_FOLDER + PATH_SEPARATOR + "v_report" + PATH_SEPARATOR;
    /**
     * 报表空间 path /seeyon/v_report/default-page.psml
     */
    public static final String V_REPORT_PAGE_PATH                       = V_REPORT_PAGE_FOLDER + "default-page.psml";

    /**
     * 默认的关联项目应用空间path /seeyon/project/default-page.psml
     */
    public static final String DEFAULT_PROJECT_PAGE_PATH                = PROJECT_FOLDER + "default-page.psml";

    public static final String KEY_SLOGAN                               = "space.label.slogan.default";
    public static final String DEFAULT_BANNER                           = "space_banner.gif";

    public static final String resource_main                            = "com.seeyon.v3x.main.resources.i18n.MainResources";
	public static String getValueOfKey(String key){
		return ResourceUtil.getString(key);
	}

	/**
	 * 取到默认的空间名称
	 * @param spaceType
	 * @return
	 */
	public static String getDefaultSpaceName(SpaceType spaceType){
		String key = "seeyon.top." + spaceType + ".space.label";
        if(spaceType.equals(SpaceType.group)){
            key += (String)SysFlag.EditionSuffix.getFlag();
        }
        String value = ResourceUtil.getString(key);
        if(key.equals(value)){
        	return "";
        }
		return value;
	}

	public static String getSpaceName(PortalSpaceFix portalSpaceFix) {
		if (portalSpaceFix == null) {
			return "";
		}

		SpaceType spaceType = EnumUtil.getEnumByOrdinal(SpaceType.class, portalSpaceFix.getType().intValue());
		if (spaceType == SpaceType.Default_department) {
			return getDefaultSpaceName(spaceType);
		}
		if (spaceType == SpaceType.department) {
			//return getDefaultSpaceName(spaceType) + "(" + Functions.getDepartment(spaceFix.getEntityId()).getName() + ")";
			return getDefaultSpaceName(spaceType);
		}

		String name = portalSpaceFix.getSpacename();
		if (Strings.isBlank(name)) {
			switch (spaceType) {
			case personal:
				spaceType = SpaceType.Default_personal;
				break;
			case leader:
				spaceType = SpaceType.default_leader;
				break;
			case outer:
				spaceType = SpaceType.Default_out_personal;
				break;
			case personal_custom:
				spaceType = SpaceType.Default_personal_custom;
				break;
			}
			return getDefaultSpaceName(spaceType);
		}
		return name;
	}

    /**
     * 得到默认口号国际化的key
     * @return
     */
    public static final String getSloganKey(){
        String s = (String)SysFlag.EditionSuffix.getFlag();
        return "space.label.slogan.default" + s;
    }

    public static boolean isPersonalSpace(String spaceType){
    	if(Strings.isNotBlank(spaceType)){
    		SpaceType type = SpaceType.valueOf(spaceType);
    		if(type != null){
    			switch(type){
    			case personal :
    			case leader   :
    			case outer:
    			case personal_custom:
    				return true;
    			}
    		}
    	}
    	return false;
    }

    /**
     * 判断是否个人类型的空间（默认个人空间/自定义个人空间/外部人员空间/自定义外部人员空间/领导空间）
     * @param spaceType
     * @return
     */
    public static boolean isPersonalStyleSpace(int spaceType){
        SpaceType type = SpaceType.getEnumByKey(spaceType);
        switch(type){
            case personal:
            case Default_personal:
            case default_leader:
            case leader:
            case Default_personal_custom:
            case Default_out_personal:
            case personal_custom:
            case outer: return true;
        }
        return false;
    }

    public static boolean isInPersonalClass(SpaceType type){
    	if(type != null){
			switch(type){
			case Default_personal:
			case default_leader:
			case Default_out_personal:
			case Default_personal_custom:
				return true;
			}
    	}
    	return false;
    }

    public static <T> List<T> pagenate(List<T> list) {
		if (null == list || list.size() == 0)
			return new ArrayList<T>();
		Integer first = Pagination.getFirstResult();
		Integer pageSize = Pagination.getMaxResults();
		Pagination.setRowCount(list.size());
		List<T> subList = null;
		if (first + pageSize > list.size()) {
			subList = list.subList(first, list.size());
		} else {
			subList = list.subList(first, first + pageSize);
		}
		return subList;
	}


    /**
     * @author
     * 热点实体类型
     */
    public static enum EntityLevel {
        defaulted, //预置导入
        system, //系统管理员
        group, //集团管理员
        account, //单位管理员
        member; //个人
        public static EntityLevel getEnumByKey(int key) {
            for (EntityLevel e : EntityLevel.values()) {
                if (e.ordinal() == key) {
                    return e;
                }
            }
            throw new IllegalArgumentException("未定义的枚举类型!key=" + key);
        }
    }
    public static void main(String[] args){
    	System.out.println(UUIDLong.absLongUUID());
    }
}
