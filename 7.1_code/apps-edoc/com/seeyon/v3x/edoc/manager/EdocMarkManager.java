/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;
import com.seeyon.v3x.edoc.domain.EdocParam;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.webmodel.EdocMarkNoModel;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0 
 * @since JDK 5.0
 */
public interface EdocMarkManager {
	
	/**
	 * 
	 * @param markDefineId
	 * @return
	 */
	public List<EdocMark> findListByMarkDefineId(Long markDefineId);
	
	public List<EdocMarkHistory> findHistoryListByMarkDefineId(Long markDefineId);
	
	/**
	 * 
	 * @param edocMarkList
	 */
	public void save(List<EdocMark> edocMarkList);
	/**
	 * 
	 * @param edocMarkList
	 */
	public void update(List<EdocMark> edocMarkList,User user);
	
	/**
     * 方法描述：保存公文文号
     */
    public void save(EdocMark edocMark);
    
    /**
     * 登记使用的文号,返回真正的文号串
     * @param markStr:掩码格式文号，详细见EdocMarkModel.parse()方法
     * @param markNum
     */
    public String registDocMark(Long summaryId,String markStr,int markNum,int edocType,boolean checkId,int markType) throws EdocMarkHistoryExistException;
    public String registDocMark(EdocSummary summary,Long summaryId,String markStr,int markNum,int edocType,boolean checkId,int markType) throws EdocMarkHistoryExistException;
    
    /**
     * 表单模板调用文号
     * @param edocParam 封装文号保存的参数类，参数太多了
     * @throws EdocMarkHistoryExistException
     */
    public void saveDocMark(EdocParam edocParam,User user) throws EdocMarkHistoryExistException;
    
    /**
     * 方法描述：保存公文文号，并更新当前值
     * @param edocMark  公文文号对象
     * @param catId     公文类别id
     * @param currentNo 提供给用户选择的公文文号的当前值
     */
    public void save(EdocMark edocMark,Long catId,int currentNo);
    
    /**
     * 根据ID返回EdocMark对象
     * @param edocMarkId  ID
     * @return
     */
    public EdocMark getEdocMark(Long edocMarkId);
    
