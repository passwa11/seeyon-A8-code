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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.addressbook.constants.AddressbookConstants;
import com.seeyon.apps.addressbook.po.AddressBookSet;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.BasePO;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.dao.OrgCache;
import com.seeyon.ctp.organization.dao.OrgHelper;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;

/**
 * <p>Title: 人员实体类BO对象</p>
 * <p>Description: 代码描述</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 */
public class V3xOrgMember extends V3xOrgEntity implements Serializable {

    private static final long   serialVersionUID = -1332273704873347989L;
    private final static Log   logger    = LogFactory.getLog(V3xOrgMember.class);

    private java.lang.Long      orgLevelId       = -1L;
    private java.lang.Long      orgPostId        = -1L;
    private java.lang.Long      orgDepartmentId  = -1L;
    private java.lang.Integer   type             = OrgConstants.MEMBER_TYPE.FORMAL.ordinal();  //默认人员正常
    private Boolean             isInternal       = true;                                       //是否为内部人员
    private Boolean             isLoginable      = true;                                       //是否为能登录账号
    private Boolean             isVirtual        = false;                                      //是否为虚拟账号
    private Boolean             isAssigned       = true;                                       //是否已经分配到某个单位
    private Boolean             isAdmin          = false;                                      //人员是否有效（综合判断）
    private Boolean             isValid          = false;
	private int                 state            = OrgConstants.MEMBER_STATE.ONBOARD.ordinal();
    private Map<String, Object> properties       = new HashMap<String, Object>();              //扩展属性, key: 
    /** 人员副岗   不允许直接操作它, 必须使用 {@link #getSecond_post()} */
    private List<MemberPost>    second_post      = null;                                      
    private List<MemberPost>    concurrent_post  = null;                                       //人员兼职
    private V3xOrgPrincipal     v3xOrgPrincipal;
    //存放自定义通讯录字段的list
  	private List<String> customerAddressBooklist=new ArrayList<String>();
	//首选语言
	private String primaryLanguange = null;
	
	private String pinyin;
	
	private String pinyinhead;

	private Boolean create263=false;

    public Boolean getCreate263() {
        return create263;
    }

    public void setCreate263(String create263) {
        this.create263 = create263.equals("true")?true:false;
    }

    /**
     * 复制传入的实体的属性值到Member的实例。
     * @param orgMember
     */
    public V3xOrgMember(V3xOrgMember orgMember) {
        this.id = orgMember.getId();
        this.createTime = orgMember.getCreateTime();
        this.orgAccountId = orgMember.getOrgAccountId();
        this.orgLevelId = orgMember.getOrgLevelId();
        this.orgPostId = orgMember.getOrgPostId();
        this.type = orgMember.getType();
        this.code = orgMember.getCode();
        this.name = orgMember.getName();
        this.sortId = orgMember.getSortId();
        this.description = orgMember.getDescription();
        this.isAssigned = orgMember.getIsAssigned();
        this.updateTime = orgMember.getUpdateTime();
        this.isInternal = orgMember.getIsInternal();
        this.orgDepartmentId = orgMember.getOrgDepartmentId();
        this.state = orgMember.getState();
        this.isAdmin = orgMember.getIsAdmin();
        this.status = orgMember.getStatus();
        this.isDeleted = orgMember.getIsDeleted();
        this.isLoginable = orgMember.getIsLoginable();
        this.isVirtual = orgMember.getIsVirtual();
        this.enabled = orgMember.getEnabled();
        this.externalType = orgMember.getExternalType();
        this.create263 = orgMember.getCreate263();

        if(orgMember.second_post != null){
            this.second_post = new ArrayList<MemberPost>(orgMember.second_post);
        }
        
        if(orgMember.concurrent_post != null){
            this.concurrent_post = new ArrayList<MemberPost>(orgMember.concurrent_post);
        }
        
        this.pinyin = orgMember.getPinyin();
        this.pinyinhead = orgMember.getPinyinhead();
        
        this.properties.putAll(orgMember.properties);
    }

    public V3xOrgMember() {
    }

    public V3xOrgMember(OrgMember orgMember) {
        this.fromPO(orgMember);
    }

