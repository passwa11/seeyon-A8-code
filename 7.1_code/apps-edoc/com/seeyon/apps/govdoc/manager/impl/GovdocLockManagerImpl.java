package com.seeyon.apps.govdoc.manager.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.bo.GovdocLockObject;
import com.seeyon.apps.govdoc.helper.GovdocContentHelper;
import com.seeyon.apps.govdoc.manager.GovdocFormManager;
import com.seeyon.apps.govdoc.manager.GovdocLockManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.manager.GovdocWorkflowManager;
import com.seeyon.apps.govdoc.vo.GovdocLockParam;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.lock.manager.LockManager;
import com.seeyon.ctp.common.lock.manager.LockState;
import com.seeyon.ctp.common.office.HandWriteManager;
import com.seeyon.ctp.common.office.OfficeLockManager;
import com.seeyon.ctp.common.office.UserUpdateObject;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.content.CtpContentAll;
import com.seeyon.ctp.common.po.lock.Lock;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormAuthViewBean;
import com.seeyon.ctp.form.bean.FormAuthViewFieldBean;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldComBean.FormFieldComEnum;
import com.seeyon.ctp.form.bean.FormViewBean;
import com.seeyon.ctp.form.util.Enums.FormAuthorizationType;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocSummaryManagerImpl;

/**
 * @author Mr_Cai
 * 
 */
public class GovdocLockManagerImpl implements GovdocLockManager {

	private static final Log LOGGER = LogFactory.getLog(GovdocLockManagerImpl.class);

	private GovdocSummaryManager govdocSummaryManager;
	private GovdocWorkflowManager govdocWorkflowManager;
	private GovdocFormManager govdocFormManager;
	private LockManager lockManager;
	private HandWriteManager handWriteManager;
	private OfficeLockManager officeLockManager;
	private AffairManager affairManager;
	private OrgManager orgManager;
	private FormApi4Cap3 formApi4Cap3;

	private static Map<Long, Boolean> locksMap = new ConcurrentHashMap<Long, Boolean>();
	private final Object lockObject = new Object();
	private final Object CheckAndupdateLock = new Object();

	public FlipInfo getGovdocFormLockList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		if (flipInfo == null) {
			flipInfo = new FlipInfo();
		}

		List<GovdocLockParam> list = new ArrayList<GovdocLockParam>();
		GovdocLockParam param = null;

