/**
 * 
 */
package com.seeyon.v3x.edoc.webmodel;

import java.util.List;

import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.util.EdocUtil;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocMarkModel {
	
	/**
	 * 公文文号定义ID
	 */
	private Long markDefinitionId;
	
	private EdocMarkDefinition markDef;
	
	private EdocMarkNoModel markNoVo;
	
	/**
	 * 公文文号
	 */
	private String mark;
	
	/**
	 * 文号的当前值 
	 */
	
	private Integer currentNo;
	
	/**
	 * 公文文号授权部门
	 */
	private List<V3xOrgEntity> aclEntity;

	/**
	 * 公文字号
	 */
	private String wordNo;

	/**
	 * 公文文号类型，公文文号||内部文号
	 */
	private int markType;
	
	/**
	 * 流水类型
	 */
	private short codeMode;
	
	/**
	 * 预留文号
	 */
	private String markReserveUp;
	
	/**
	 * 线下占用
	 */
	private String markReserveDown;
	
	
	private Long domainId;
	
	private String titleValue;
	
	/**
	 * 文号流水类型，大流水还是小流水
	 */
	private Short categoryCodeMode ;
	
	/** 所在单位简称 **/
	private String accountShotName = null;
	
	private Integer sort;
	
	public Short getCategoryCodeMode() {
		return categoryCodeMode;
	}

	public void setCategoryCodeMode(Short categoryCodeMode) {
		this.categoryCodeMode = categoryCodeMode;
	}
	/**
	 * @return the aclDept
	 */

	public List<V3xOrgEntity> getAclEntity() {
		return aclEntity;
	}

	public void setAclEntity(List<V3xOrgEntity> aclEntity) {
		this.aclEntity = aclEntity;
	}

	/**
	 * @return the mark
	 */
	public String getMark() {
		return mark;
	}

	/**
	 * @param mark the mark to set
	 */
	public void setMark(String mark) {
		this.mark = mark;
	}

	/**
	 * @return the markDefinitionId
	 */
	public Long getMarkDefinitionId() {
		return markDefinitionId;
	}

	/**
	 * @param markDefinitionId the markDefinitionId to set
	 */
	public void setMarkDefinitionId(Long markDefinitionId) {
		this.markDefinitionId = markDefinitionId;
	}

	public Integer getCurrentNo() {
		return currentNo;
	}

	public void setCurrentNo(Integer currentNo) {
		this.currentNo = currentNo;
	}

	public String getWordNo() {
		return wordNo;
	}

	public void setWordNo(String wordNo) {
		this.wordNo = wordNo;
	}
	
	/*以下方法用于,解析从前台提取过来得文号,进行解析,放置到对应字段*/
	private int docMarkCreateMode=1;//公文文号生成方式,见com.seeyon.v3x.edoc.util.Constants定义
	private Long markId=-1L;//选择断号的时候;
	
	public void setDocMarkCreateMode(int docMarkCreateMode)
	{
		this.docMarkCreateMode=docMarkCreateMode;
	}
	public int getDocMarkCreateMode()
	{
		return this.docMarkCreateMode;
	}
	
	public void setMarkId(Long morkId)
	{
		this.markId=morkId;
	}
	public Long getMarkId()
	{
		return this.markId;
	}
	public int getMarkType() {
		return markType;
	}

	public void setMarkType(int markType) {
		this.markType = markType;
	}
	public String toString()
	{
		String tempMarkValue=getMarkDefinitionId()+ "|" + getMark() + "|";
		if(getCurrentNo()!=null){tempMarkValue+=getCurrentNo();}
		tempMarkValue+="|" + getDocMarkCreateMode();
		return tempMarkValue;
	}
	
	public static EdocMarkModel parse(String reqMark)
	{
		EdocMarkModel em = null;
		if(Strings.isBlank(reqMark)){
		    return em;
		}
		String[] arr = EdocUtil.parseDocMark(reqMark);
		if (arr == null || arr.length != 4) {
		    return em;
		}
		em = new EdocMarkModel();
		if(!"".equals(arr[3])){
		    em.setDocMarkCreateMode(Integer.valueOf(arr[3]));
		}
		
		em.setMark(arr[1]);
		
		if(Strings.isNotBlank(arr[0])){
		    em.setMarkDefinitionId(Long.valueOf(arr[0]));
		    em.setMarkId(Long.valueOf(arr[0]));
		}
		
		if(Strings.isNotBlank(arr[2])){
		    em.setCurrentNo(Integer.valueOf(arr[2]));
	    }
		
		return em;
	}

	public String getMarkReserveUp() {
		return markReserveUp;
	}

	public void setMarkReserveUp(String markReserveUp) {
		this.markReserveUp = markReserveUp;
	}

	public String getMarkReserveDown() {
		return markReserveDown;
	}

	public void setMarkReserveDown(String markReserveDown) {
		this.markReserveDown = markReserveDown;
	}

	public short getCodeMode() {
		return codeMode;
	}

	public void setCodeMode(short codeMode) {
		this.codeMode = codeMode;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public String getTitleValue() {
		return titleValue;
	}

	public void setTitleValue(String titleValue) {
		this.titleValue = titleValue;
	}

    public String getAccountShotName() {
        return accountShotName;
    }

    public void setAccountShotName(String accountShotName) {
        this.accountShotName = accountShotName;
    }

	public EdocMarkDefinition getMarkDef() {
		return markDef;
	}

	public void setMarkDef(EdocMarkDefinition markDef) {
		this.markDef = markDef;
	}

	public EdocMarkNoModel getMarkNoVo() {
		return markNoVo;
	}

	public void setMarkNoVo(EdocMarkNoModel markNoVo) {
		this.markNoVo = markNoVo;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

}
