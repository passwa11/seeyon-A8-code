package com.seeyon.apps.govdoc.mark.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.bo.TemplateMarkInfo;
import com.seeyon.apps.govdoc.mark.vo.GovdocMarkVO;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.apps.govdoc.vo.GovdocNewVO;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

/**
 * 新公文文号管理接口
 * @author 唐桂林
 *
 */
public interface GovdocMarkManager {
	
	/****************************** 公文文号管理相关方法 start *******************************/
	/**
	 * 公文文号管理列表
	 * @param flipInfo
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo findList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	/**
	 * 公文文号占号列表
	 * @param flipInfo
	 * @param condition
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo findUsedList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException;
	/****************************** 公文文号管理相关方法   end *******************************/
	
	
	/****************************** 公文文号定义相关方法 start *******************************/
	/**
	 * 根据id返回公文文号定义。
	 * @param id 公文文号定义id
	 * @return EdocMarkDefinition
	 */
	public EdocMarkDefinition getMarkDef(long id);
	public EdocMarkDefinition getMarkDef(String wordNo, String markType);
	/**
     * 方法描述：保存公文文号定义
     */
	public void saveMarkDef(EdocMarkDefinition po);
	/**
     * 方法描述：修改公文文号定义
     */
	public void updateMarkDef(EdocMarkDefinition po);
	/**
     * 方法描述：删除公文文号定义
     */
	public void deleteMarkDef(EdocMarkDefinition po);
	/**
	 * 逻辑删除文号定义记录（将状态置为已删除）
	 * @param defId  文号定义Id
	 * @param status 状态
	 */
	public void logicalDeleteMarkDef(long defId, short status);
	/**
	 * 是否包含指定字号的公文文号（新建公文文号时调用）。
	 * @param wordNo 字号
	 * @param domainId 单位id
	 * @return Boolean
	 */
	public Boolean containMarkDef(String wordNo, long domainId,int markType);
	/**
	 * 是否包含指定字号的公文文号（修改公文文号时调用）。
	 * @param markDefId 公文文号定义id
	 * @param wordNo 字号
	 * @param domainId 单位id
	 * @return Boolean
	 */
	public Boolean containMarkDef(long markDefId, String wordNo, long domainId,int markType);
	/**
	 * 判断在指定文号类别中是否包含公文文号定义。
	 * @param categoryId
	 * @return Boolean
	 */
	public boolean containMarkDefInCategory(long categoryId);
	/**
	 * 
	 * @param categoryId
	 * @return
	 */
	public List<EdocMarkDefinition> getMarkDefsByCategory(Long categoryId);
	/**
	 * 
	 * @param markDefIdList
	 * @return
	 * @throws BusinessException
	 */
	public List<GovdocMarkVO> getMarkVoListByMarkDefId(List<Long> markDefIdList) throws BusinessException;
	/**
	 * 根据id返回公文文号定义。
	 * @param id 公文文号定义id
	 * @return EdocMarkDefinition
	 */
	public String checkExistMarkDef(String id) throws BusinessException;
	/****************************** 公文文号定义相关方法   end *******************************/

	
	/****************************** 公文文号组装相关方法 start *******************************/
	/**
	 * 
	 * @param domainId
	 * @param depId
	 * @param condition
	 * @param textfield
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkModel> getEdocMarkDefs(Long domainId, Long depId, String condition, String textfield) throws BusinessException;
	/**
	 * 
	 * @param markDef
	 * @param yearNo
	 * @param currentNo
	 * @return
	 */
	public GovdocMarkVO markDef2Mode(EdocMarkDefinition markDef,String yearNo,Integer currentNo);
	/****************************** 公文文号组装相关方法  end *******************************/
    
    
	/****************************** 公文文号流水相关方法 start *******************************/
	/**
     * 根据类别(大流水,小流水)查找类别（分页显示）
     * @param type
     * @param domainId 单位id
     * @return List
     */
    public List<EdocMarkCategory> findByPage(Short type, Long domainId);
	/**
     * 返回指定单位的所有大流水定义。
     * @param domainId 单位id
     * @return List
     */
    public List<EdocMarkCategory> getMarkCategories(Long domainId);
    /**
     * 
     * @param categoryIdList
     * @return
     */
    public Map<Long, Integer> getMarkCategories(List<Long> categoryIdList);
    /**
     * 方法描述：根据类别(大流水,小流水)查找类别
     * 
     * @param type
     * @param domainId 单位id
     * @return
     */
    public List<EdocMarkCategory> findByTypeAndDomainId(Short type,Long domainId);
    /**
     * 方法描述：通过id取得公文类别对象
     * @param id
     * @return
     */
    public EdocMarkCategory findById(Long id);
    /**
     * 方法描述：保存公文文号类别
     * @param edocMarkCategory
     */
    public void saveCategory(EdocMarkCategory po);
    /**
     * 方法描述：修改公文文号类别
     */
    public void updateCategory(EdocMarkCategory po);
	/**
     * 
     * @param categoryId
     */
    public void deleteCategory(long categoryId);
    /**
     * 
     * @param name
     * @param domainId
     * @return
     */
    public Boolean containEdocMarkCategory(String name, long domainId);
    /**
     * 
     * @param categoryId
     * @param name
     * @param domainId
     * @return
     */
    public Boolean containEdocMarkCategory(long categoryId, String name, long domainId);
    /****************************** 公文文号流水相关方法   end *******************************/
        
    
    /****************************** 公文文号授权相关方法 start *******************************/
    /**
	 * 根据文号定义ID查询文号授权的对象
	 * @param edocMarkDefinitionId
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkAcl> getMarkAclById(Long edocMarkDefinitionId)throws BusinessException;
	/**
	 * 
	 * @param defId
	 */
	public void deleteByDefId(Long defId);
	/****************************** 公文文号授权相关方法   end *******************************/
	
	
	/****************************** 公文预留文号相关方法  start *******************************/
	/**
	 * 文号管理-设置预留文号-添加预留文号
	 * @param markDef
	 * @param addReserveList
	 * @param delReservedIdList
	 * @throws BusinessException
	 */
	public void saveMarkReserve(User user, Integer type, EdocMarkDefinition markDef, List<EdocMarkReserveVO> addReserveList, List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException;
	public void saveMarkReserveForCategory(User user, EdocMarkDefinition markDef) throws BusinessException;
	/**
	 * 根据markDefineId删除预留文号
	 * @param markDefineId
	 * @throws BusinessException
	 */
	public void deleteReserveByMarkDefId(Long markDefId) throws BusinessException;
	public void deleteMarkHistoryByReserve(Integer selectType, Long markDefId) throws BusinessException;
	
	/**
	 * 获取某个文号的全部预留文号(用于断号选择界面预留文号)
	 * @Author      : xuqw
	 * @Date        : 2015年3月17日下午7:25:47
	 * @param markDef 文号对象，为null返回全部
	 * @param type 1线上 2线下 -1所有
	 * @return
	 * @throws BusinessException
	 */
	public List<GovdocMarkVO> findReserveListByMarkDefId(Integer type, GovdocMarkVO markDefVo) throws BusinessException;
	/**
	 * 获取某个文号的全部预留文号(用于文号管理列表-预留文号)
	 * @Author      : xuqw
	 * @Date        : 2015年3月17日下午7:25:47
	 * @param markDef 文号对象，为null返回全部
	 * @param type 1线上 2线下 -1所有
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveVO> findMarkReserveVoList(Integer type, EdocMarkDefinition markDef, Integer queryNumber) throws BusinessException;
	/**
	 * 按格式显示某一个文号(用于文号管理列表-预留文号-初始化预留文号VO对象)
	 * @param markDef
	 * @param markNumber 流水号
	 * @return
	 */
	public EdocMarkReserveVO getMarkReserveByFormat(EdocMarkDefinition markDef, Integer markNumber);
	/**
	 * 校验预留文号值域是否重复
	 * 使用点：1文号编辑判断当前文号
	 * @param startNo
	 * @param endNo
	 * @return
	 * @throws BusinessException
	 */
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo) throws BusinessException;
	/**
	 * 校验预留文号值域是否重复(不包括某部分的预留文号)
	 * 使用点：1 预留文号添加删除点确定时做同步判断
	 * @param markDefineId
	 * @param startNo
	 * @param endNo
	 * @param thisReservedIdList
	 * @return
	 * @throws BusinessException
	 */
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo, List<Long> thisReservedIdList, List<Long> delReservedIdList) throws BusinessException;
	public boolean checkRepeatMarkReserved(EdocMarkDefinition markDef, int startNo, int endNo, List<Long> thisReservedIdList, List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException;
	/**
	 * 重新加载预留文号的缓存
	 * 使用点：保存预留文号出异常时调用
	 */
	public void reloadCache();
	/****************************** 公文预留文号相关方法    end *******************************/
	
	
	/****************************** 公文文号前端展现方法 start *******************************/
	/**
	 * 获取文号列表，用于文号前端下拉展示
	 * @param userId
	 * @param currentAccountId
	 * @param isAdmin
	 * @return
	 * @throws BusinessException
	 */
	public List<GovdocMarkVO> getListByUserId(Map<String, Object> condition) throws BusinessException;
	/**
	 * 获取公文单中文号下拉值
	 * @param markType
	 * @param domainId
	 * @return
	 * @throws BusinessException
	 */
	public Map<Integer, List<TemplateMarkInfo>> getFormBindMarkList(String markType, Long domainId)  throws BusinessException;
	/**
	 * 公文单中返回文号的display及value值
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public Map<String, Object> getFormMarkDisplayValue(Map<String, Object> params) throws BusinessException;

	/**
	 * 
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public List<Map<String, Object>> getFormMarkList(Map<String, Object> params) throws BusinessException;
	
	/**
	 * 
	 * @param newVo
	 * @param parentSummary
	 * @throws BusinessException
	 */
	public void fillMarkParamByAuto(GovdocNewVO newVo, EdocSummary parentSummary) throws BusinessException;
	/**
	 * 
	 * @param markDefId
	 * @param type
	 * @return
	 * @throws BusinessException
	 */
	public List<GovdocMarkVO> findCallVoListByMarkDefId(GovdocMarkVO markDefVo) throws BusinessException;
	/**
	 * 通过文号定义ID获取文号VO
	 * @param markDefId
	 * @return
	 * @throws BusinessException
	 */
	public GovdocMarkVO getVoByMarkDefId(Long markDefId) throws BusinessException;
	/**
	 * 通过公文类型，获取某公文文号使用记录
	 * @param summaryId
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	public GovdocMarkVO getVoBySummaryId(Long summaryId, Integer markType) throws BusinessException;
	/**
	 * 获取某公文所有文 号使用记录
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public Map<Integer, GovdocMarkVO> getVoBySummaryId(Long summaryId) throws BusinessException;
	/****************************** 公文文号前端展现方法   end *******************************/	
	

	/****************************** 公文文号断号/占号/跳号保存方法 start *******************************/
	/**
	 * 删除待发公文时，断号解绑
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveUnbindMark(Long summaryId) throws BusinessException;
	/**
	 * 公文撤销断号/占号
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveCancelMark(GovdocBaseVO newVo) throws BusinessException;
	/**
	 * 公文新建保存文号(保存待发)
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveDraftMark(GovdocBaseVO newVo) throws BusinessException;
	
	/**
	 * 公文新建保存文号(发送/快速发文)
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveSendMark(GovdocBaseVO newVo) throws BusinessException;
	
	/**
	 * 公文处理保存文号(暂存待办/提交/分送)
	 * @param summaryVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveDealMark(GovdocBaseVO dealVo) throws BusinessException;
	
	/**
	 * 公文文号流程结束
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveFinishMark(GovdocBaseVO newVo) throws BusinessException;
	
	/**
	 * 公文文号流程结束
	 * @param newVo
	 * @return
	 * @throws BusinessException
	 */
	public boolean saveDraftMarkOld(GovdocBaseVO newVo) throws BusinessException;
	public boolean saveSendMarkOld(GovdocBaseVO newVo) throws BusinessException;
	public boolean saveCancelMarkOld(GovdocBaseVO newVo) throws BusinessException;
	public boolean saveDealMarkOld(GovdocBaseVO newVo) throws BusinessException;
	public boolean saveFinishMarkOld(GovdocBaseVO newVo) throws BusinessException;
	public boolean saveSignMarkOld(GovdocBaseVO newVo) throws BusinessException;
	public boolean saveRegisterSendMarkOld(GovdocBaseVO newVo) throws BusinessException;
	public boolean saveRegisterDraftMarkOld(GovdocBaseVO newVo) throws BusinessException;
	/****************************** 公文文号断号/占号/跳号保存方法   end *******************************/
	
	
	/****************************** 公文文号断号/占号校验方法 start *******************************/
	/**
	 * 验证文号断号
	 * @param summaryId
	 * @param orgAccountId
	 * @param govdocType
	 * @param edocType
	 * @param markType
	 * @param markstr
	 * @return
	 * @throws BusinessException
	 */
	public boolean checkMarkIsCalled(String summaryId, String orgAccountId, String govdocType, String edocType, String jianbanType, String markType, String markstr) throws BusinessException;
	
	/**
	 * 验证文号占号
	 * @param summaryId
	 * @param orgAccountId
	 * @param govdocType
	 * @param edocType
	 * @param markType
	 * @param markstr
	 * @return
	 * @throws BusinessException
	 */
	public boolean checkMarkIsUsed(String summaryId, String orgAccountId, String govdocType, String edocType, String jianbanType, String markType, String markstr) throws BusinessException;
	
	/**
	 * 
	 * @param markType
	 * @param markDefId
	 * @param markstr
	 * @param domainId
	 * @return
	 */
	public boolean checkMarkIsUsedForReserver(String markDefId, int startNo, int endNo);
	
	/**
	 * 
	 * @param markDefId
	 * @param startNo
	 * @param endNo
	 * @return
	 */
	public boolean checkMarkIsCalledForReserver(String markDefId, int startNo, int endNo);
	/****************************** 公文文号断号/占号校验方法   end *******************************/
	
	
	/****************************** 模板文号方法  start *******************************/
	/**
	 * 
	 * @param user
	 * @param fieldName
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("rawtypes")
	public String getTemplateMarkInfoXmlByParams(User user, Map params) throws BusinessException;
	/****************************** 模板文号方法     end *******************************/
	
	/**
	 * 提供给统计使用
	 * @param domainId
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkModel> getMarkVoListForStat(Long domainId) throws BusinessException;
	
	/**
	 * 
	 * @param edocId
	 * @param markType
	 * @return
	 * @throws BusinessException
	 */
	public EdocMarkHistory getMarkHistoryByEdocId(Long summaryId, Integer markType, String markstr) throws BusinessException;
	public EdocMark getMarkByEdocId(Long summaryId, Integer markType, String markstr) throws BusinessException;
	
}
