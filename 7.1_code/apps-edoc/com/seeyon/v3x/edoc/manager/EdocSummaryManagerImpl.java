package com.seeyon.v3x.edoc.manager;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.seeyon.apps.edoc.bo.SimpleEdocSummary;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.encrypt.CoderFactory;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.filemanager.manager.Util;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.office.UserUpdateObject;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.login.online.OnlineManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.dao.EdocBodyDao;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSubjectWrapRecord;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryExtend;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.exchange.dao.EdocRecieveRecordDao;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;

public class EdocSummaryManagerImpl implements EdocSummaryManager {
	
	private EdocSummaryDao edocSummaryDao;
	private static OrgManager orgManager;
	private static OnlineManager onlineManager;
    private FileManager fileManager;
    private EdocBodyDao edocBodyDao;
    private EdocSummaryExtendManager edocSummaryExtendManager;
    private static Log LOG = CtpLogFactory.getLog(EdocSummaryManagerImpl.class);
    private AffairManager affairManager;
    private PermissionManager permissionManager;
    
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	public void setEdocBodyDao(EdocBodyDao edocBodyDao) {
        this.edocBodyDao = edocBodyDao;
    }
    private static final Log log = LogFactory.getLog(EdocSummaryManagerImpl.class);
	private synchronized void init() {
		if(onlineManager == null){
			orgManager = (OrgManager) AppContext.getBean("orgManager");
			onlineManager = (OnlineManager)AppContext.getBean("onlineManager");
		}
	}
	public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao)
	{
		this.edocSummaryDao=edocSummaryDao;
	}

	public EdocSummaryManagerImpl() {
		init();
	}

	public EdocSummary findById(long id) {
		return edocSummaryDao.get(id);		
	}
	
	public EdocSummary getEdocSummaryById(long summaryId, boolean needBody, boolean isLoadExtend) throws EdocException {
		EdocSummary summary = edocSummaryDao.get(summaryId);
	       if(summary!=null){
				try {
		        	summary.checkSendUnitData();
		            V3xOrgMember member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
		            summary.setStartMember(member);
		        } catch (BusinessException e) {
		            log.error("读取公文主体属性时查询发起人错误", e);
		        }
		        if (needBody) {
		            summary.getEdocBodies().size();
		        }
	       }
		   if(isLoadExtend) {
			   try {
				   summary = edocSummaryExtendManager.transSetSummaryExtendValue(summary);
				} catch(BusinessException e) {
					log.error("获取公文扩展属性出错", e);
				}
		   }
	        return summary;
	}
	
	public void saveEdocSummary(EdocSummary o) {
		this.saveOrUpdateEdocSummary(o, false);
	}
	public void saveEdocSummary(EdocSummary o, boolean isSaveExtend) {		
			edocSummaryDao.save(o);
			if(isSaveExtend) {
				try {
					EdocSummaryExtend summaryExtend = new EdocSummaryExtend();
					summaryExtend.setNewId();
					summaryExtend = edocSummaryExtendManager.transSetSummaryToExtend(o,summaryExtend);
					edocSummaryExtendManager.saveEdocSummaryExtend(summaryExtend);
				} catch(BusinessException e) {
					log.error("保存公文扩展表数据出错", e);
				}
			}
	}
	
	public void updateEdocSummary(EdocSummary o) {
		this.updateEdocSummary(o, false);
	}
	public void updateEdocSummary(EdocSummary o, boolean isSaveExtend) {
		edocSummaryDao.update(o);
		if(isSaveExtend) {
			try {
				edocSummaryExtendManager.saveEdocSummaryExtendBySummary(o);
			} catch(BusinessException e) {
				log.error("修改公文扩展表数据出错", e);
			}
		}
	}
	
	public void saveOrUpdateEdocSummary(EdocSummary o) {
		this.saveOrUpdateEdocSummary(o, false);
	}
	public void saveOrUpdateEdocSummary(EdocSummary o, boolean isSaveExtend) {
		edocSummaryDao.saveOrUpdate(o);
		if(isSaveExtend) {
			try {
				edocSummaryExtendManager.saveEdocSummaryExtendBySummary(o);
			} catch(BusinessException e) {
				log.error("保存公文扩展表数据出错", e);
			}
		}
	}
	
	public void deletePhysicalEdocSummary(Long summaryId) {
		if(summaryId != null) {
			edocSummaryDao.delete(summaryId);
			try {
				edocSummaryExtendManager.deleteEdocSummaryExtendBySummaryId(summaryId);
			} catch(BusinessException e) {
				log.error("删除公文扩展表数据出错", e);
			}
		}
	}
	
	public void updateEdocSummaryState(Long edocId, int state) {
		edocSummaryDao.updateEdocSummaryState(edocId, state);		
	}
	
	public EdocSummary getSummaryByProcessId(String processId){
		return edocSummaryDao.getSummaryByProcessId(processId);
	}
	/**
	 * 根据内部文号判断文号内部文号是否已经使用
	 * @param serialNo  内部文号
	 * @return
	 */
	public int checkSerialNoExsit(String serialNo,Long loginAccout){
		return edocSummaryDao.checkSerialNoExsit(null,serialNo,loginAccout);
	}
	/**
	 * 根据内部文号判断文号内部文号是否已经使用
	 * @param summaryId  公文ID
	 * @param serialNo   内部文号
	 * @param loginAccount  登录单位
	 * @return (1：存在  0：不存在)
	 */
	public int checkSerialNoExsit(String summaryId,String serialNo,Long loginAccount){
		return edocSummaryDao.checkSerialNoExsit(summaryId,serialNo,loginAccount);
	}