    public V3xOrgEntity fromPO(BasePO po) {
        OrgMember orgMember = (OrgMember) po;
        this.id = orgMember.getId();
        this.createTime = orgMember.getCreateTime();
        this.orgAccountId = orgMember.getOrgAccountId();
        this.orgLevelId = orgMember.getOrgLevelId();
        this.orgPostId = orgMember.getOrgPostId();
        this.type = orgMember.getType();
        this.code = orgMember.getCode();
        this.name = orgMember.getName();
        this.sortId = orgMember.getSortId();
        this.description = orgMember.getDescription();
        this.isAssigned = orgMember.isAssigned();
        this.updateTime = orgMember.getUpdateTime();
        this.isInternal = orgMember.isInternal();
        this.orgDepartmentId = orgMember.getOrgDepartmentId();
        this.state = orgMember.getState();
        this.isAdmin = orgMember.isAdmin();
        this.status = orgMember.getStatus();
        this.isDeleted = orgMember.isDeleted();
        this.isLoginable = orgMember.isLoginable();
        this.isVirtual = orgMember.isVirtual();
        this.enabled = orgMember.isEnable();
        this.externalType = orgMember.getExternalType();
        this.pinyin = this.getPinyin();
        this.pinyinhead = this.getPinyinhead();

        this.properties.put("EXT_ATTR_1", orgMember.getExtAttr1());
        this.properties.put("EXT_ATTR_2", orgMember.getExtAttr2());
        this.properties.put("EXT_ATTR_3", orgMember.getExtAttr3());
        this.properties.put("EXT_ATTR_4", orgMember.getExtAttr4());
        this.properties.put("EXT_ATTR_5", orgMember.getExtAttr5());
        this.properties.put("EXT_ATTR_6", orgMember.getExtAttr6());
        this.properties.put("EXT_ATTR_7", orgMember.getExtAttr7());
        this.properties.put("EXT_ATTR_8", orgMember.getExtAttr8());
        this.properties.put("EXT_ATTR_9", orgMember.getExtAttr9());
        this.properties.put("EXT_ATTR_10", orgMember.getExtAttr10());
        this.properties.put("EXT_ATTR_11", orgMember.getExtAttr11()==null?0:orgMember.getExtAttr11());
        this.properties.put("EXT_ATTR_12", orgMember.getExtAttr12()==null?0:orgMember.getExtAttr12());
        this.properties.put("EXT_ATTR_13", orgMember.getExtAttr13()==null?0:orgMember.getExtAttr13());
        this.properties.put("EXT_ATTR_14", orgMember.getExtAttr14()==null?0:orgMember.getExtAttr14());
        this.properties.put("EXT_ATTR_15", orgMember.getExtAttr15()==null?0:orgMember.getExtAttr15());
        this.properties.put("EXT_ATTR_16", orgMember.getExtAttr16());
        this.properties.put("EXT_ATTR_17", orgMember.getExtAttr17());
        this.properties.put("EXT_ATTR_18", orgMember.getExtAttr18());
        this.properties.put("EXT_ATTR_19", orgMember.getExtAttr19());
        this.properties.put("EXT_ATTR_20", orgMember.getExtAttr20());
        this.properties.put("EXT_ATTR_21", orgMember.getExtAttr21());
        this.properties.put("EXT_ATTR_22", orgMember.getExtAttr22());
        this.properties.put("EXT_ATTR_23", orgMember.getExtAttr23());
        this.properties.put("EXT_ATTR_24", orgMember.getExtAttr24());
        this.properties.put("EXT_ATTR_25", orgMember.getExtAttr25());
        this.properties.put("EXT_ATTR_26", orgMember.getExtAttr26());
        this.properties.put("EXT_ATTR_27", orgMember.getExtAttr27());
        this.properties.put("EXT_ATTR_28", orgMember.getExtAttr28());
        this.properties.put("EXT_ATTR_29", orgMember.getExtAttr29());
        this.properties.put("EXT_ATTR_30", orgMember.getExtAttr30());
        this.properties.put("EXT_ATTR_31", orgMember.getExtAttr31());
        this.properties.put("EXT_ATTR_32", orgMember.getExtAttr32());
        this.properties.put("EXT_ATTR_33", orgMember.getExtAttr33());
        this.properties.put("EXT_ATTR_34", orgMember.getExtAttr34());
        this.properties.put("EXT_ATTR_35", orgMember.getExtAttr35());       
        this.properties.put("EXT_ATTR_36", orgMember.getExtAttr36());
        this.properties.put("EXT_ATTR_37", orgMember.getExtAttr37());
        
        return this;
    }

