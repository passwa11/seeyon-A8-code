package com.seeyon.apps.govdoc.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.webmodel.GovdocStatConditionVO;
import com.seeyon.v3x.edoc.webmodel.GovdocStatDisplayVO;
import com.seeyon.v3x.edoc.webmodel.WebSignCount;

/**
 * 新公文统计接口
 * @author 唐桂林
 *
 */
public interface GovdocStatManager {
	
	/**
     * 根据统计值及开始结束时间统计
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    public List<WebSignCount> findMtechSignCount(Date startDate,Date endDate,Long statId)throws Exception;
	
	/**
	 * 新公文签收统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws Exception
	 */
	public FlipInfo signStatToListGovdoc(FlipInfo flipInfo, Map<String, String> conditionMap) throws Exception;
	
	/**
	 * 新公文统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws Exception
	 */
	public FlipInfo statToListGovdoc(FlipInfo flipInfo, Map<String, String> conditionMap) throws Exception;
	/**
     * 通过工作统计配置获取查询部门条件数据
     * @param statId
     * @return
     * @throws Exception
     */
    public List<V3xOrgDepartment> initWorkStatDeptList(long statId) throws Exception;
	
	/**
	 * 新公文签收统计
	 * @param conditionVo
	 * @return
	 * @throws Exception
	 */
	public List<GovdocStatDisplayVO> findGovdocStatSignResult(GovdocStatConditionVO conditionVo) throws Exception;
	
	/**
	 * 新公文退文统计
	 * @param conditionVo
	 * @return
	 * @throws Exception
	 */
	public List<GovdocStatDisplayVO> findGovdocStatSignSelfResult(GovdocStatConditionVO conditionVo) throws Exception;
	
	/**
	* 新公文工作统计
	* @param listType
	* @param conditionVo
	* @return
	* @throws Exception
	*/
	public List<GovdocStatDisplayVO> findGovdocStatResult(GovdocStatConditionVO conditionVo) throws Exception;
	/**
	 * 获取公文统计数据
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getGovdocStatVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
	/**
	 * 获取公文统计穿透列表
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocVoList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
	/**
	 * 公文统计界面-选择枚举值
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws BusinessException
	 */
	public FlipInfo getEdocEnumitemList(FlipInfo flipInfo, Map<String, String> conditionMap) throws BusinessException;
	
	public void saveOrUpdateEdocStat(EdocSummary summary, User user) throws BusinessException;
	
	public void deleteEdocStat(Long summaryId)throws Exception;
    
}