		Long summaryId = Long.parseLong(params.get("summaryId"));
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (summary != null && summary.getFormRecordid() != null) {
			Lock lock = govdocFormManager.getLock(summary.getFormRecordid());
			if (lock != null) {
				param = new GovdocLockParam();
				param.setSortNo(1);
				param.setLockTime(sdf.format(new Date(lock.getLockTime())));
				param.setOwnerId(lock.getOwner());
				param.setOwnerName(orgManager.getMemberById(lock.getOwner()).getName());
				param.setFromRecordId(summary.getFormRecordid());
				param.setSummaryId(summaryId);
			}
		} else {
			Map<String, UserUpdateObject> map = EdocSummaryManagerImpl.getUseObjectList();
			UserUpdateObject obj = map.get(String.valueOf(summaryId));
			if (obj != null) {
				param = new GovdocLockParam();
				param.setSortNo(1);
				param.setLockTime(sdf.format(obj.getLastUpdateTime()));
				param.setOwnerId(obj.getUserId());
				param.setOwnerName(obj.getUserName());
				param.setSummaryId(summaryId);
				param.setFromRecordId(-1L);
			}
		}
		if (param != null) {
			list.add(param);
		}
		flipInfo.setData(list);
		return flipInfo;
	}

	/**
	 * 公文单解锁
	 * 
	 * @param summaryId
	 * @param fromRecordId
	 * @param ownerId
	 * @param ownerName
	 * @return
	 */
	@Override
	public boolean unlockGovdocForm(String summaryId, String fromRecordId, String ownerId, String ownerName) {
		// 解除表单锁
		if (Strings.isNotBlank(fromRecordId) && !"-1".equals(fromRecordId)) {
			govdocFormManager.removeSessionMasterDataBean(Long.parseLong(fromRecordId));
			LOGGER.info("公文发起人" + AppContext.currentUserName() + "对公文《" + "》进行公文单解锁，被解锁人：" + ownerName + " 解锁时间：" + DateUtil.get19DateAndTime());
		} else {
			govdocSummaryManager.unlockEdocFormLock(summaryId, ownerId);
			LOGGER.info("公文发起人" + AppContext.currentUserName() + "对公文《" + "》进行公文单解锁，被解锁人：" + ownerName + " 解锁时间：" + DateUtil.get19DateAndTime());
		}
		return true;
	}

	@Override
	public void colDelLock(Long affairId) throws BusinessException {
		CtpAffair affair = affairManager.get(affairId);
		// 待发列表预览，删除，这个时候物理删除以后，展现页面解锁。查找不到affair了。
		if (affair == null) {
			LOGGER.info("公文解锁，获取affair为null,可能有问题");
			return;
		}
		Long summaryId = affair.getObjectId();
		EdocSummary summary = govdocSummaryManager.getSummaryById(summaryId);
		colDelLock(summary, affair);
	}

	public void colDelLock(EdocSummary summary, CtpAffair _affair) throws BusinessException {
		Map<String, String> param = new HashMap<String, String>();

		param.put("summaryId", String.valueOf(summary.getId()));
		param.put("processId", summary.getProcessId());
		param.put("formAppId", summary.getFormAppid() == null ? null : String.valueOf(summary.getFormAppid()));
		param.put("fromRecordId", summary.getFormRecordid() == null ? null : String.valueOf(summary.getFormRecordid()));
		param.put("bodyType", summary.getBodyType());

		ajaxColDelLock(param);
	}

	@Override
	public void ajaxColDelLock(Map<String, String> param) throws BusinessException {
		Long summaryId = Long.valueOf(param.get("summaryId"));
		String processId = Strings.isBlank(param.get("processId")) ? "" : param.get("processId");
		Long formAppId = Strings.isBlank(param.get("formAppId")) ? 0l : Long.valueOf(param.get("formAppId"));
		Long fromRecordId = Strings.isBlank(param.get("fromRecordId")) ? 0l : Long.valueOf(param.get("fromRecordId"));
		String delAll = param.get("delAll");
		if (summaryId == null) {
			LOGGER.info("协同解锁，获取summary为null,可能有问题");
			return;
		}
		// 解除流程锁
		try {
			if (Strings.isNotBlank(delAll) && "true".equals(delAll)) {
				lockManager.unlock(Long.valueOf(processId));
				lockManager.unlock(summaryId);
			} else {
				govdocWorkflowManager.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
				govdocWorkflowManager.releaseWorkFlowProcessLock(String.valueOf(summaryId), String.valueOf(AppContext.currentUserId()));
			}
			// 解除表单锁
			if (formAppId != null) {
				govdocFormManager.removeSessionMasterDataBean(fromRecordId);
				LOGGER.info("AjaxColDelLock协同页面离开，解锁表单锁：summaryid:" + summaryId + ",fromRecordId:" + fromRecordId);
			}
		} catch (Throwable e) {
			LOGGER.error("协同解锁失败colDelLock", e);
			throw new BusinessException(e);
		}
	}

	@Override
	public boolean canGetLock(Long affairId) {
		synchronized (lockObject) {
			boolean isLock = locksMap.get(affairId) == null ? false : true;
			if (isLock) {
				return false;
			} else {
				locksMap.put(affairId, Boolean.TRUE);
				LOGGER.info(AppContext.currentUserName() + ",内存锁加锁,affairId:" + affairId);
				return true;
			}
		}
	}

	@Override
	public void unlock(Long affairId) {
		synchronized (lockObject) {
			LOGGER.info(AppContext.currentUserName() + ",内存锁移除,affairId:" + affairId);
			locksMap.remove(affairId);
		}
	}

	public void unlockAll(EdocSummary summary) {
		unlockAll(summary, null);
	}

	public void unlockAll(EdocSummary summary, Long affairId, Long userId) {
		unlockAll(summary, userId);
		unlock(affairId);
	}

	public void unlockAll(EdocSummary summary, Long userId) {
		try {
			Long summaryId = null;
			if (summary != null) {
				summaryId = summary.getId();
				/** 1 流程解锁 */
				if (Strings.isNotBlank(summary.getProcessId())) {
					if (userId != null) {
						govdocWorkflowManager.releaseWorkFlowProcessLock(summary.getProcessId(), String.valueOf(userId));
					} else {

					}
				}
				/** 2 公文单解锁 */
				try {
					lockManager.unlock(Long.valueOf(summary.getProcessId()));
					lockManager.unlock(summaryId);
					if (summary.getFormRecordid() != null) {
						govdocFormManager.removeSessionMasterDataBean(summary.getFormRecordid());
					}
				} catch (Exception e) {
					LOGGER.error("解锁公文单失败 userId:" + userId + " summaryId:" + summaryId, e);
				}
				/** 3 正文解锁(多人同时修改同一正文) */
				if (summary.getGovdocType().intValue() == 0) {
					String bodyType = summary.getFirstBody().getContentType();
					if (Constants.EDITOR_TYPE_OFFICE_EXCEL.equals(bodyType) || Constants.EDITOR_TYPE_OFFICE_WORD.equals(bodyType)
							|| Constants.EDITOR_TYPE_WPS_EXCEL.equals(bodyType) || Constants.EDITOR_TYPE_WPS_WORD.equals(bodyType)) {
						try {
							String contentId = summary.getFirstBody().getContent();
							handWriteManager.deleteUpdateObj(contentId);
						} catch (Exception e) {
							LOGGER.error("解锁office正文失败 userId:" + userId + " summaryId:" + summary.getId(), e);
						}
					} else {
						try {
							handWriteManager.deleteUpdateObj(String.valueOf(summaryId));
						} catch (Exception e) {
							LOGGER.error("解锁html正文失败 userId:" + userId + " summaryId:" + summaryId, e);
						}
					}
				}
				/** 4 解锁文档中的所有office锁 */
				officeLockManager.unlockAll(summary.getId());
			}
		} catch (Exception e) {
			LOGGER.error("公文解锁出错", e);
		}
	}

	@SuppressWarnings("deprecation")
	@AjaxAccess
	@Override
	public GovdocLockObject formAddLock(Long affairId) throws BusinessException {
		GovdocLockObject obj = null;
		CtpAffair affair = affairManager.get(affairId);
		if (affair != null && !("zhihui").equals(affair.getNodePolicy()) && !("yuedu").equals(affair.getNodePolicy())) {
			if (affair.getState().intValue() == StateEnum.col_pending.getKey()) {
				boolean isReadOnly = false;
				CtpContentAll content = GovdocContentHelper.getFormContentByModuleId(affair.getObjectId());
				if (content != null) {
					Long masterId = content.getContentDataId();
					Lock lock = formApi4Cap3.getLock(masterId);
					Long formapp_id = affair.getFormAppId();
					if (formapp_id == null) {
						if (content.getContentTemplateId() != null) {
							formapp_id = content.getContentTemplateId();
						}
					}
					if (formapp_id == null) {
						return obj;
					}
					Integer subapp = affair.getSubApp();
					String operation_id = null;
					String formview_id = null;
					if (affair.getApp() == ApplicationCategoryEnum.edoc.getKey()) {
						Map<String, Object> vomMap = new HashMap<String, Object>();
						vomMap.put("formAppid", formapp_id);
						vomMap.put("govdocType", subapp);
						String rightId = govdocFormManager.getGovdocFormViewRight(vomMap, affair);
						if (Strings.isNotBlank(rightId)) {
							if (rightId.contains("_")) {
								rightId = rightId.split("_")[0];
							}
							String[] para = rightId.split("\\.");
							formview_id = para[0];
							operation_id = para[1];
						} else {
							// 自定义节点没有表单操作权限时 chenyq 2018-01-09
							isReadOnly = true;
						}
					} else {
						operation_id = affair.getFormOperationId() + "";
						// 视图id
						formview_id = affair.getFormId() + "";
					}
					if (Strings.isNotBlank(formview_id) && Strings.isNotBlank(operation_id)) {
						FormBean formBean = formApi4Cap3.getForm(formapp_id);
						FormViewBean formViewBean = formBean.getFormView(Long.valueOf(formview_id));
						//错误数据兼容
						if(formViewBean == null) {
							isReadOnly = true;
						}else {
							List<FormAuthViewBean> fav = formViewBean.getAllOperations();
							for (FormAuthViewBean formAuthViewBean : fav) {
								String _operation_id = formAuthViewBean.getId().toString();
								if (operation_id.equals(_operation_id)) {
									if (formAuthViewBean != null) {
										String auth = formAuthViewBean.getType();
										isReadOnly = FormAuthorizationType.show.getKey().equals(auth);
									}
									if (!isReadOnly && affair.getApp() == ApplicationCategoryEnum.edoc.getKey()) {
										List<FormAuthViewFieldBean> favf = formAuthViewBean.getFormAuthorizationFieldList();
										boolean isTempEdit = false;
										for (FormAuthViewFieldBean formAuthViewFieldBean : favf) {
											String inputType = formAuthViewFieldBean.getFormFieldBean().getInputType();
											String formfieldtype = null;
											if (affair.getApp() == ApplicationCategoryEnum.edoc.getKey()) {
												formfieldtype = FormFieldComEnum.EDOCFLOWDEALOPITION.getKey();
											} else {
												formfieldtype = FormFieldComEnum.FLOWDEALOPITION.getKey();
											}
											// 不判断意见元素
											if (inputType.equals(formfieldtype)) {
												continue;
											} else {
												if ("edit".equals(formAuthViewFieldBean.getAccess())) {
													isTempEdit = true;
													break;
												}
											}
										}
										if (isTempEdit) {
											isReadOnly = false;
										} else {
											isReadOnly = true;
										}
									}
									break;
								}
							}
						}
					}

					// 判断一下当前锁是否有效
					if (lock != null) {
						if (!LockState.effective_lock.equals(lockManager.isValid(lock))) {
							lockManager.unlock(masterId);
							lock = null;
						}
					}
					if (!isReadOnly) {
						synchronized (CheckAndupdateLock) {
							// Long masterId = formRecordId;
							// Lock lock = formManager.getLock(masterId);
							obj = new GovdocLockObject();
							obj.setCanSubmit("1");
							if (lock == null) {
								Long userId = AppContext.currentUserId();
								LOGGER.info("开始加锁 - lock is null :,userId= " + userId + ",affairId:" + affairId);
								formApi4Cap3.lockFormData(masterId, null, null);
								obj.setOwner(AppContext.currentUserId());
								obj.setLoginName(AppContext.currentUserLoginName());
								obj.setLoginTimestamp(AppContext.getCurrentUser().getLoginTimestamp().getTime());
								obj.setCanSubmit("1");
							} else {
								obj.setLoginName(Functions.showMemberName(lock.getOwner()));
								obj.setOwner(lock.getOwner());
								obj.setLoginTimestamp(lock.getLoginTime());
								obj.setFrom(lock.getFrom());

								boolean isOne = lock.getOwner() == AppContext.currentUserId();

								if (isOne) {
									LOGGER.info("SAME AND DIFFERENT,user:" + AppContext.currentUserId() + ",affairId:" + affairId);
								}

								LOGGER.info("判断锁：isOne=" + isOne);

								if (isOne) {
									obj.setCanSubmit("1");
									LOGGER.info("可以提交！");
								} else {
									obj.setCanSubmit("0");
									LOGGER.info("SAME AND DIFFERENT,user:" + AppContext.currentUserId() + ",affairId:" + affairId);
									LOGGER.info("不能可以提交！！！");
								}
							}
						}
					}
				}
			}
		}
		return obj;
	}

	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}

	public void setGovdocFormManager(GovdocFormManager govdocFormManager) {
		this.govdocFormManager = govdocFormManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}

	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	public void setGovdocWorkflowManager(GovdocWorkflowManager govdocWorkflowManager) {
		this.govdocWorkflowManager = govdocWorkflowManager;
	}

	public void setHandWriteManager(HandWriteManager handWriteManager) {
		this.handWriteManager = handWriteManager;
	}

	public void setOfficeLockManager(OfficeLockManager officeLockManager) {
		this.officeLockManager = officeLockManager;
	}

	public void setFormApi4Cap3(FormApi4Cap3 formApi4Cap3) {
        this.formApi4Cap3 = formApi4Cap3;
    }

}