    public BasePO toPO() {
        OrgMember o = new OrgMember();
        o.setId(this.id);
        o.setCreateTime(this.createTime);
        o.setOrgAccountId(this.orgAccountId);
        o.setOrgLevelId(this.orgLevelId);
        o.setOrgPostId(this.orgPostId);
        o.setType(this.type);
        o.setCode(this.code);
        o.setName(this.name);
        o.setSortId(this.sortId.longValue());
        o.setDescription(this.description);
        o.setAssigned(this.isAssigned);
        o.setUpdateTime(this.updateTime);
        o.setInternal(this.isInternal);
        o.setOrgDepartmentId(this.orgDepartmentId);
        o.setState(this.state);
        o.setAdmin(this.isAdmin);
        o.setStatus(this.status);
        o.setDeleted(this.isDeleted);
        o.setLoginable(this.isLoginable);
        o.setVirtual(this.isVirtual);
        o.setEnable(this.enabled);
        o.setExternalType(this.externalType);
        o.setCreate263(this.create263);

        o.setExtAttr1((String) this.properties.get("EXT_ATTR_1"));
        o.setExtAttr2((String) this.properties.get("EXT_ATTR_2"));
        o.setExtAttr3((String) this.properties.get("EXT_ATTR_3"));
        o.setExtAttr4((String) this.properties.get("EXT_ATTR_4"));
        o.setExtAttr5((String) this.properties.get("EXT_ATTR_5"));
        o.setExtAttr6((String) this.properties.get("EXT_ATTR_6"));
        o.setExtAttr7((String) this.properties.get("EXT_ATTR_7"));
        o.setExtAttr8((String) this.properties.get("EXT_ATTR_8"));
        o.setExtAttr9((String) this.properties.get("EXT_ATTR_9"));
        o.setExtAttr10((String) this.properties.get("EXT_ATTR_10"));
        //o.setExtAttr11(Integer.valueOf((String)this.properties.get("EXT_ATTR_11")));
        o.setExtAttr11(Integer.valueOf(this.properties.get("EXT_ATTR_11")==null?"-1":this.properties.get("EXT_ATTR_11").toString()));//gender
        o.setExtAttr12(Integer.valueOf(this.properties.get("EXT_ATTR_12")==null?"0":this.properties.get("EXT_ATTR_12").toString()));
        o.setExtAttr13(Integer.valueOf(this.properties.get("EXT_ATTR_13")==null?"0":this.properties.get("EXT_ATTR_13").toString()));
        o.setExtAttr14(Integer.valueOf(this.properties.get("EXT_ATTR_14")==null?"0":this.properties.get("EXT_ATTR_14").toString()));
        o.setExtAttr15(Integer.valueOf(this.properties.get("EXT_ATTR_15")==null?"0":this.properties.get("EXT_ATTR_15").toString()));
        o.setExtAttr16((Long) this.properties.get("EXT_ATTR_16"));
        o.setExtAttr17((Long) this.properties.get("EXT_ATTR_17"));
        o.setExtAttr18((Long) this.properties.get("EXT_ATTR_18"));
        o.setExtAttr19((Long) this.properties.get("EXT_ATTR_19"));
        o.setExtAttr20((Long) this.properties.get("EXT_ATTR_20"));
        o.setExtAttr21((Date) this.properties.get("EXT_ATTR_21"));
        o.setExtAttr22((Date) this.properties.get("EXT_ATTR_22"));
        o.setExtAttr23((Date) this.properties.get("EXT_ATTR_23"));
        o.setExtAttr24((Date) this.properties.get("EXT_ATTR_24"));
        o.setExtAttr25((Date) this.properties.get("EXT_ATTR_25"));
        o.setExtAttr26((Date) this.properties.get("EXT_ATTR_26"));
        o.setExtAttr27((Date) this.properties.get("EXT_ATTR_27"));
        o.setExtAttr28((Date) this.properties.get("EXT_ATTR_28"));
        o.setExtAttr29((Date) this.properties.get("EXT_ATTR_29"));
        o.setExtAttr30((Date) this.properties.get("EXT_ATTR_30"));
        o.setExtAttr31((String) this.properties.get("EXT_ATTR_31"));
        o.setExtAttr32((String) this.properties.get("EXT_ATTR_32"));
        o.setExtAttr33((String) this.properties.get("EXT_ATTR_33"));
        o.setExtAttr34((String) this.properties.get("EXT_ATTR_34"));
        o.setExtAttr35((String) this.properties.get("EXT_ATTR_35"));    
        o.setExtAttr36((String) this.properties.get("EXT_ATTR_36"));
        o.setExtAttr37((Long) this.properties.get("EXT_ATTR_37"));
        
        //TODO 统一做为空的防护为
        return o;
    }

    /**
     * 根据实体添加人员副岗
     * @param memberPost
     */
    public void addSecondPost(MemberPost memberPost) {
        if(this.second_post == null){
            this.second_post = new ArrayList<MemberPost>();
        }
        
        if (memberPost.getMemberId().equals(id)) {
            if (!this.second_post.contains(memberPost)) {
                this.second_post.add(memberPost);
            }
        }
    }

    /**
     * 根据部门及岗位ID添加副岗
     * @param deptId
     * @param postId
     */
    public void addSecondPost(Long deptId, Long postId) {
        MemberPost memberPost = new MemberPost();
        memberPost.setMemberId(this.getId());
        memberPost.setDepId(deptId);
        memberPost.setPostId(postId);
        memberPost.setSortId(this.getSortId());
        //利用该接口创建人员副岗需要补充单位ID
        memberPost.setOrgAccountId(this.getOrgAccountId());
        memberPost.setType(OrgConstants.MemberPostType.Second);

        addSecondPost(memberPost);
    }

    /**
     * 根据部门ID查找副岗
     * @param deptId
     * @return List
     */
    public List<MemberPost> getSecondPostByDeptId(Long deptId) {
        List<MemberPost> mpList = new ArrayList<MemberPost>();
        for (MemberPost mp : getSecond_post()) {
            if (mp.getDepId().equals(deptId)) {
                mpList.add(mp);
            }
        }

        return mpList;
    }

    /**
     * 根据岗位ID查找副岗
     * @param postId
     * @return List
     */
    public List<MemberPost> getSecondPostByPostId(Long postId) {
        List<MemberPost> mpList = new ArrayList<MemberPost>();
        for (MemberPost mp : getSecond_post()) {
            if (mp.getPostId().equals(postId)) {
                mpList.add(mp);
            }
        }

        return mpList;
    }

