/**
 * $Author: 
 $
 * $Rev: 
 $
 * $Date:: 2012-06-05 15:14:56#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.ctp.organization.bo;

import com.seeyon.ctp.common.po.BasePO;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.util.UUIDLong;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>Title:BO对象实体基类</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 */

public abstract class V3xOrgEntity extends com.seeyon.ctp.util.ObjectToXMLBase implements Serializable {

    private static final long serialVersionUID = -4198484799949590797L;

    public static final String ORGACCOUNT_PATH = "0";

	public static final String DEP_PATH_DELIMITER = ".";

	public static final String ORG_ID_DELIMITER = ",";

	public static final String ROLE_ID_DELIMITER = "_";

	public static final long DEFAULT_NULL_ID = -1;

	public static final String DEFAULT_EMPTY_STRING = "";

	public static final Long SORT_START_NUMBER = 0L;

	public static final Long SORT_STEP_NUMBER = 1L;

	public static final byte ROLETYPE_FIXROLE = 1;

	public static final byte ROLETYPE_RELATIVEROLE = 2;

	public static final byte ROLETYPE_USERROLE = 3;
	
	@Deprecated
	private static final byte ROLETYPE_PLUGIN = 4; // 插件角色
	
	public static final byte ROLETYPE_REPORT = 5;//通过接口创建的自定义的报表角色
	
	/* 实体类型定义 */
    public static final String ORGENT_TYPE_ACCOUNT = "Account";
    
    public static final String ORGENT_TYPE_JOINACCOUNTTAG = "JoinAccountTag";
    
    public static final String ORGENT_START_TYPE_ACCOUNT = "startaccount";

    public static final String ORGENT_TYPE_DEPARTMENT = "Department";

    public static final String ORGENT_TYPE_TEAM = "Team";

    public static final String ORGENT_TYPE_MEMBER = "Member";

    public static final String ORGENT_TYPE_ROLE = "Role";

    public static final String ORGENT_TYPE_POST = "Post";

    public static final String ORGENT_TYPE_LEVEL = "Level";
    
    public static final String ORGENT_TYPE_DUTYLEVEL = "DutyLevel"; //政务版--职级
    
    public static final String ORGREL_TYPE_DEP_ROLE = "Department_Role";
    
    public static final String ORGREL_TYPE_ACCOUNT_ROLE = "Account_Role";
    
    public static final String ORGREL_TYPE_DEP_POST = "Department_Post";
    
	//表示系统管理员所在的单位的ID.同时在工作流里面
	//表示全局单位
    /**
     * 系统管理员所在的单位的Id，表示全集团。
     */
	public static final Long VIRTUAL_ACCOUNT_ID = 1L;
	//表示无组织ID
	public static final long NULL_ACCOUNT_ID = 0;


	public static final int ROLE_ADMIN = 6;
	// 系统管理员
	public static final String CONFIG_SYSTEM_ADMIN_CATEGORY="v3xorg_system_admin_definition";
	public static final String CONFIG_SYSTEM_ADMIN_NAME="system_login_name";
	public static final long CONFIG_SYSTEM_ADMIN_ID = 1L;
	//审计管理员
	public static final String CONFIG_AUDIT_ADMIN_CATEGORY="v3xorg_audit_admin_definition";
	public static final String CONFIG_AUDIT_ADMIN_NAME="audit_login_name";
	public static final long CONFIG_AUDIT_ADMIN_ID = 0L;

	//系統自動觸發
	public static final String CONFIG_SYSTEM_AUTO_TRIGGER_CATEGORY="v3xorg_system_auto_trigger_definition";
	public static final String CONFIG_SYSTEM_AUTO_TRIGGER_NAME="system_auto_trigger_name";
	public static final long CONFIG_SYSTEM_AUTO_TRIGGER_ID = 2L;
	
	//工资管理员发系统消息ID为8L
	public static final long CONFIG_SALARY_ADMIN_TRIGGER_ID = 7974674186147152704L;
	
	//智能推送消息，发送人员ID
	public static final long CONFIG_SYSTEM_AI_PUSH_ID = 8116875651793571198L;
	
	public static final String ORGENT_TYPE_DYNAMIC_ROLE = "Role"; // 动态角色
	
	/* 扩展属性 */
	public static final String ORGPROPERTY_CATEGORY = "v3xorg_property_definition"; //扩展属性类别
	public static final String ORGPROPERTY_CATEGORY_PREFIX = "v3xorg_prpprefix_"; 	//单位扩展属性类别前缀
	public static final int    ORGPROPERTY_TYPE_ACCOUNT = 1; 
	public static final int    ORGPERM_TYPE_MEMBER = 2;

	/* 元数据的KEY */
	public static final String ORGENT_META_KEY_DEFAULTPWD = "DefaultPWD";

	public static final String ORGENT_META_KEY_ORGROLE = "OrganizationRoles";
    
	public static final char MEMBER_TYPE_REGULAR = 1;
	public static final int MEMBER_GENDER_NULL = -1; //无性别
	public static final int MEMBER_GENDER_MALE = 1;  //男性
	public static final int MEMBER_GENDER_FEMALE = 2; //女性

	/* 岗位类型常量 */
	public static final int POST_TYPE_ACCOUNT = 1; //单位自建岗
	public static final int POST_TYPE_GROUP = 2; //集团基准岗
	
