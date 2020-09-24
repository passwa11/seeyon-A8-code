package com.seeyon.apps.xkjt.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.xkjt.po.EdocFormInfo;
import com.seeyon.apps.xkjt.po.XkjtLeaderBanJie;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.apps.xkjt.po.XkjtOpenMode;
import com.seeyon.apps.xkjt.po.XkjtSummaryAttachment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocSummary;



public interface XkjtDao {
	/**
	 * @Title: saveOpenMode  
	 * @Description: 保存节点公开方式
	 * @author wxt.chenq
	 * @param xkjtOpenMode
	 * @throws BusinessException
	 * @throws
	 */
	public void saveOpenMode(XkjtOpenMode xkjtOpenMode) throws BusinessException;
	
	/**
	 * @Title: saveOpenModes  
	 * @Description: 批量保存节点公开方式
	 * @author zelda
	 * @param xkjtOpenModes
	 * @throws BusinessException
	 * @throws
	 */
	public void saveOpenModes(List<XkjtOpenMode> xkjtOpenModes) throws BusinessException;
	
	/**
	 * 
	 * @Title: updateOpenMode  
	 * @Description: 更新公开方式
	 * @author wxt.chenq
	 * @param xkjtOpenMode
	 * @throws BusinessException
	 * @throws
	 */
	public void updateOpenMode(XkjtOpenMode xkjtOpenMode) throws BusinessException;
	
	/**
	 * 
	 * @Title: getOpenModeByNodeId  
	 * @Description: 通过节点Id获取公开方式
	 * @author wxt.chenq
	 * @param nodeId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List findOpenModeByNodeId(Long nodeId) throws BusinessException;
	
	/**
	 * 
	 * @Title: saveMainAttachment  
	 * @Description: 保存公文的或协同的主附件
	 * @author wxt.chenq
	 * @param xkjtSummaryAttachment
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public void saveMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment) throws BusinessException;
	
	/**
	 * 
	 * @Title: saveMainAttachment  
	 * @Description: 通过summary_Id获取其主附件
	 * @author wxt.chenq
	 * @param summaryId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public XkjtSummaryAttachment findMainAttachmentBySummaryId(Long summaryId) throws BusinessException;
	
	/**
	 * 
	 * @Title: updateMainAttachmentBySummaryId  
	 * @Description: 通过summary_Id更新其主附件
	 * @author wxt.chenq
	 * @param xkjtSummaryAttachment
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public void updateMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment) throws BusinessException;
	
	/**
	 * 
	 * @Title: isMainAttachment  
	 * @Description: 判断是否主附件
	 * @author wxt.chenq
	 * @param attachmentId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public boolean isMainAttachment(Long attachmentId,Long summaryId) throws BusinessException;
	
	/**
	 * 
	 * @Title: saveXkjtLeaderDaiYue  
	 * @Description: 保存发给人员的回执信息
	 * @author wxt.chenq
	 * @param xkjtLeaderDaiYue
	 * @throws BusinessException
	 * @throws
	 */
	public void saveXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueByEdocId  
	 * @Description: 通过EdocId找到给各个人员发送的传阅件
	 * @author wxt.chenq
	 * @param edocId
	 * @param leaderId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocId(Long edocId,Long leaderId) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueByEdocId  
	 * @Description: 获取回执信息中领导阅读件
	 * @author wxt.chenq
	 * @param edocId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocIdAndSendId(Long sendRecordId, Long edocId) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueByMemberId  
	 * @Description: 通过memberId找到传阅件
	 * @author wxt.xiangrui
	 * @param leaderName
	 * @throws BusinessException
	 */
	public List<Object> findXkjtLeaderDaiYueByMemberId(Long memberId) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueByMemberId  
	 * @Description: 通过memberId adn edocId找到传阅件
	 * @author wxt.chenq
	 * @param memberId
	 * @param memberId
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByMemberId(Long memberId,Long edocId) throws BusinessException;
	
	/**
	 * 
	 * @Title: updateXkjtLeaderDaiYue  
	 * @Description: 修改待阅状态
	 * @author wxt.chenq
	 * @param xkjtLeaderDaiYue
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public void updateXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue) throws BusinessException;
	
	/**
	 * @Title: findMoreXkjtLeaderDaiYueByMemberId  
	 * @Description: 分页查询待阅
	 * @author wxt.chenq
	 * @param fi
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public FlipInfo findMoreXkjtLeaderDaiYueByMemberId(FlipInfo fi,Map params) throws BusinessException;
	
	/**
	 * 
	 * @Title: findXkjtLeaderDaiYueById  
	 * @Description: 通过Id查找待阅件
	 * @author wxt.chenq
	 * @param id
	 * @return
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueById(Long id);
	
	/**
	 * @Title: findXkjtLeaderYiYueByMemberId  
	 * @Description: 通过memberId找到已阅件
	 * @author wxt.chenq
	 * @param leaderName
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public List<XkjtLeaderDaiYue> findXkjtLeaderYiYueByMemberId(Long memberId) throws BusinessException;
	
	/**
	 * @Title: findMoreXkjtLeaderYiYueByMemberId  
	 * @Description: 分页查询已阅
	 * @author wxt.chenq
	 * @param fi
	 * @param params
	 * @return
	 * @throws BusinessException
	 * @throws
	 */
	public FlipInfo findMoreXkjtLeaderYiYueByMemberId(FlipInfo fi,Map params) throws BusinessException;
	
	/**
	 * 
	 * 项目：徐州矿物集团[办结栏目] 作者:xiangrui 修改日期：2019年5月24日 [修改功能:] start
	 *@param memberId
	 *@return
	 *@throws BusinessException
	 */
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId) throws BusinessException;
	
	/**
	 * 
	 * 项目：徐州矿物集团[办结栏目] 作者:zelda 修改日期：2019年5月24日 [修改功能:] start
	 *@param memberId
	 **@param templetIds 模板id
	 *@return
	 *@throws BusinessException
	 */
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId, String templetIds) throws BusinessException;
	
	/**
	 * 
	 * 项目：徐州矿物集团[办结栏目更多页] 作者:xiangrui 修改日期：2019年5月27日 
	 *@param fi
	 *@param params
	 *@return
	 *@throws BusinessException
	 */
	public FlipInfo findMoreXkjtLeaderBanJieByMemberId(FlipInfo fi, Map<String,Object> params) throws BusinessException;

	/**
	 * @author best
	 * @param formId
	 *            文单id
	 * @return 返回文单配置的不需要展示的部门信息
	 */
	public EdocFormInfo getByFormId(Long formId);

	/**
	 * @author best
	 * @param info
	 *            更新文单配置信息
	 */
	public void updateEdocFormInfo(EdocFormInfo info);

	/**
	 * @author best
	 * @param info
	 *            保存文单配置信息
	 */
	public void saveEdocFormInfo(EdocFormInfo info);
	
}