    public List<MemberPost> getSecond_post() {
        if(null == second_post) {
            try {
                second_post = OrgHelper.getOrgManager().getMemberSecondPosts(id);
            }
            catch (Exception e) {
                logger.error("",e);
                return new ArrayList<MemberPost>(0);
            }
        }
        
        return new ArrayList<MemberPost>(second_post);
    }
    
    public List<MemberPost> getConcurrent_post() {
        if(null == concurrent_post) {
            try {
                concurrent_post = OrgHelper.getOrgManager().getMemberConcurrentPosts(id);
        		Collections.sort(concurrent_post, new Comparator<MemberPost>() {
                    public int compare(MemberPost o1, MemberPost o2) {
                        return o2.getCreateTime().compareTo(o1.getCreateTime());
                    }
                });
            }
            catch (Exception e) {
                logger.error("",e);
                return new ArrayList<MemberPost>(0);
            }
        }
        
        return new ArrayList<MemberPost>(concurrent_post);
    }
    public Boolean getIsValid() {
    	this.isValid = !isDeleted && enabled && isLoginable && isAssigned && state == OrgConstants.MEMBER_STATE.ONBOARD.ordinal();
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}
	
	
	/**
	 * 取得实体名称。
	 * @return 实体名称。
	 */
	public String getName() {
		if(this.id == OrgConstants.GUEST_ID){
			this.name =  ResourceUtil.getString("org.guest.default.account.name");
		}
		return this.name;
	}
	
	public void setName(String name) {
		if(name==null) {
			this.name = name;
		}
		else{
			this.name = name.trim();
		}
		
		if(this.id == OrgConstants.GUEST_ID){
			this.name =  ResourceUtil.getString("org.guest.default.account.name");
		}
	}
	
	/**
	 * 纯内部人员
	 * @return
	 */
    public Boolean getIsInternal() {
        return this.isInternal;
    }
    /**
     * v5编外人员
     * @return
     */
    public Boolean isV5External(){
    	return !this.getIsInternal() && Integer.valueOf(OrgConstants.ExternalType.Inner.ordinal()).equals(this.getExternalType());
    }
    
    /**
     * v-join人员
     * @return
     */
    public Boolean isVJoinExternal(){
    	return !this.getIsInternal() && Integer.valueOf(OrgConstants.ExternalType.Interconnect1.ordinal()).equals(this.getExternalType());
    }
    
    /**
     * 特殊账号（guest和大屏）
     * @return
     */
    public Boolean isGuest(){
    	return !this.getIsInternal() && Integer.valueOf(OrgConstants.ExternalType.Interconnect2.ordinal()).equals(this.getExternalType());
    }
    
    /**
     * 是否登录前账号
     * @return
     */
    public Boolean isDefaultGuest(){
    	return isGuest() && this.id == OrgConstants.GUEST_ID;
    }
    