    /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-公文文号对象的最小创建时间]start*/
    public EdocMark getMinCreaateDateEdocMark();
    /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-公文文号对象的最小创建时间]end*/
    
    /**
     * 根据EDOC_ID返回EdocMark对象
     * @param edocId  edocId
     * @return
     */
    public EdocMark getEdocMarkByEdocID(Long edocId);
    
    /**
     * 根据业务ID和临时变量查询mark
     * @param memo 表单文号值
     * @return
     */
    public List<EdocMark> getEdocMarkByMemo(String memo,User user);
    
    /**
     * 根据业务ID和临时变量查询mark
     * @param memo 表单文号值
     * @return
     */
    public List<EdocMark> getAllEdocMark(String memo,Long summaryID);
    
    public void updateStatus(String memo,User user) throws BusinessException;
    /**
     * 删除公文文号
     * @param id  公文文号
     */
    public void deleteEdocMark(long id);
    
    /**
     * 删除公文断号
     * @param edocId
     */
    public void deleteEdocMarkByEdocId(long edocId);
    
    /**
     * 删除公文断号
     */
    public void deleteEdocMarkByMarkstr(String markstr);
    
    /**
     * 删除公文断号(20170721)
     */
    public void deleteEdocMarkByMarkstr(List<String> markstrList);
    public void deleteEdocMarkByMarkstr(List<String> markstrList, Integer markType);
    
    /**
     * @方法描述： 拟文时创建文号，并将文号类别当前值加一
     * @param definitionId 公文定义Id
     * @param currentNo 公文文号的序号
     * @param docMark 公文文号
     * @param edocId 公文Id
     */    
    public void createMark(String memo,Long definitionId, Integer currentNo, String docMark, Long edocId,int markNum, int govdocType);   
    public void createMark(Long definitionId, Integer currentNo, String docMark, Long edocId,int markNum);
    public void createMark(String memo,String docMark, Long edocId,int markNum,int govdocType);
    public void createMark(String docMark, Long edocId,int markNum);
    
    /**
     * 判断文号是否被占用
     * @param edocId     公文id
     * @param edocMark   文号
     * @return   true 被占用 false 未占用
     */
    public boolean isUsed(Long edocId);
    
    /**
     * 判断文号是否被占用
     * @param markStr : 文号字符串
     * @param edocId : 公文ID
     * @param summaryOrgAccountId : 单位ID
     * @return true 被占用 false 未占用
     */
    public boolean markIsUsed(String markStr,String edocId,String summaryOrgAccountId);
    public boolean markIsUsed(List<String> markStr,String edocId,String summaryOrgAccountId);
    public boolean isUsed(List<String> markStr,String edocId,String summaryOrgAccountId);
    public boolean isUsed(String markStr,String edocId,String summaryOrgAccountId);
    public boolean isUsed(String markStr,String edocId,String summaryOrgAccountId,String govdocType);
    
    /**
     * 按年度把公文文号归为最小值
     */
    
    public void turnoverCurrentNoAnnual();
    
    
    /**
     * 根据公文文号定义id查找断号
     */    
    public List<EdocMarkNoModel> getDiscontinuousMarkNos(Long edocMarkDefinitionId);
    public List<EdocMarkNoModel> getDiscontinuousMarkNos(EdocMarkDefinition markDef);
    
    /**
     * 方法描述：拟文/修改文单时，选择一个断号
     * @param edocMarkId 公文文号id
     * @param edocId 公文id
     */    
    public void createMarkByChooseNo(String memo,Long edocMarkId, Long edocId,int markNum, int govdocType);
    public void createMarkByChooseNo(Long edocMarkId, Long edocId,int markNum);
    public void createMarkByChooseReserveNo(String memo,Long edocMarkId, Long edocId, Integer markNumber, int markNum, int govdocType);
    public void createMarkByChooseReserveNo(Long edocMarkId, Long edocId, Integer markNumber, int markNum);
    
    /**
     * 断开已经被调用但没有正式使用的公文文号与公文的连接;
     * 用于修改了文号,原来文号变成断号
     * @param edocSummaryId
     */
    public void disconnectionEdocSummary(Long edocSummaryId, int markNum);
    public void disconnectionEdocSummary(EdocSummary summary, long edocSummaryId, int markNum);
    
    public List<EdocMark> findByCategoryAndNo(Long categoryId,Integer docMarkNo);
    
    /**
     * 发起人撤销流程后，已经调用的文号（如果是最大号）可以恢复，下次发文时可继续调用。
     * @param summary 		公文对象
     * @return
     */
    public void edocMarkCategoryRollBack(EdocSummary summary);
    /**
     * 签收编号撤销
     * @param summaryId
     */
    public void rollBackRecNo(Long summaryId);
    
    /**
     * 获取预留文号
     * @param reserveId
     * @return
     * @throws BusinessException
     */
    public EdocMarkReserve getEdocMarkReserve(Long reserveId) throws BusinessException;
    
    /**
     * 保存文号断号、占号及跳转(20170721)
     * @param edocParam 封装文号保存的参数类，参数太多了
     * @throws EdocMarkHistoryExistException
     */
    public String saveDocMark(EdocParam edocParam) throws EdocMarkHistoryExistException;
    
    /**
     * 文号跳号(20170721)
     * @param markDef
     * @param thisNo
     */
    public void updateNextCurrentNo(EdocMarkDefinition markDef, Integer thisNo);
    /**
     * 文号跳号(20170721)
     * @param markDef
     * @param thisNo
     * @param checkCurrentNo 是否检查当前值=thisNo
     */
    public void updateNextCurrentNo(EdocMarkDefinition markDef, Integer thisNo,boolean checkCurrentNo);
    
    /**
     * 验校EdocMarkHistory文号占用表中是否有该文号
     * @param markType
     * @param govdocType
     * @param markStr
     * @param summaryId
     * @param summaryOrgAccountId
     * @return
     */
    public boolean isGovdocUsedNew(String markType, String govdocType, String markStr, String summaryId, String summaryOrgAccountId);

}