//	yangzd=============================================避免文单正文多人同时修改代码开始===================================
	//用office的处理文件ID做为key保存的修改记录
	private final static CacheAccessable cacheFactory = CacheFactory.getInstance(EdocSummaryManager.class);
	private static CacheMap<String,UserUpdateObject> useObjectList = cacheFactory.createMap("FlowIdMsgListMap");
	
	
	
	
	//修改对象,放入对象修改列表
	public synchronized UserUpdateObject editObjectState(String objId)
	{  
		String from = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
		if(objId==null || "".equals(objId)){return null;}
		User user=AppContext.getCurrentUser();
		UserUpdateObject os=null;
		os=useObjectList.get(objId);
		if(os==null)
		{//无人修改
			os=new UserUpdateObject();
			try{
				os.setLastUpdateTime(user.getLoginTimestamp());
				os.setObjId(objId);			
				os.setUserId(user.getId());
				os.setUserName(user.getName());
				os.setCurEditState(false);
				os.setFrom(from);
				addUpdateObj(os);
			}catch(Exception e){	
			    LOG.error("", e);
			}			
		}else{
			if(Strings.equals(os.getUserId(), user.getId()) && from.equals(os.getFrom()))
			{
				os.setCurEditState(false);
			}
			else
			{
				//有用户修改时，要判断用户是否在线,如果用户不在线，删除修改状态
				boolean editUserOnline=true;
				V3xOrgMember member = null; //当前office控件编辑用户
				try{
					member = orgManager.getEntityById(V3xOrgMember.class, os.getUserId());
					boolean isSameLogin  = onlineManager.isSameLogin(member.getLoginName(), os.getLastUpdateTime()) ;
					editUserOnline=onlineManager.isOnline(member.getLoginName()) && isSameLogin ;
				}
				catch(Exception e1){
					log.warn("检查文档是否被编辑，文档编辑用户不存在[" + os.getUserId() + "]", e1);					
				}
				if(editUserOnline)
				{
					os.setCurEditState(true);
				}
				else
				{
					//编辑用户已经离线，修改文档编辑人为当前用户
					os.setUserId(user.getId());
					os.setUserName(user.getName());
					os.setCurEditState(false);		
					os.setLastUpdateTime(user.getLoginTimestamp());
					os.setFrom(from);
				}
			}						
		}
		return os;
	}
	//检查对象是否被修改
	public synchronized UserUpdateObject checkObjectState(String objId)
	{
		UserUpdateObject os=null;
		os=useObjectList.get(objId);
		if(os==null){os=new UserUpdateObject();}
		return os;
	}
	public synchronized boolean deleteUpdateObj(String objId)
	{
		User user=AppContext.getCurrentUser();
		if(user==null) return true;
		long userId = user.getId();
		return deleteUpdateObj(objId, String.valueOf(userId));
	}
	
	//OA-33739  客户bug：代理人处理公文后被代理人仍能处理  
	public String deleteUpdateObjAndIsAffairEnd(String objId, String affairId) {
	    deleteUpdateObj(objId);

	    if (Strings.isNotBlank(affairId)) {
	      CtpAffair affair;
            try {
                affair = affairManager.get(Long.valueOf(Long.parseLong(affairId)));
                if ((affair == null) || (affair.getState().intValue() != StateEnum.col_pending.key()))
                    return "true";
            } catch (BusinessException e) {
                log.error("获取affair事项错误!"+e.getMessage());
            }
    	}
	    return "false";
	  }
	
	
	public synchronized boolean deleteUpdateObj(String objId, String userId) {
		UserUpdateObject os=null;
		if(objId!=null&&!"".equals(objId))
		{
			os=useObjectList.get(objId);
			if(os==null){return true;}
			String from = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
			if(userId.equals(String.valueOf(os.getUserId())) && Strings.isNotBlank(from) && from.equals(os.getFrom()))
			{
				useObjectList.remove(objId);
//				//发送集群通知
//				NotificationManager.getInstance().send(NotificationType.EdocUserOfficeObjectRomove, new String[]{objId,userId});
			}
		}
		return true;
	}
	public synchronized boolean addUpdateObj(UserUpdateObject uo)
	{		
		useObjectList.put(uo.getObjId(),uo);	
		//发送集群通知
//		NotificationManager.getInstance().send(NotificationType.EdocUserOfficeObjectAdd, uo);

		return true;
	}
	//yangzd=============================================避免文单正文多人同时修改代码结束===================================
	
	
	
	//GOV-4894 公文管理，发文分发后，可以多人同时打开收文签收界面，非第一个点签收的人都报脚本错误 ----start
	
	//避免收文签收单多人同时打开
	private static CacheMap<String,UserUpdateObject> useRecieveObjectList = cacheFactory.createMap("RecieveIdMsgListMap");
	
	private EdocRecieveRecordDao getEdocRecieveRecordDao(){
		EdocRecieveRecordDao edocRecieveRecordDao = (EdocRecieveRecordDao)AppContext.getBean("edocRecieveRecordDao");
		return edocRecieveRecordDao;
	}
	
	
	
	//修改对象,放入对象修改列表
	public synchronized UserUpdateObject editRecieveObjectState(String objId)
	{
		EdocRecieveRecordDao edocRecieveRecordDao = getEdocRecieveRecordDao();
		if(objId==null || "".equals(objId)){return null;}
		User user=AppContext.getCurrentUser();
		UserUpdateObject os=null;
		os=useRecieveObjectList.get(objId);
		if(os==null)
		{//无人修改
			os=new UserUpdateObject();
			try{
				EdocRecieveRecord er = edocRecieveRecordDao.get(Long.parseLong(objId));
				if(er!=null)
				{
					os.setLastUpdateTime(er.getCreateTime());
				}
				else
				{
					os.setLastUpdateTime(null);
					return os;
				}
				os.setObjId(objId);			
				os.setUserId(user.getId());
				os.setUserName(user.getName());
				addUpdateRecieveObj(os);
			}catch(Exception e)
			{				
			    LOG.error("", e);
			}			
		}
		else
		{
			if(Strings.equals(os.getUserId(), user.getId()))
			{
				os.setCurEditState(false);
			}
			else
			{
				if(orgManager == null){
					orgManager = (OrgManager)AppContext.getBean("orgManager");
				}
				if(onlineManager == null){
					onlineManager = (OnlineManager)AppContext.getBean("onlineManager");
				}
				
				//有用户修改时，要判断用户是否在线,如果用户不在线，删除修改状态
				boolean editUserOnline=true;
				V3xOrgMember member = null; //当前office控件编辑用户
				try{
					member = orgManager.getEntityById(V3xOrgMember.class, os.getUserId());
					editUserOnline=onlineManager.isOnline(member.getLoginName());
				}
				catch(Exception e1){
					log.warn("检查文档是否被编辑，文档编辑用户不存在[" + os.getUserId() + "]", e1);					
				}
				if(editUserOnline)
				{
					os.setCurEditState(true);
				}
				else
				{
					//编辑用户已经离线，修改文档编辑人为当前用户
					os.setUserId(user.getId());
					os.setUserName(user.getName());
					os.setCurEditState(false);					
				}
			}						
		}
		return os;
	}
	
	
	public synchronized boolean deleteUpdateRecieveObj(String objId)
	{
		User user=AppContext.getCurrentUser();
		if(user==null) return true;
		long userId = user.getId();
		return deleteUpdateRecieveObj(objId, String.valueOf(userId));
	}
	public synchronized boolean deleteUpdateRecieveObj(String objId, String userId) {
		UserUpdateObject os=null;
		if(objId!=null&&!"".equals(objId))
		{
			os=useRecieveObjectList.get(objId);
			if(os==null){return true;}
			if(userId.equals(String.valueOf(os.getUserId())))
			{
				useRecieveObjectList.remove(objId);
			}
		}
		return true;
	}
	
	public synchronized boolean addUpdateRecieveObj(UserUpdateObject uo)
	{		
		useRecieveObjectList.put(uo.getObjId(),uo);	
		return true;
	}
	
	//GOV-4894 公文管理，发文分发后，可以多人同时打开收文签收界面，非第一个点签收的人都报脚本错误 ---------end
	
	
	
	public static Map<String, UserUpdateObject> getUseObjectList() {
		return useObjectList.toMap();
	}
	public static void setUseObjectList(Map<String, UserUpdateObject> uol) {
//		EdocSummaryManagerImpl.useObjectList = useObjectList;
		useObjectList.replaceAll(uol);
	}
	@Override
	public Attachment getAttsList(EdocBody body, String transmitSendNewEdocId,
			Date createDate, EdocSummary summary) throws BusinessException, FileNotFoundException {
		Attachment atts=null;
 		if(Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(body.getContentType())
 				||Constants.EDITOR_TYPE_OFFICE_WORD.equals(body.getContentType())
 				||Constants.EDITOR_TYPE_WPS_WORD.equals(body.getContentType())
 				||Constants.EDITOR_TYPE_WPS_EXCEL.equals(body.getContentType())){//非html正文都以附件形势转发
 			InputStream in = null;
 			try { 
 				Long srcFileId=Long.parseLong(body.getContent());
 				if(transmitSendNewEdocId!=null&&!"".equals(transmitSendNewEdocId))
 				srcFileId=Long.parseLong(transmitSendNewEdocId);
 				String srcPath=fileManager.getFolder(createDate, true) + separator+ String.valueOf(srcFileId);
 				String newPath=CoderFactory.getInstance().decryptFileToTemp(srcPath);
 				String newPathName = SystemEnvironment.getSystemTempFolder() + separator + String.valueOf(UUIDLong.longUUID());
 				Util.jinge2StandardOffice(newPath, newPathName);
 				in = new FileInputStream(new File(newPathName)) ;
 				V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, summary.getSubject() + EdocUtil.getOfficeFileExt(body.getContentType()), createDate, false);
 				atts=new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE);
 			}catch (Exception e) {
 			   LOG.error("", e);
 			}finally{IOUtils.closeQuietly(in);}
	    		}else if(Constants.EDITOR_TYPE_PDF.equals(body.getContentType())){
	    			String srcPath=fileManager.getFolder(createDate, true) + separator+ String.valueOf(body.getContent());
	    			InputStream in = new FileInputStream(new File(srcPath)) ;
					V3XFile f = fileManager.save(in, ApplicationCategoryEnum.edoc, summary.getSubject() + ".pdf", createDate, false);
					atts=new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE);
	    		}else
	    		{		
	    			V3XFile f =fileManager.save(body.getContent()==null?"":body.getContent(), ApplicationCategoryEnum.edoc, summary.getSubject()+".htm", createDate, false);    			
					atts=new Attachment(f, ApplicationCategoryEnum.edoc, com.seeyon.ctp.common.filemanager.Constants.ATTACHMENT_TYPE.FILE);
	    		}
 		      return atts;
	}

	@Override
	public List<EdocSummary> getEdocSummaryList(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate) {
		return 
		edocSummaryDao.getEdocSummaryList(accountId,templeteId,workFlowState,startDate,endDate);
	}
	
	/**
	 * 获得以公文结束时间作为查询开始或结束时间的列表，其他条件与getEdocSummaryList接口一致
	 * @param templeteId  : 模板ID
	 * @param workFlowState ： 流程状态
	 * @param startDate : 开始时间
	 * @param endDate ： 结束时间
	 * @return
	 */
	@Override
	public List<EdocSummary> getEdocSummaryCompleteTimeList(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState,Date startDate,Date endDate){
		return 
		edocSummaryDao.getEdocSummaryCompleteTimeList(accountId,templeteId,workFlowState,startDate,endDate);
		
	}
	@Override
	public Integer getCaseCountByTempleteId(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate) {
		return edocSummaryDao.getCaseCountByTempleteId(
				accountId,
				templeteId, workFlowState, startDate, endDate);
	}
	@Override
	public Integer getAvgRunWorkTimeByTempleteId(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate) {
		return edocSummaryDao.getAvgRunWorkTimeByTempleteId(accountId,
				templeteId, workFlowState, startDate, endDate);
	}
	@Override
	public Integer getCaseCountGTSD(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate,
			Integer standarduration) {
		return edocSummaryDao.getCaseCountGTSD(
				accountId,templeteId, workFlowState, startDate, endDate, standarduration);
	}
	public Double getOverCaseRatioByTempleteId(Long accountId,
			Long templeteId,
			List<Integer> workFlowState,
			Date startDate,
			Date endDate){
		return edocSummaryDao.getOverCaseRatioByTempleteId(
				accountId, 
				templeteId, 
				workFlowState, 
				startDate, 
				endDate);
	}
	
    @Override
	public void subjectWrapDisabled(Long AccountId, Long userId, int listType, int edocType) {
		// TODO Auto-generated method stub
		this.edocSummaryDao.deleteSubjectWrapRecord(AccountId, userId, listType, edocType);
	}

	@Override
	public void subjectWrapSetting(EdocSubjectWrapRecord subjectWrapRecord) {
		// TODO Auto-generated method stub
		this.edocSummaryDao.saveSubjectWrapRecord(subjectWrapRecord);
	}

	@Override
	public boolean hasSubjectWrapRecordByCurrentUser(Long AccountId,
			Long userId, int listType, int edocType) {
		List<EdocSubjectWrapRecord> list = this.edocSummaryDao.getCurrentUserSubjectWrapRecord(AccountId, userId, listType, edocType);
		if(list.size() > 0){
			return true;
		}else {
			return false;
		}
	}
	//lijl添加
	public void saveOrUpdateEdocSummaryClean(EdocSummary o){
		edocSummaryDao.saveOrUpdateClean(o);
	}
	
	//GOV-4870 会签节点，竞争执行签章和文单签批前一个人执行后，后面的人再次签章/文单签批，虽然提出了已被人竞争执行，但是还是覆盖了前面的签章 
	public synchronized boolean isCompeteOver(String affairId) throws NumberFormatException, BusinessException
	{		
		if(Strings.isNotBlank(affairId)){
			AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
        	CtpAffair affair = affairManager.get(Long.parseLong(affairId));
        	//当公文竞争
            if(affair != null && (affair.getState() == StateEnum.col_competeOver.key() 
            					|| affair.getState() == StateEnum.col_done.key())){
            	return false;
            }
        }
		return true;
	}

	/**
     * 获取body对象
     * @param edocId   公文id
     * @param contentNum   body 编号
     * @return
     */
	public EdocBody getBodyByIdAndNum(String edocId,int contentNum) {
	    return this.edocBodyDao.getBodyByIdAndNum(edocId, contentNum);
	}
	@Override
	public void updateEdocSummaryCoverTime(Long edocId, boolean isCoverTime) {
		edocSummaryDao.updateEdocSummaryCoverTime(edocId, isCoverTime);
		
	}
	
	public List<EdocSummary> findEdocSummarysByIds(List<Long> ids){
		return edocSummaryDao.findEdocSummarysByIds(ids);
	}
	
	public List<SimpleEdocSummary> findSimpleEdocSummarysByIds(List<Long> ids){
		return edocSummaryDao.findSimpleEdocSummarysByIds(ids);
	}
	
	public void setEdocSummaryExtendManager(EdocSummaryExtendManager edocSummaryExtendManager) {
		this.edocSummaryExtendManager = edocSummaryExtendManager;
	}

    @Override
    public List<EdocBody> findEdocBodys(long summaryId) {
        return edocBodyDao.findEdocBodys(summaryId);
    }

	@Override
	public void update(Long id, Map<String, Object> columns) {
		edocSummaryDao.update(id, columns);
	}
	
	/**
     * 获取公文的默认节点
     * @return key: defaultNodeName/defaultNodeLable
     * @throws BusinessException
     */
	@AjaxAccess
    public Map<String,String> getEdocDefaultNode(String edocType,Long orgAccountId) throws BusinessException {
    	Map<String,String> tempMap = new HashMap<String,String>();
    	//发文、收文、签报各自设置默认节点权限
        String configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        if (ModuleType.edocSend.name().equals(edocType)) {
        	configCategory = EnumNameEnum.edoc_send_permission_policy.name();
        } else if(ModuleType.edocRec.name().equals(edocType)) { 
        	configCategory = EnumNameEnum.edoc_rec_permission_policy.name();
        } else if(ModuleType.edocSign.name().equals(edocType)) {
            configCategory = EnumNameEnum.edoc_qianbao_permission_policy.name();
        }
        PermissionVO permission = permissionManager.getDefaultPermissionByConfigCategory(configCategory,orgAccountId);
        String defaultNodeName = "";
        String defaultNodeLable = "";
        if (permission != null) {
        	defaultNodeName = permission.getName();
        	defaultNodeLable = permission.getLabel();
        }
        tempMap.put("defaultNodeName", defaultNodeName);
        tempMap.put("defaultNodeLable", defaultNodeLable);
        return tempMap;
    }
	@AjaxAccess
    public Map<String,String> getGovdocDefaultNode(String edocType,Long orgAccountId) throws BusinessException {
    	Map<String,String> tempMap = new HashMap<String,String>();
    	//发文、收文、签报各自设置默认节点权限
        String configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
        if (ModuleType.edocSend.name().equals(edocType)) {
        	configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
        } else if(ModuleType.edocRec.name().equals(edocType)) { 
        	configCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
        } else if(ModuleType.edocSign.name().equals(edocType)) {
            configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
        } else if (ModuleType.govdocSend.name().equals(edocType)) {
        	configCategory = EnumNameEnum.edoc_new_send_permission_policy.name();
        } else if(ModuleType.govdocRec.name().equals(edocType)) { 
        	configCategory = EnumNameEnum.edoc_new_rec_permission_policy.name();
        } else if(ModuleType.govdocExchange.name().equals(edocType)) {
            configCategory = EnumNameEnum.edoc_new_change_permission_policy.name();
        } else if(ModuleType.govdocSign.name().equals(edocType)) {
            configCategory = EnumNameEnum.edoc_new_qianbao_permission_policy.name();
        }
        PermissionVO permission = permissionManager.getDefaultPermissionByConfigCategory(configCategory,orgAccountId);
        String defaultNodeName = "";
        String defaultNodeLable = "";
        if (permission != null) {
        	defaultNodeName = permission.getName();
        	defaultNodeLable = permission.getLabel();
        }
        tempMap.put("defaultNodeName", defaultNodeName);
        tempMap.put("defaultNodeLable", defaultNodeLable);
        return tempMap;
	}
	
	@Override
	public void updateEdocSummaryArchiveId(Long edocId, Long archiveId) {
		edocSummaryDao.updateEdocSummaryArchiveId(edocId, archiveId);
	}
	
	
	
}