    /**
     * 是否大屏账号
     * @return
     */
    public Boolean isScreenGuest(){
    	return isGuest() && this.id != OrgConstants.GUEST_ID;
    }

    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }

    public Long getOrgDepartmentId() {
        return this.orgDepartmentId;
    }

    public void setOrgDepartmentId(Long orgDepartmentId) {
        this.orgDepartmentId = orgDepartmentId;
    }

    public Long getOrgLevelId() {
        return this.orgLevelId;
    }

    public void setOrgLevelId(Long orgLevelId) {
        this.orgLevelId = orgLevelId;
    }

    public Long getOrgPostId() {
        return this.orgPostId;
    }

    public void setOrgPostId(Long orgPostId) {
        this.orgPostId = orgPostId;
    }

    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getEntityType() {
    	if(this.isGuest()){
    		return "Guest";
    	}
        return OrgConstants.ORGENT_TYPE.Member.name();
    }

    public boolean isValid() {
        return (!isDeleted && enabled && isLoginable && isAssigned && state == OrgConstants.MEMBER_STATE.ONBOARD.ordinal());
    }

    /**
     * 设置属性，如果改key不在已有的key列表中，则添加该key到列表中
     * 
     * @param key 如emailaddress，telnumber
     * @param value
     * @throws BusinessException
     */
    public void setProperty(String key, Object value) {
        Map<String, String> attrMap = OrgHelper.getMemberExtAttrKeyMaps();
        String poKey = attrMap.get(key);
        if (poKey == null) {
            throw new IllegalArgumentException(key);
        }

        this.properties.put(poKey, value);
    }

    /**
     * 获得一个属性，如果没有该Key，返回null
     * 
     * @param key 如emailaddress，telnumber
     * @return
     */
    public Object getProperty(String key) {
        return OrgHelper.getMemberExtAttr(this, key);
    }

    /**
     * key:  如emailaddress，telnumber
     * 
     * @return
     */
    public Map<String, Object> getProperties() {
        return OrgHelper.getMemberExtAttrs(this);
    }

    /**
     * 得到手机号
     * emailaddress
     * @return
     */
    public String getTelNumber() {
        return (String) this.properties.get("EXT_ATTR_1");
    }

    /**
     * 替换原有所有属性
     * 
     * @param properties key:  如emailaddress，telnumber
     */
    public void setProperties(Map<String, Object> properties) {
        Map<String, Object> newProperties = new HashMap<String, Object>();

        Map<String, String> attrMap = OrgHelper.getMemberExtAttrKeyMaps();
        for (Iterator<Map.Entry<String, String>> iterator = attrMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String poKey = entry.getValue();
            if(properties.containsKey(key)) {//OA-61081
                newProperties.put(poKey, properties.get(key));
            }
        }

        this.properties.putAll(newProperties);
    }

    /**
     * 应用层不要调用
     * 
     * @param poKey ExtAttr1 ...... ExtAttr30
     * @return
     */
    public Object getPOProperties(String poKey) {
        return properties.get(poKey);
    }

    /**
     * 给选人界面用的，不要轻易修改
     * {K:"6998870676086867221",N:"谭敏锋",S:12,P:4,L:10,D:2,F:[[3,4],[5,6]]}
     * 
     * @param o
     * @param orgManager
     * @param needMobile 是否需要手机号，当没有短信插件的时候就不需要了
     */
    public void toJsonString(StringBuilder o, OrgCache orgCache, boolean needMobile, List<MemberPost> secondPost,AddressBookSet addressBookSet) {
        int depHash = -1;
        int levelHash = -1;
        int postHash = -1;

        try {
            V3xOrgDepartment dep = orgCache.getV3xOrgEntityNoClone(V3xOrgDepartment.class, this.getOrgDepartmentId());
            if (dep != null) {
                depHash = dep.makeLiushuihao();
            }

            if (this.getIsInternal() || this.isVJoinExternal()) {
                V3xOrgLevel level = orgCache.getV3xOrgEntityNoClone(V3xOrgLevel.class, this.getOrgLevelId());
                if (level != null) {
                    levelHash = level.makeLiushuihao();
                }
                if (this.getOrgPostId() != -1) {
                    V3xOrgPost post = orgCache.getV3xOrgEntityNoClone(V3xOrgPost.class, this.getOrgPostId());
                    if (post != null) {
                        postHash = post.makeLiushuihao();
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(this.toString(), e);
        }

        o.append("{");
        o.append(TOXML_PROPERTY_id).append(":'").append(this.getId()).append("'");
        o.append(",S:").append(this.getSortId());
        if (postHash != -1) {
            o.append(",P:").append(postHash);
        }
        if (levelHash != -1) {
            o.append(",L:").append(levelHash);
        }
        if (depHash != -1) {
            o.append(",D:").append(depHash);
        }

        if (!this.getIsInternal()) {
            o.append(",").append(TOXML_PROPERTY_isInternal).append(":0");
        }

        o.append(",").append(TOXML_PROPERTY_externalType).append(":'").append(this.getExternalType()).append("'");
        o.append(",").append(TOXML_PROPERTY_NAME).append(":'").append(Strings.escapeJavascript(this.getName())).append("'");

        String emailAddress = this.getEmailAddress();
        String telNumber = this.getTelNumber();

        if (Strings.isNotBlank(emailAddress)) {
            o.append(",").append(TOXML_PROPERTY_Email).append(":'").append(Strings.escapeJavascript(emailAddress)).append("'");
        }
        if (needMobile && Strings.isNotBlank(telNumber)) {
            if (addressBookSet != null && !OrgHelper.getAddressBookManager().checkPhone(AppContext.currentUserId(), this.getId(), this.getOrgAccountId(), addressBookSet)) {
                o.append(",").append(TOXML_PROPERTY_Mobile).append(":'").append(AddressbookConstants.ADDRESSBOOK_INFO_REPLACE).append("'");
            }else{
            	o.append(",").append(TOXML_PROPERTY_Mobile).append(":'").append(Strings.escapeJavascript(telNumber)).append("'");
            }
        }

        if (Strings.isNotEmpty(secondPost)) {
            o.append(",F:[");
            int i = 0;
            for (MemberPost memberPost : secondPost) {
                try {
                    V3xOrgDepartment dep = orgCache.getV3xOrgEntityNoClone(V3xOrgDepartment.class, memberPost.getDepId());
                    if (dep != null) {
                        V3xOrgPost post = orgCache.getV3xOrgEntityNoClone(V3xOrgPost.class, memberPost.getPostId());
                        if (post != null) {
                            if (i++ != 0) {
                                o.append(",");
                            }
                            o.append("[");
                            o.append(dep.makeLiushuihao());
                            o.append(",");
                            o.append(post.makeLiushuihao());
                            o.append("]");
                        }
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(this.toString(), e);
                }
            }
            o.append("]");
        }

        o.append("}");
    }
    
    
	/**
	 * 特殊账号
	 * 给选人界面用的，不要轻易修改
	 */
	public void toGuestJsonString(StringBuilder o) {
		if(this.isGuest()){
			o.append("{");
			o.append(TOXML_PROPERTY_id).append(":'").append(this.getId()).append("'");
			o.append(",").append(TOXML_PROPERTY_NAME).append(":'").append(Strings.escapeJavascript(this.getName())).append("'");
			o.append(",T:'").append("Guest").append("'");
			o.append(",A:'").append(this.getOrgAccountId()).append("'");
			o.append(",E:'").append(this.getEnabled()).append("'");
			o.append("}");
		}
	}

    public Boolean getIsAdmin() {
        return null == isAdmin ? false : isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getIsAssigned() {
        return isAssigned;
    }

    public void setIsAssigned(Boolean isAssigned) {
        this.isAssigned = isAssigned;
    }

    public Boolean getIsLoginable() {
        return isLoginable;
    }

    public void setIsLoginable(Boolean isLoginable) {
        this.isLoginable = isLoginable;
    }

    public Boolean getIsVirtual() {
        return isVirtual;
    }

    public void setIsVirtual(Boolean isVirtual) {
        this.isVirtual = isVirtual;
    }

    public V3xOrgPrincipal getV3xOrgPrincipal() {
        if(v3xOrgPrincipal == null){
            v3xOrgPrincipal = OrgHelper.getV3xOrgPrincipal(this.getId());
        }
        
        return v3xOrgPrincipal;
    }

    public void setV3xOrgPrincipal(V3xOrgPrincipal v3xOrgPrincipal) {
        this.v3xOrgPrincipal = v3xOrgPrincipal;
    }

    /**
     * 用于设置人员登录名并默认设置登录密码为123456方法，慎用
     * @param loginName 登录名
     */
    public void setV3xOrgPrincipal(String loginName) {
        this.v3xOrgPrincipal = new V3xOrgPrincipal(this.getId(), loginName, OrgHelper.getOrgManager().getInitPWD());
    }

    /** 扩展字段获取的get方法 */

    public Date getBirthday() {
        return (Date) this.properties.get("EXT_ATTR_21");
    }

    public String getOfficeNum() {
        return (String) this.properties.get("EXT_ATTR_3");
    }

    public String getEmailAddress() {
        return (String) this.properties.get("EXT_ATTR_2");
    }
    
    public void setWeibo(String weiboStr) {
        this.setProperty("weibo", weiboStr);
    }
    
    public String getWeibo() {
        return (String) this.properties.get("EXT_ATTR_31");
    }
    
    public void setWeixin(String weixinStr) {
        this.setProperty("weixin", weixinStr);
    }
    
    public String getWeixin() {
        return (String) this.properties.get("EXT_ATTR_32");
    }
    
    public String getIdNum() {
        return (String) this.properties.get("EXT_ATTR_33");
    }
    
    public String getDegree() {
        return (String) this.properties.get("EXT_ATTR_34");
    }
    
    public void setPostalcode(String postalcode) {
        this.setProperty("postalcode", postalcode);
    }
    
    public String getPostalcode() {
        return (String) this.properties.get("EXT_ATTR_5");
    }
    
    public void setAddress(String address) {
        this.setProperty("address", address);
    }
    
    public String getAddress() {
        return (String) this.properties.get("EXT_ATTR_4");
    }
    
    public void setPostAddress(String postAddress) {
        this.setProperty("postAddress", postAddress);
    }
    
    public String getPostAddress() {
        return (String) this.properties.get("EXT_ATTR_35");
    }
    public void setLocation(String location) {
        this.setProperty("location", location);
    }
    public String getLocation() {
        return (String) this.properties.get("EXT_ATTR_36");
    }
    
    public void setReporter(Long reporter) {
        this.setProperty("reporter", reporter);
    }
    public Long getReporter() {
    	if(this.properties.get("EXT_ATTR_37") == null){
    		return null;
    	}
    	if(Long.valueOf(this.properties.get("EXT_ATTR_37").toString()) == -1L){
    	    return null;
    	}
    	return Long.valueOf(this.properties.get("EXT_ATTR_37").toString());
    }
    
    public Date getHiredate() {
        return (Date) this.properties.get("EXT_ATTR_22");
    }
	public Integer getGender() {
		return Integer.valueOf(this.properties.get("EXT_ATTR_11") == null ? "-1" : this.properties.get("EXT_ATTR_11").toString());
	}

    public String getLoginName() {
    	if(this.getV3xOrgPrincipal()==null){
    		return null;
    	}else{
    		return this.getV3xOrgPrincipal().getLoginName();
    	}
       
    }
    
    public String getBlog() {
        return (String) this.properties.get("EXT_ATTR_7");
    }
    
    public String getWebsite() {
        return (String) this.properties.get("EXT_ATTR_6");
    }
    
    public String getPassword() {
        if(this.getV3xOrgPrincipal()==null){
            return null;
        }else{
            return this.getV3xOrgPrincipal().getPassword();
        }
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setSecond_post(List<MemberPost> second_post) {
        this.second_post = second_post;
    }

    /**
     * 仅用于HR模块修改人员登陆名实用，不能用于新建
     * @param member
     * @param loginName
     */
    public void setLoginName(V3xOrgMember member, String loginName) {
        member.getV3xOrgPrincipal().setLoginName(loginName);
    }

	public List<String> getCustomerAddressBooklist() {
		return customerAddressBooklist;
	}

	public void setCustomerAddressBooklist(List<String> customerAddressBooklist) {
		this.customerAddressBooklist = customerAddressBooklist;
	}

	public String getPrimaryLanguange() {
		return primaryLanguange;
	}

	public void setPrimaryLanguange(String primaryLanguange) {
		this.primaryLanguange = primaryLanguange;
	}
	
	public V3xOrgMember cloneImmutableDecorator() {
	    return new V3xOrgMemberImmutableDecorator(this);
	}
	
    class V3xOrgMemberImmutableDecorator extends V3xOrgMember {

        private static final long serialVersionUID = 164684307944703307L;
        
        private final V3xOrgMember source;
        
        //可修改的属性
        private Long sortId; //如果是兼职单位人员，需要修改为兼职单位的排序号
        
        private String _name; //兼职或者副岗人员，在有些地方需要在名称后添加兼职标识
        
        V3xOrgMemberImmutableDecorator(V3xOrgMember source) {
            this.source = source;
        }
        
        public Long getId() {
            return this.source.getId();
        }

        public Date getCreateTime() {
            return this.source.getCreateTime();
        }

        public Date getUpdateTime() {
            return this.source.getUpdateTime();
        }

        public Long getOrgAccountId() {
            return this.source.getOrgAccountId();
        }

        public String getName() {
        	 return Strings.escapeNULL(this._name, this.source.getName());
        }

        public Integer getStatus() {
            return this.source.getStatus();
        }

        public String getCode() {
            return this.source.getCode();
        }

        public Long getSortId() {
            return Strings.escapeNULL(this.sortId, this.source.getSortId());
        }

        public Boolean getIsDeleted() {
            return this.source.getIsDeleted();
        }

        public Boolean getEnabled() {
            return this.source.getEnabled();
        }
        
        public Integer getExternalType() {
            return this.source.getExternalType();
        }

        public String getDescription() {
            return this.source.getDescription();
        }

        public List<MemberPost> getSecondPostByDeptId(Long deptId) {
            return this.source.getSecondPostByDeptId(deptId);
        }

        public List<MemberPost> getSecondPostByPostId(Long postId) {
            return this.source.getSecondPostByPostId(postId);
        }

        public List<MemberPost> getSecond_post() {
            return this.source.getSecond_post();
        }

        public List<MemberPost> getConcurrent_post() {
            return this.source.getConcurrent_post();
        }

        public Boolean getIsValid() {
            return this.source.getIsValid();
        }

        public Boolean getIsInternal() {
            return this.source.getIsInternal();
        }

        public Long getOrgDepartmentId() {
            return this.source.getOrgDepartmentId();
        }

        public Long getOrgLevelId() {
            return this.source.getOrgLevelId();
        }

        public Long getOrgPostId() {
            return this.source.getOrgPostId();
        }

        public Integer getState() {
            return this.source.getState();
        }

        public Integer getType() {
            return this.source.getType();
        }

        public String getEntityType() {
            return this.source.getEntityType();
        }

        public boolean isValid() {
            return this.source.isValid();
        }

        public Object getProperty(String key) {
            return this.source.getProperty(key);
        }

        public Map<String, Object> getProperties() {
            return this.source.getProperties();
        }

        public String getTelNumber() {
            return this.source.getTelNumber();
        }

        public Object getPOProperties(String poKey) {
            return this.source.getPOProperties(poKey);
        }

        public Boolean getIsAdmin() {
            return this.source.getIsAdmin();
        }

        public Boolean getIsAssigned() {
            return this.source.getIsAssigned();
        }

        public Boolean getIsLoginable() {
            return this.source.getIsLoginable();
        }

        public Boolean getIsVirtual() {
            return this.source.getIsVirtual();
        }

        public V3xOrgPrincipal getV3xOrgPrincipal() {
            return this.source.getV3xOrgPrincipal();
        }

        public Date getBirthday() {
            return this.source.getBirthday();
        }

        public String getOfficeNum() {
            return this.source.getOfficeNum();
        }

        public String getEmailAddress() {
            return this.source.getEmailAddress();
        }

        public String getWeibo() {
            return this.source.getWeibo();
        }

        public String getWeixin() {
            return this.source.getWeixin();
        }

        public String getIdNum() {
            return this.source.getIdNum();
        }

        public String getDegree() {
            return this.source.getDegree();
        }

        public String getPostalcode() {
            return this.source.getPostalcode();
        }

        public String getAddress() {
            return this.source.getAddress();
        }

        public String getPostAddress() {
            return this.source.getPostAddress();
        }

        public String getLocation() {
            return this.source.getLocation();
        }

        public Long getReporter() {
            return this.source.getReporter();
        }

        public Date getHiredate() {
            return this.source.getHiredate();
        }

        public Integer getGender() {
            return this.source.getGender();
        }

        public String getLoginName() {
            return this.source.getLoginName();
        }

        public String getBlog() {
            return this.source.getBlog();
        }

        public String getWebsite() {
            return this.source.getWebsite();
        }

        public String getPassword() {
            return this.source.getPassword();
        }

        public List<String> getCustomerAddressBooklist() {
            return this.source.getCustomerAddressBooklist();
        }

        public String getPrimaryLanguange() {
            return this.source.getPrimaryLanguange();
        }
        
    	public String getPinyin() {
    		return this.source.getPinyin();
    	}
    	
    	public String getPinyinhead() {
    		return this.source.getPinyinhead();
    	}

        public void setIdIfNew() {
            throw new UnsupportedOperationException();
        }

        public void setId(Long id) {
            throw new UnsupportedOperationException();
        }

        public void setCreateTime(Date createTime) {
            throw new UnsupportedOperationException();
        }

        public void setUpdateTime(Date updateTime) {
            throw new UnsupportedOperationException();
        }

        public void setOrgAccountId(Long orgAccountId) {
            throw new UnsupportedOperationException();
        }

        public void setStatus(Integer status) {
            throw new UnsupportedOperationException();
        }

        public void setName(String name) {
        	 this._name = name;
        }

        public void setCode(String code) {
            throw new UnsupportedOperationException();
        }

        public void setSortId(Long sortId) {
            this.sortId = sortId;
        }

        public void setIsDeleted(Boolean isDeleted) {
            throw new UnsupportedOperationException();
        }

        public void setEnabled(Boolean enabled) {
            throw new UnsupportedOperationException();
        }
        
        public void setExternalType(Integer externalType) {
            throw new UnsupportedOperationException();
        }

        public void setDescription(String description) {
            throw new UnsupportedOperationException();
        }

        public void addSecondPost(MemberPost memberPost) {
            throw new UnsupportedOperationException();
        }

        public void addSecondPost(Long deptId, Long postId) {
            throw new UnsupportedOperationException();
        }

        public void setIsValid(Boolean isValid) {
            throw new UnsupportedOperationException();
        }

        public void setIsInternal(Boolean isInternal) {
            throw new UnsupportedOperationException();
        }

        public void setOrgDepartmentId(Long orgDepartmentId) {
            throw new UnsupportedOperationException();
        }

        public void setOrgLevelId(Long orgLevelId) {
            throw new UnsupportedOperationException();
        }

        public void setOrgPostId(Long orgPostId) {
            throw new UnsupportedOperationException();
        }

        public void setState(Integer state) {
            throw new UnsupportedOperationException();
        }

        public void setType(Integer type) {
            throw new UnsupportedOperationException();
        }

        public void setProperty(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        public void setProperties(Map<String, Object> properties) {
            throw new UnsupportedOperationException();
        }

        public void setIsAdmin(Boolean isAdmin) {
            throw new UnsupportedOperationException();
        }

        public void setIsAssigned(Boolean isAssigned) {
            throw new UnsupportedOperationException();
        }

        public void setIsLoginable(Boolean isLoginable) {
            throw new UnsupportedOperationException();
        }

        public void setIsVirtual(Boolean isVirtual) {
            throw new UnsupportedOperationException();
        }

        public void setV3xOrgPrincipal(V3xOrgPrincipal v3xOrgPrincipal) {
            throw new UnsupportedOperationException();
        }

        public void setV3xOrgPrincipal(String loginName) {
            throw new UnsupportedOperationException();
        }

        public void setWeibo(String weiboStr) {
            throw new UnsupportedOperationException();
        }

        public void setWeixin(String weixinStr) {
            throw new UnsupportedOperationException();
        }

        public void setPostalcode(String postalcode) {
            throw new UnsupportedOperationException();
        }

        public void setAddress(String address) {
            throw new UnsupportedOperationException();
        }

        public void setPostAddress(String postAddress) {
            throw new UnsupportedOperationException();
        }

        public void setLocation(String location) {
            throw new UnsupportedOperationException();
        }

        public void setReporter(Long reporter) {
            throw new UnsupportedOperationException();
        }

        public void setState(int state) {
            throw new UnsupportedOperationException();
        }

        public void setSecond_post(List<MemberPost> second_post) {
            throw new UnsupportedOperationException();
        }

        public void setLoginName(V3xOrgMember member, String loginName) {
            throw new UnsupportedOperationException();
        }

        public void setCustomerAddressBooklist(List<String> customerAddressBooklist) {
            throw new UnsupportedOperationException();
        }

        public void setPrimaryLanguange(String primaryLanguange) {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object other) {
            return this.source.equals(other);
        }

        public int hashCode() {
            return this.source.hashCode();
        }
        
    }

	public String getPinyin() {
		if(Strings.isBlank(pinyin)){
			pinyin = StringUtil.getPingYin(this.name);
		}
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getPinyinhead() {
		if(Strings.isBlank(pinyinhead)){
			pinyinhead = StringUtil.getPinYinHeadChar(this.name);
		}
		return pinyinhead;
	}

	public void setPinyinhead(String pinyinhead) {
		this.pinyinhead = pinyinhead;
	}
    
    
}