	public static final int ROLE_BOND_USER = 3;
	
	
	/* 单位的访问权限 */
	public static final int ACCOUNT_ACC_ALL		= 0;		//全部
	public static final int ACCOUNT_ACC_NBR_SUB_SUP = 1;		//上级、下级、平级	
	public static final int ACCOUNT_ACC_SUP		= 2;		//上级
	public static final int ACCOUNT_ACC_SUP_NBR	= 3;		//上级、平级
	public static final int ACCOUNT_ACC_SUP_SUB	= 4;		//上级、下级
	public static final int ACCOUNT_ACC_NBR		= 5;		//平级
	public static final int ACCOUNT_ACC_NBR_SUB	= 6;		//平级、下级	
	public static final int ACCOUNT_ACC_SUB		= 7;		//下级	
	public static final int ACCOUNT_ACC_NON		= 8;		//全否（不能访问）

	public static final int MAX_LEVEL_NUM = 100;  //职务级别序号的最大值
	
	/**
     * toXML的节点属性名称，用缩写，减负
     */
    public static final String TOXML_PROPERTY_ENTITY_TYPE = "ET";
    public static final String TOXML_PROPERTY_NAME = "N";
    public static final String TOXML_PROPERTY_isInternal = "I";
    public static final String TOXML_PROPERTY_externalType = "E";
    public static final String TOXML_PROPERTY_id = "K";
    public static final String TOXML_PROPERTY_parentId = "PK";
    public static final String TOXML_PROPERTY_Code = "C";
    public static final String TOXML_PROPERTY_Email = "Y";
    public static final String TOXML_PROPERTY_Mobile = "M";
	
	//Field
	
	protected Long orgAccountId;
	
	protected Long id;

	protected String name;
	
	protected String code = "";

	protected Date createTime = new Date();

	protected Date updateTime= new Date();

	protected Long sortId = SORT_START_NUMBER;
	
	protected Boolean isDeleted = false; 	//是否已经被删除
	
	protected Boolean enabled = true;
	
	protected Integer  externalType     = OrgConstants.ExternalType.Inner.ordinal();
	
	protected Integer status = OrgConstants.ORGENT_STATUS.NORMAL.ordinal();
	
	protected String exter = "";
	
	protected String description = "";

	protected Boolean create263=false;

	public Boolean getCreate263() {
		return create263;
	}

	public void setCreate263(Boolean create263) {
		this.create263 = create263;
	}

	public abstract String getEntityType();
	
	/**
	 * 判断实体是否有效
	 * @return
	 */
	public abstract boolean isValid();
	
	public abstract V3xOrgEntity fromPO(BasePO po);
	
	public abstract BasePO toPO();
    
    /**
     * 取得实体的Id。
     * @return 实体Id，唯一标识实体。
     */
    public Long getId() {
        return id;
    }

    public void setIdIfNew() {
        if(this.id == null){
            this.id = UUIDLong.longUUID();
        }
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
	/**
	 * 取得实体的创建时间。
	 * @return 创建时间。
	 */
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	/**
	 * 取得实体的最后更新时间。
	 * @return 最后更新时间。
	 */
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	/**
	 * 取得实体所在单位的Id。
	 * @return 所在单位Id。
	 */
	public Long getOrgAccountId() {
		return orgAccountId;
	}
	/**
	 * 设置所在单位。
	 * @param orgAccountId 所在单位Id。
	 */
	public void setOrgAccountId(Long orgAccountId) {
		this.orgAccountId = orgAccountId;
	}
	/**
	 * 取得实体名称。
	 * @return 实体名称。
	 */
	public String getName() {
		return name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 设置实体名称。
	 * @param name 实体名称。
	 */
	public void setName(String name) {
		if(name==null) {
			this.name = name;
		}
		else{
			this.name = name.trim();
		}
	}
	
	public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
	 * 取得排序号。
	 * @return 排序号。
	 */
	public Long getSortId() {
		return sortId;
	}
	/**
	 * 设置排序号。
	 * @param sortId 排序号。
	 */
	public void setSortId(Long sortId) {
		this.sortId = sortId;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	/**
	 * 取得实体的启用/停用状态。
	 * @return 启用则返回<CODE>true</CODE>，否则返回<CODE>false</CODE>。
	 */
	public Boolean getEnabled() {
		return enabled;
	}
	/**
	 * 启用/停用实体。
	 * @param enabled 为<CODE>true</CODE>启用，<CODE>false</CODE>停用。
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public Integer getExternalType() {
		if(externalType==null){
			externalType = OrgConstants.ExternalType.Inner.ordinal();
		}
		return externalType;
	}

	public void setExternalType(Integer externalType) {
		this.externalType = externalType;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Object other){
		if(!(other instanceof V3xOrgEntity)){
			return false;
		}
		
		if (this == other) {
			return true;
		}
		
		if(this.getId() == null){
			return false;
		}
		
		return this.getId().equals(((V3xOrgEntity)other).getId());
	}
    
    public int hashCode() {
        return this.getId().hashCode();
    }
    
    public static final ToStringStyle v3xToStringStyle = new V3xToStringStyle();

    private static final class V3xToStringStyle extends ToStringStyle {
        private static final long serialVersionUID = -6192155606714372299L;

        private V3xToStringStyle() {
            super();
        }

        public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
            if (value != null) {
                appendFieldStart(buffer, fieldName);
                appendInternal(buffer, fieldName, value, isFullDetail(fullDetail));
                appendFieldEnd(buffer, fieldName);
            }
        }

        private Object readResolve() {
            return BasePO.v3xToStringStyle;
        }
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, v3xToStringStyle);
    }

}