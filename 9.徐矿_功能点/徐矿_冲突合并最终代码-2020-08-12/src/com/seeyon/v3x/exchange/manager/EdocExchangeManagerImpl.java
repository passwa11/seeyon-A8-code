package com.seeyon.v3x.exchange.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.index.manager.IndexManager;
import com.seeyon.apps.sursenexchange.api.SendToSursenParam;
import com.seeyon.apps.sursenexchange.api.SursenExchangeApi;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.IConfigPublicKey;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.AffairDao;
import com.seeyon.ctp.common.content.affair.AffairExtPropEnums;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.content.affair.AffairUtil;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.operationlog.manager.OperationlogManager;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.MemberPost;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.privilege.manager.PrivilegeManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;
import com.seeyon.v3x.edoc.domain.EdocObjTeamMember;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSignReceipt;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.enums.EdocMessageFilterParamEnum;
import com.seeyon.v3x.edoc.event.EdocSignEvent;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;
import com.seeyon.v3x.edoc.manager.EdocMessageHelper;
import com.seeyon.v3x.edoc.manager.EdocObjTeamManager;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.edoc.manager.EdocRoleHelper;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;
import com.seeyon.v3x.edoc.util.EactionType;
import com.seeyon.v3x.edoc.util.EdocUtil;
import com.seeyon.v3x.edoc.webmodel.EdocMarkModel;
import com.seeyon.v3x.exchange.dao.EdocRecieveRecordDao;
import com.seeyon.v3x.exchange.dao.EdocSendDetailDao;
import com.seeyon.v3x.exchange.dao.EdocSendRecordReferenceDao;
import com.seeyon.v3x.exchange.domain.EdocExchangeTurnRec;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendDetail;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecordReference;
import com.seeyon.v3x.exchange.enums.EdocExchangeMode.EdocExchangeModeEnum;
import com.seeyon.v3x.exchange.exception.ExchangeException;
import com.seeyon.v3x.exchange.util.Constants;
import com.seeyon.v3x.exchange.util.ExchangeUtil;

public class EdocExchangeManagerImpl implements EdocExchangeManager {

    private static final Log LOGGER = LogFactory.getLog(EdocExchangeManager.class);

    private SendEdocManager sendEdocManager;
    private EdocSendDetailDao edocSendDetailDao;
    private EdocRecieveRecordDao edocRecieveRecordDao;
    private RecieveEdocManager recieveEdocManager;
    private OrgManager orgManager;
    private AffairManager affairManager;
    private EdocSummaryManager edocSummaryManager;
    private UserMessageManager userMessageManager;
    private OperationlogManager operationlogManager;
    private EdocObjTeamManager edocObjTeamManager;
    private EdocRegisterManager edocRegisterManager;
    private ConfigManager configManager;
    private ExchangeAccountManager exchangeAccountManager;
    private PrivilegeManager privilegeManager;
    private SursenExchangeApi sursenExchangeApi;
    private AppLogManager appLogManager;
    private EnumManager v3xmetadataManager;
    private AttachmentManager attachmentManager = null;
    private EdocSendRecordReferenceDao edocSendRecordReferenceDao;
    private EdocExchangeTurnRecManager edocExchangeTurnRecManager;

    public void setEdocExchangeTurnRecManager(EdocExchangeTurnRecManager edocExchangeTurnRecManager) {
        this.edocExchangeTurnRecManager = edocExchangeTurnRecManager;
    }

    public void setEdocSendRecordReferenceDao(EdocSendRecordReferenceDao edocSendRecordReferenceDao) {
        this.edocSendRecordReferenceDao = edocSendRecordReferenceDao;
    }

    public void setPrivilegeManager(PrivilegeManager privilegeManager) {
        this.privilegeManager = privilegeManager;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setV3xmetadataManager(EnumManager v3xmetadataManager) {
        this.v3xmetadataManager = v3xmetadataManager;
    }


    public void setSursenExchangeApi(SursenExchangeApi sursenExchangeApi) {
        this.sursenExchangeApi = sursenExchangeApi;
    }

    public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
        this.edocRegisterManager = edocRegisterManager;
    }

    public EdocSendRecord getEdocSendRecordByDetailId(long detailId) {
        return sendEdocManager.getEdocSendRecordByDetailId(detailId);
    }

    public SendEdocManager getSendEdocManager() {
        return sendEdocManager;
    }

    public void setSendEdocManager(SendEdocManager sendEdocManager) {
        this.sendEdocManager = sendEdocManager;
    }

    public EdocSendDetailDao getEdocSendDetailDao() {
        return edocSendDetailDao;
    }

    public void setEdocSendDetailDao(EdocSendDetailDao edocSendDetailDao) {
        this.edocSendDetailDao = edocSendDetailDao;
    }

    public RecieveEdocManager getRecieveEdocManager() {
        return recieveEdocManager;
    }

    public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
        this.recieveEdocManager = recieveEdocManager;
    }

    public EdocRecieveRecordDao getEdocRecieveRecordDao() {
        return edocRecieveRecordDao;
    }

    public void setEdocRecieveRecordDao(
            EdocRecieveRecordDao edocRecieveRecordDao) {
        this.edocRecieveRecordDao = edocRecieveRecordDao;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public OrgManager getOrgManager() {
        return orgManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setExchangeAccountManager(
            ExchangeAccountManager exchangeAccountManager) {
        this.exchangeAccountManager = exchangeAccountManager;
    }

    public void setAppLogManager(AppLogManager appLogManager) {
        this.appLogManager = appLogManager;
    }

    /**
     * 当前登录人员是否具有当前登录单位的收文登记权
     */
    public boolean isEdocCreateRole() {
        boolean ret = false;
        try {
            ret = EdocRoleHelper.isEdocCreateRole(EdocEnum.edocType.recEdoc.ordinal());
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ret;
    }

    /*
     *  判断当前人有没有登记权限
     */
    public int isEdocRegister(long registerUserId) throws BusinessException {
        //获取当前用户
        User user = AppContext.getCurrentUser();
        boolean isEdocRegister = true;
        int regValidation = 0;
        if (user.getId().longValue() != registerUserId) {
            //根据登记人ID获取登记
            V3xOrgMember member = orgManager.getMemberById(registerUserId);
            //获取登记的登记权限
            isEdocRegister = EdocRoleHelper.isEdocCreateRole(member.getOrgAccountId(), member.getId(), EdocEnum.edocType.recEdoc.ordinal());
            regValidation = 2;
        } else {
            isEdocRegister = EdocRoleHelper.isEdocCreateRole(user.getLoginAccount(), user.getId(), EdocEnum.edocType.recEdoc.ordinal());
            regValidation = 1;
        }
        /*
         * 如果权限验证通过，可以进行登记。
         * 0等于验证通过
         * 1表示当前用户已经不是登记人
         * 2表示被代理人已经没有登记权限
         */
        if (isEdocRegister) {
            regValidation = 0;
        }
        return regValidation;
    }

    /**
     * 判断是否具有指定单位下的收文登记权
     *
     * @param userId
     * @param exchangeAccountId
     * @return
     */
    public boolean isEdocCreateRole(Long userId, Long exchangeAccountId) {
        boolean ret = false;
        try {
            ret = EdocRoleHelper.isEdocCreateRole(exchangeAccountId, userId, EdocEnum.edocType.recEdoc.ordinal());
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ret;
    }

    public boolean isEdocCreateRole(Long userId, Long exchangeAccountId, String resCode) {
        //OA-13942  应用检查---在待签收、已签收中变更登记人，变更时没有提示，且被变更的这个人员都没有给公文管理的资源，变更时也没做是否这个人有登记的权限
        boolean ret = false;
        try {
            ret = privilegeManager.checkByReourceCode(resCode, userId, exchangeAccountId);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        return ret;
    }


    /**
     * 判断是否具有指定单位下的收文分发权
     *
     * @param userId
     * @param exchangeAccountId
     * @return
     */
    public boolean isEdocCreateRole(Long userId, Long exchangeAccountId, int edocType) {
        boolean ret = false;
        try {
            //exchangeAccountId来自EdocRegister.OrgAccountid.但是交换可能是部门,即exchangeAccountId实际可能是部门Id,检查权限需要转为单位Id
            V3xOrgDepartment department = orgManager.getDepartmentById(exchangeAccountId);
            if (department != null) {
                exchangeAccountId = department.getOrgAccountId();
            }
            if (edocType == EdocEnum.edocType.recEdoc.ordinal()) {
                return isEdocCreateRole(userId, exchangeAccountId);
            } else if (edocType == EdocEnum.edocType.distributeEdoc.ordinal()) {
                ret = EdocRoleHelper.isEdocCreateRole(exchangeAccountId, userId, EdocEnum.edocType.distributeEdoc.ordinal());
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ret;
    }


    public String checkExchangeRole(String typeAndIds) {
        StringBuilder msg = new StringBuilder("");
        try {
            List<V3xOrgMember> roles = null;
            V3xOrgEntity tempEntity = null;
            String[] items = typeAndIds.split(V3xOrgEntity.ORG_ID_DELIMITER);
            String data[] = null;

            //对机构组进行处理
            List<String> strLs = new ArrayList<String>();
            for (String temp : items) {
                data = temp.split("[|]");
                if (EdocObjTeam.ENTITY_TYPE_OrgTeam.equals(data[0])) {
                    EdocObjTeam et = edocObjTeamManager.getById(Long.parseLong(data[1]));
                    Set<EdocObjTeamMember> mems = et.getEdocObjTeamMembers();
                    for (EdocObjTeamMember mem : mems) {
                        strLs.add(mem.getTeamType() + "|" + mem.getMemberId());
                    }
                } else {
                    strLs.add(temp);
                }
            }
            items = new String[strLs.size()];
            strLs.toArray(items);
            Long accountId = AppContext.getCurrentUser().getLoginAccount();
            for (String item : items) {
                data = item.split("[|]");
                if (V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(data[0])) {
                    roles = EdocRoleHelper.getAccountExchangeUsers(Long.parseLong(data[1]));
                } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(data[0])) {
                    tempEntity = orgManager.getEntity(item);
                    roles = EdocRoleHelper.getDepartMentExchangeUsers(tempEntity.getOrgAccountId(), tempEntity.getId());
                } else {
                    tempEntity = null;
                    roles = null;
                    continue;
                }
                if (roles == null || roles.size() <= 0) {
                    if (tempEntity == null) {
                        tempEntity = orgManager.getEntity(item);
                    }
                    if (msg.length() > 0) {
                        msg.append(",");
                    }
                    if (tempEntity != null) {
                        msg.append(tempEntity.getName());
                        if (!tempEntity.getOrgAccountId().equals(accountId)) {
                            V3xOrgEntity acc = orgManager.getEntityById(V3xOrgAccount.class, tempEntity.getOrgAccountId());
                            if (acc != null) {
                                msg.append(ResourceUtil.getString("edoc.symbol.opening.brace")).append(((V3xOrgAccount) acc).getShortName()).append(ResourceUtil.getString("edoc.symbol.closing.brace"));
                            }
                        }
                    }
                }
                tempEntity = null;
                roles = null;
            }

        } catch (Exception e) {
            LOGGER.error("公文收发员角色验证异常", e);
        }
        String retMsg = msg.toString();
        if ("".equals(retMsg)) {
            retMsg = "check ok";
        }
        return retMsg;
    }

    // 重写上面的方法  --向凡  修复GOV-4427  添加了一个参数 userId，主要是Ajax异步请求验证的时候传入 选中的交换员的ID，需要验证此交换员是否具有交换权限，不需要时传入 null即可！
    public String checkExchangeRole(String typeAndIds, String userId) {
        StringBuilder msg = new StringBuilder("");
        try {
            List<V3xOrgMember> roles = null;
            V3xOrgEntity tempEntity = null;
            String[] items = typeAndIds.split(V3xOrgEntity.ORG_ID_DELIMITER);
            String data[] = null;

            //对机构组进行处理
            List<String> strLs = new ArrayList<String>();
            for (String temp : items) {
                data = temp.split("[|]");
                if (EdocObjTeam.ENTITY_TYPE_OrgTeam.equals(data[0])) {
                    EdocObjTeam et = edocObjTeamManager.getById(Long.parseLong(data[1]));
                    Set<EdocObjTeamMember> mems = et.getEdocObjTeamMembers();
                    for (EdocObjTeamMember mem : mems) {
                        strLs.add(mem.getTeamType() + "|" + mem.getMemberId());
                    }
                } else {
                    strLs.add(temp);
                }
            }
            items = new String[strLs.size()];
            strLs.toArray(items);
            Long accountId = AppContext.getCurrentUser().getLoginAccount();
            for (String item : items) {
                data = item.split("[|]");
                if (V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(data[0])) {
                    roles = EdocRoleHelper.getAccountExchangeUsers(Long.parseLong(data[1]));
                } else if (V3xOrgEntity.ORGENT_TYPE_DEPARTMENT.equals(data[0])) {
                    tempEntity = orgManager.getEntity(item);
                    roles = EdocRoleHelper.getDepartMentExchangeUsers(tempEntity.getOrgAccountId(), tempEntity.getId());
                } else {
                    tempEntity = null;
                    roles = null;
                    continue;
                }
                if (roles == null || roles.size() <= 0) {
                    if (tempEntity == null) {
                        tempEntity = orgManager.getEntity(item);
                    }
                    if (msg.length() > 0) {
                        msg.append(",");
                    }
                    if (tempEntity != null) {
                        msg.append(tempEntity.getName());
                        if (!tempEntity.getOrgAccountId().equals(accountId)) {
                            V3xOrgEntity acc = orgManager.getEntityById(V3xOrgAccount.class, tempEntity.getOrgAccountId());
                            if (acc != null) {
                                msg.append(ResourceUtil.getString("edoc.symbol.opening.brace")).append(((V3xOrgAccount) acc).getShortName()).append(ResourceUtil.getString("edoc.symbol.closing.brace"));
                            }
                        }
                    }
                } else if (Strings.isNotBlank(userId)) {
                    for (V3xOrgMember orgMember : roles) {
                        if (Long.valueOf(userId).longValue() == orgMember.getId().longValue()) {
                            msg = new StringBuilder("");
                            break;
                        } else {
                            msg = new StringBuilder("changed");//交换员 已经被单位管理员更改 --向凡
                        }
                    }
                }
                tempEntity = null;
                roles = null;
            }
        } catch (Exception e) {
            msg = new StringBuilder("unknow err");
        }
        String retMsg = msg.toString();
        if ("".equals(retMsg)) {
            retMsg = "check ok";
        }
        return retMsg;
    }

    public void sendEdoc(EdocSendRecord edocSendRecord, long sendUserId, String sender, boolean reSend) throws Exception {
        sendEdoc(edocSendRecord, sendUserId, sender, null, reSend, null);
    }

    /**
     * 交换-发送公文（签发）
     */
    public Map<String, String> sendEdoc(EdocSendRecord edocSendRecord, long sendUserId, String sender, Long agentToId, boolean reSend, String[] exchangeModeValues) throws Exception {
        //EdocSendRecord edocSendRecord = sendEdocManager.getEdocSendRecord(id);
        if (!ExchangeUtil.isEdocExchangeToSendRecord(edocSendRecord.getStatus()) && (reSend == false)) {
            return null;
        }

        User user = AppContext.getCurrentUser();

        Set<EdocSendDetail> sendDetails = (Set<EdocSendDetail>) edocSendRecord
                .getSendDetails();

        Map<String, String> map = new HashMap<String, String>();
        EdocSummary summary = edocSummaryManager.findById(edocSendRecord.getEdocId());

        boolean hasPlugin = AppContext.hasPlugin("sursenExchange");
        String internalExchange = "";
        String sursenExchange = "";
        if (exchangeModeValues != null) {
            for (String exchangeModeValue : exchangeModeValues) {
                if (String.valueOf(EdocExchangeModeEnum.internal.getKey()).equals(exchangeModeValue)) {
                    internalExchange = String.valueOf(EdocExchangeModeEnum.internal.getKey());
                } else if (String.valueOf(EdocExchangeModeEnum.sursen.getKey()).equals(exchangeModeValue)) {
                    sursenExchange = String.valueOf(EdocExchangeModeEnum.sursen.getKey());
                }
            }
        }
        // 1 不安装该插件走原来的逻辑  2在安装该插件情况给待签收发送数据，（1）内部（致远）交换和书生交换一起被选中  （2）只有内部（致远）交换选中 。
        if ((!hasPlugin || (hasPlugin && String.valueOf(EdocExchangeModeEnum.internal.getKey()).equals(internalExchange))) && sendDetails != null && sendDetails.size() > 0) {

            Iterator<EdocSendDetail> it = sendDetails.iterator();
            //待发送时，选择的发送单位要记录到接收记录里面，用于显示
            String[] aRecUnit = new String[3];
            aRecUnit[0] = edocSendRecord.getSendEntityNames();
            while (it.hasNext()) {
                EdocSendDetail sendDetail = (EdocSendDetail) it.next();
                String exchangeOrgId = sendDetail.getRecOrgId();
                int exchangeOrgType = sendDetail.getRecOrgType();
                long replyId = sendDetail.getId();

                String recieveId = recieveEdocManager.create(edocSendRecord, Long
                                .valueOf(exchangeOrgId), exchangeOrgType, replyId,
                        aRecUnit, sender, agentToId, summary);
                map.put(sendDetail.getRecOrgId(), recieveId);
            }

        }
        // 插件是提前  选择书生交换或者 书生交换和内部交换
        String accountName = "";
        if (hasPlugin && String.valueOf(EdocExchangeModeEnum.sursen.getKey()).equals(sursenExchange)) {
            StringBuffer sb = new StringBuffer();
            String data[] = null;
            String accountNames = "";
            String sendedTypeIds = edocSendRecord.getSendedTypeIds();
            String[] items = sendedTypeIds.split(V3xOrgEntity.ORG_ID_DELIMITER);

            for (int x = 0; x < items.length; x++) {
                String item = items[x];
                data = item.split("[|]");
                if (data.length > 0) {
                    if ("Account".equals(data[0])) {
                        accountNames = orgManager.getAccountById(Long.valueOf(data[1])).getName();
                        sb.append(accountNames + ";");
                    } else if ("Department".equals(data[0])) {
                        accountNames = orgManager.getDepartmentById(Long.valueOf(data[1])).getName();
                        sb.append(accountNames + ";");
                    } else if ("ExchangeAccount".equals(data[0])) {
                        // 外部人员接口
                        accountNames = exchangeAccountManager.getExchangeAccount(Long.valueOf(data[1])).getName();
                        sb.append(accountNames + ";");
                    }
                }

            }
            accountName = sb.toString();
            if (Strings.isNotBlank(accountName)) {
                accountName = accountName.substring(0, accountName.length() - 1);
            }
        }


        // 当内部（致远）交换和书生交换一起被选中，并且有该插件，如果只是选中书生交换，下面直接更新状态
        if (hasPlugin && (String.valueOf(EdocExchangeModeEnum.internal.getKey()).equals(internalExchange) && String.valueOf(EdocExchangeModeEnum.sursen.getKey()).equals(sursenExchange))) {

            EdocSendRecord record = new EdocSendRecord();
            record.setIdIfNew();
            record.setSubject(edocSendRecord.getSubject());
            record.setDocType(edocSendRecord.getDocType());
            record.setDocMark(edocSendRecord.getDocMark());
            record.setSecretLevel(edocSendRecord.getSecretLevel());
            record.setUrgentLevel(edocSendRecord.getUrgentLevel());
            record.setSendUnit(edocSendRecord.getSendUnit());
            record.setIssuer(edocSendRecord.getIssuer());
            record.setIssueDate(edocSendRecord.getIssueDate());
            record.setCopies(edocSendRecord.getCopies());
            record.setEdocId(edocSendRecord.getEdocId());
            record.setExchangeOrgId(edocSendRecord.getExchangeOrgId());
            record.setExchangeAccountId(edocSendRecord.getExchangeAccountId());
            record.setExchangeType(edocSendRecord.getExchangeType());
            record.setSendUserId(agentToId == null ? sendUserId : agentToId);
            long l = System.currentTimeMillis();
            record.setSendTime(new Timestamp(l));
            record.setCreateTime(new Timestamp(l));
            record.setContentNo(edocSendRecord.getContentNo());
            record.setSendedTypeIds(edocSendRecord.getSendedTypeIds());
            record.setStepBackInfo(edocSendRecord.getStepBackInfo());
            record.setAssignType(edocSendRecord.getAssignType());
            record.setIsBase(edocSendRecord.getIsBase());
            record.setIsTurnRec(edocSendRecord.getIsTurnRec());
            record.setSendNames(edocSendRecord.getSendNames());


            SendToSursenParam param = generateSursenParam(summary, edocSendRecord, accountName);
            boolean sendToSursen = sursenExchangeApi.sendToSursen(param);

            String sendFailed = "failed";
            // 如果发生成功，更改该状态并保存
            if (sendToSursen) {
                record.setStatus(EdocSendRecord.Exchange_iStatus_Sent);
                //同时内部交换和书生交换都设置成非原发，发文登记簿过滤重复，这里取巧了，修改请注意相关
                record.setIsBase(EdocSendRecord.Exchange_Base_NO);
                record.setExchangeMode(EdocExchangeModeEnum.sursen.getKey());
                edocSendDetailDao.save(record);
                appLogManager.insertLog(user, 342, user.getName(), record.getSubject());
            } else {
                record.setExchangeMode(EdocExchangeModeEnum.internal.getKey());
                record.setStatus(EdocSendRecord.Exchange_iStatus_Tosend);
                edocSendDetailDao.save(record);
                appLogManager.insertLog(user, 343, user.getName(), record.getSubject());
                map.put("sendFailed", sendFailed);
            }

        }
        // 更新内部交换数据和书生交换数据，当选书生交换时，直接更新该状态，不单独创建新对象
        edocSendRecord.setSendUserId(agentToId == null ? sendUserId : agentToId);
        long l = System.currentTimeMillis();
        edocSendRecord.setSendTime(new Timestamp(l));
        edocSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Sent);
        // 如果只是选中书生交换，设置exchangeMode，更改状态
        if (hasPlugin && String.valueOf(EdocExchangeModeEnum.sursen.getKey()).equals(sursenExchange) && !String.valueOf(EdocExchangeModeEnum.internal.getKey()).equals(internalExchange)) {

            SendToSursenParam param = generateSursenParam(summary, edocSendRecord, accountName);
            boolean sendToSursen = sursenExchangeApi.sendToSursen(param);

            String sendFailed = "failed";
            if (sendToSursen) {
                edocSendRecord.setExchangeMode(EdocExchangeModeEnum.sursen.getKey());
                appLogManager.insertLog(user, 342, user.getName(), edocSendRecord.getSubject());
            } else {
                edocSendRecord.setExchangeMode(EdocExchangeModeEnum.internal.getKey());
                edocSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Tosend);
                appLogManager.insertLog(user, 343, user.getName(), edocSendRecord.getSubject());
                map.put("sendFailed", sendFailed);
            }
        }

        if (reSend) {
            sendEdocManager.reSend(edocSendRecord, summary);
        } else {
            sendEdocManager.update(edocSendRecord);
        }


        return map;
    }

    /**
     * 生成发送到书生交换所需要的数据
     *
     * @return
     * @Author : xuqw
     * @Date : 2015年9月1日下午5:31:20
     */
    private SendToSursenParam generateSursenParam(EdocSummary edocSummary, EdocSendRecord edocSendRecord, String accountName) {

        SendToSursenParam param = new SendToSursenParam();

        param.setEdocId(edocSummary.getId());
        param.setSendUnitNames(edocSummary.getSendUnit());
        param.setCreateDate(edocSummary.getCreateTime());

        param.setDoctitle(edocSummary.getSubject());//标题
        param.setSubject(edocSummary.getKeywords());//主题词
        param.setPublishperson(edocSummary.getIssuer());
        // 处理公文号的问题 拆分公文号如:省信（2013）0001号
        param.setDocMark(edocSummary.getDocMark());

        param.setCreator(edocSummary.getCreatePerson());
        param.setSendTo(edocSummary.getSendTo());
        param.setCc(edocSummary.getCopyTo());

        // 获取枚举数据
        String edocLevel = "";// 公文密级
        try {
            if (Strings.isNotBlank(edocSummary.getSecretLevel())) {
                CtpEnumBean metadata = v3xmetadataManager.getEnum(MetadataNameEnum.edoc_secret_level.name());// 得到公文密级的枚举
                CtpEnumItem item = metadata.getItem(edocSummary.getSecretLevel());
                if (Strings.isNotBlank(metadata.getResourceBundle())) {
                    edocLevel = ResourceBundleUtil.getString(metadata.getResourceBundle(), item.getLabel());
                }
            }
        } catch (Exception e) {
            LOGGER.info("密级为空", e);
        }

        String urgentLevel = "";
        try {
            if (Strings.isNotBlank(edocSummary.getUrgentLevel())) {
                CtpEnumBean metadata = v3xmetadataManager.getEnum(MetadataNameEnum.edoc_urgent_level.name());// 得到公文紧急程度的枚举*/
                CtpEnumItem item = metadata.getItem(edocSummary.getUrgentLevel());
                if (Strings.isNotBlank(metadata.getResourceBundle())) {
                    urgentLevel = ResourceBundleUtil.getString(metadata.getResourceBundle(), item.getLabel());
                }
            }
        } catch (Exception e) {
            LOGGER.info("紧急程度为空", e);
        }

        param.setSecret(edocLevel);
        param.setEmergency(urgentLevel);

        //正文
        EdocBody body = edocSummary.getFirstBody();
        if (body != null) {
            param.setBodyType(body.getContentType());
            param.setBodyContent(body.getContent());
            param.setBodyContentType(body.getContentType());
        }

        //附件
        if (edocSummary.isHasAttachments()) {
            List<Attachment> attachments = attachmentManager.getByReference(edocSummary.getId());
            param.setAttachments(attachments);
        }


        param.setDocid(edocSendRecord.getId().toString());

        param.setReciveUnitName(accountName);

        return param;
    }


    public void sendEdoc(long id, long sendUserId) throws Exception {
        User user = AppContext.getCurrentUser();
        EdocSendRecord edocSendRecord = sendEdocManager.getEdocSendRecord(id);
        if (!ExchangeUtil.isEdocExchangeToSendRecord(edocSendRecord.getStatus())) {
            return;
        }

        Set<EdocSendDetail> sendDetails = (Set<EdocSendDetail>) edocSendRecord
                .getSendDetails();

        EdocSummary summary = edocSummaryManager.findById(edocSendRecord.getEdocId());
        if (sendDetails != null && sendDetails.size() > 0) {
            Iterator<EdocSendDetail> it = sendDetails.iterator();
            while (it.hasNext()) {
                EdocSendDetail sendDetail = (EdocSendDetail) it.next();
                String exchangeOrgId = sendDetail.getRecOrgId();
                int exchangeOrgType = sendDetail.getRecOrgType();
                long replyId = sendDetail.getId();
                String[] aRecUnit = new String[3];
                aRecUnit[0] = sendDetail.getRecOrgName();
                // 内部交换
                if (exchangeOrgType != EdocSendRecord.Exchange_Send_iExchangeType_ExternalOrg) {
                    recieveEdocManager.create(edocSendRecord,
                            Long.valueOf(exchangeOrgId), exchangeOrgType,
                            replyId, aRecUnit, summary);
                }
            }
        }
        edocSendRecord.setSendUserId(sendUserId);
        edocSendRecord.setSendTime(new Timestamp(System.currentTimeMillis()));
        edocSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Sent);
        sendEdocManager.update(edocSendRecord);
        Integer systemMessageFilterParam = EdocMessageHelper.getSystemMessageFilterParam(summary).key;
        MessageReceiver receiver = new MessageReceiver(edocSendRecord.getId(), edocSendRecord.getSendUserId());
        MessageContent content = new MessageContent("exchange.sent", edocSendRecord.getSubject(), user.getName());
        if (null != summary && null != summary.getTempleteId()) {
            content.setTemplateId(summary.getTempleteId());
        }
        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.exSend, user.getId(), receiver, systemMessageFilterParam);
    }

    // 签收公文时，对公文进行自动回执。
    private void replyEdoc(long replyId, String content, String recUserName, String recNo, Long agentToId)
            throws Exception {
        EdocSendDetail edocSendDetail = edocSendDetailDao.get(replyId);
        //签收时候，发送记录已经被删除，不需要更新签收信息
        if (edocSendDetail != null) {
            edocSendDetail.setContent(content);
            edocSendDetail.setRecNo(recNo);
            edocSendDetail.setRecUserName(recUserName);
            long l = System.currentTimeMillis();
            edocSendDetail.setRecTime(new Timestamp(l));
            edocSendDetail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_Recieved);
            edocSendDetailDao.update(edocSendDetail);
        }
        User user = AppContext.getCurrentUser();
        if (EdocRoleHelper.isExchangeRole(user.getId())) {
            EdocSendRecord edocSendRecord = this.getEdocSendRecordByDetailId(replyId);
            if (null != edocSendRecord && null != edocSendRecord.getSubject()) {
                MessageReceiver receiver = new MessageReceiver(replyId, edocSendRecord.getSendUserId());
                if (agentToId != null) {
                    String agentToName = "";
                    try {
                        agentToName = orgManager.getMemberById(agentToId).getName();
                    } catch (Exception e) {
                        LOGGER.error("获取代理人姓名错误", e);
                    }
                    userMessageManager.sendSystemMessage(new MessageContent("exchange.signed", edocSendRecord.getSubject(), agentToName).add("edoc.agent.deal", user.getName()),
                            ApplicationCategoryEnum.exSign, agentToId, receiver);
                } else {
                    userMessageManager.sendSystemMessage(new MessageContent("exchange.signed", edocSendRecord.getSubject(), user.getName()), ApplicationCategoryEnum.exSign, user.getId(), receiver);
                }
            }
        }
    }

    public void recEdoc(long id, long recUserId, long registerUserId, String recNo, String remark, String keepPeriod, Long agentToId) throws Exception {
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(id);
        if (edocRecieveRecord == null || (edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Torecieve && edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Receive_Retreat)) {
            return;
        }

        // 签收公文
        edocRecieveRecord.setRemark(remark);
        if (agentToId != null) {
            recUserId = agentToId;
        }
        edocRecieveRecord.setRecUserId(recUserId);
        long l = System.currentTimeMillis();
        edocRecieveRecord.setRecTime(new Timestamp(l));
        edocRecieveRecord.setRegisterUserId(registerUserId);
        edocRecieveRecord.setRecNo(recNo);
        edocRecieveRecord.setKeepPeriod(keepPeriod);
        edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Recieved);
        edocRecieveRecord.setIsRetreat(0);//设置删除的状态为0

        String recUserName = "";
        V3xOrgEntity member = orgManager.getEntity("Member", recUserId);
        if (member != null) {
            recUserName = member.getName();
        }
        edocRecieveRecord.setRecUserId(recUserId);
        recieveEdocManager.update(edocRecieveRecord);

        // 回执公文
        String replyId = edocRecieveRecord.getReplyId();

        // 来自内部交换的公文（同一套系统）
        if (edocRecieveRecord.getFromInternal()) {
            this.replyEdoc(Long.valueOf(replyId), remark, recUserName, recNo, agentToId);
        } else { // 来自外部系统的公文
            EdocSignEvent event = new EdocSignEvent(this);
            EdocSignReceipt esr = new EdocSignReceipt();
            esr.setOpinion(remark);
            esr.setReceipient(recUserName);
            long signTime = System.currentTimeMillis();
            esr.setSignTime(signTime);

            if (EdocRecieveRecord.Exchange_Receive_iAccountType_Dept == edocRecieveRecord.getExchangeType()) {
                try {
                    V3xOrgDepartment dept = orgManager.getDepartmentById(edocRecieveRecord.getExchangeOrgId());
                    esr.setSignUnit(dept.getName());
                } catch (BusinessException e) {
                    LOGGER.error("查找部门异常:", e);
                }
            } else if (EdocRecieveRecord.Exchange_Receive_iAccountType_Org == edocRecieveRecord.getExchangeType()) {
                try {
                    V3xOrgAccount account = orgManager.getAccountById(edocRecieveRecord.getExchangeOrgId());
                    esr.setSignUnit(account.getName());
                } catch (BusinessException e) {
                    LOGGER.error("查找单位异常:", e);
                }
            }
            event.setEdocSignReceipt(esr);
            event.setSendDetailId(Long.valueOf(replyId));
            EventDispatcher.fireEvent(event);
        }

        // 生成个人待办事项（待登记事项）
        // todo:
    }

    public List<EdocSendRecord> getSendEdocs(long userId, long orgId, int status) throws Exception {

        String accountIds = null;
        String depIds = null;
        accountIds = EdocRoleHelper.getUserExchangeAccountIds();
        depIds = EdocRoleHelper.getUserExchangeDepartmentIds();
        return sendEdocManager.findEdocSendRecords(accountIds, depIds, status, null, null);
        //return sendEdocManager.getEdocSendRecords(status);
    }

    public List<EdocSendRecord> getSendEdocs(long userId, long orgId, int status, String condition, String value) throws Exception {

        String accountIds = null;
        String depIds = null;
        //将兼职单位，兼职部门的也查找出来
        accountIds = EdocRoleHelper.getUserExchangeAccountIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        depIds = EdocRoleHelper.getUserExchangeDepartmentIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        List<EdocSendRecord> list = sendEdocManager.findEdocSendRecords(accountIds, depIds, status, condition, value);

        if (list != null)
            for (EdocSendRecord r : list) {
                //存放交接人
                long undertakerId = r.getSendUserId();
                V3xOrgMember member = orgManager.getMemberById(undertakerId);
                if (null != member) {
                    r.setSendUserNames(member.getName());
                } else {
                    r.setSendUserNames("");
                }

                EdocSummary summary = edocSummaryManager.findById(r.getEdocId());
                if (summary != null) {
                    Integer currentNo = r.getContentNo();
                    if (null != currentNo && (currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_FIRST || currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_NORMAL)) {
                        r.setSendNames(summary.getSendTo());
                    } else if (null != currentNo && currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_SECOND) {
                        r.setSendNames(summary.getSendTo2());
                    }
                }
            }

        return list;
        //return sendEdocManager.getEdocSendRecords(status);
    }

    /**
     * 读取当前用户的（待发送或已发送）公文列表。
     *
     * @param userId    当前用户id
     * @param orgId     当前用户登录单位id
     * @param status    状态（待发送或已发送）
     * @param condition 查询条件
     * @param value     查询值
     * @param dateType  时间类型（本年，本月，上个月等）
     * @return List
     */
    public List<EdocSendRecord> getSendEdocs(long userId, long orgId, int status, String condition, String value, int dateType) throws Exception {

        String accountIds = null;
        String depIds = null;
        //将兼职单位，兼职部门的也查找出来
        accountIds = EdocRoleHelper.getUserExchangeAccountIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        depIds = EdocRoleHelper.getUserExchangeDepartmentIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        List<EdocSendRecord> list = sendEdocManager.findEdocSendRecords(accountIds, depIds, status, condition, value, dateType, userId);

        if (list != null)
            for (EdocSendRecord r : list) {
                //存放交接人
                long undertakerId = r.getSendUserId();
                V3xOrgMember member = orgManager.getMemberById(undertakerId);
                if (null != member) {
                    r.setSendUserNames(member.getName());
                } else {
                    r.setSendUserNames("");
                }

                if (Strings.isNotBlank(r.getSendedTypeIds())) {
                    String[] typeAndIds = r.getSendedTypeIds().split(",");
                    StringBuilder sendNames = new StringBuilder("");
                    for (int i = 0; i < typeAndIds.length; i++) {
                        String[] types = typeAndIds[i].split("[|]");
                        String tempName = null;
                        if ("Account".equals(types[0])) {
                            long accountId = Long.parseLong(types[1]);
                            V3xOrgAccount account = orgManager.getAccountById(accountId);
                            if (account != null) {
                                tempName = account.getName();
                            }
                        } else if ("Department".equals(types[0])) {
                            long deptId = Long.parseLong(types[1]);
                            V3xOrgDepartment dept = orgManager.getDepartmentById(deptId);
                            if (dept != null) {
                                tempName = dept.getName();
                            }
                        } else {
                            tempName = types[1];
                        }

                        if (tempName != null) {
                            if (sendNames.length() > 0) {
                                sendNames.append("、");
                            }
                            sendNames.append(tempName);
                        }
                    }
                    r.setSendNames(sendNames.toString());
                } else {
                    EdocSummary summary = edocSummaryManager.findById(r.getEdocId());
                    if (summary != null) {
                        Integer currentNo = r.getContentNo();
                        if (null != currentNo && (currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_FIRST || currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_NORMAL)) {
                            r.setSendNames(summary.getSendTo());
                        } else if (null != currentNo && currentNo.intValue() == Constants.EDOC_EXCHANGE_UNION_SECOND) {
                            r.setSendNames(summary.getSendTo2());
                        }
                    }
                }
            }

        return list;

    }

    /**
     * @param userId
     * @param orgId
     * @param statusSet
     * @param condition
     * @param value
     * @return
     * @throws Exception
     */
    public List<EdocRecieveRecord> getRecieveEdocs(long userId, long orgId, Set<Integer> statusSet, String condition, String[] value) throws Exception {
        return getRecieveEdocs(userId, orgId, statusSet, condition, value, 0, 0, null);
    }

    public List<EdocRecieveRecord> getRecieveEdocs(long userId, long orgId, Set<Integer> statusSet, String condition, String[] value, Map<String, Object> conditionParams) throws Exception {
        //GOV-4908 公文交换-已签收页面报红三角（修改引起）
        if (conditionParams == null) {
            conditionParams = new HashMap<String, Object>();
        }
        conditionParams.put("userId", userId);
        return getRecieveEdocs(userId, orgId, statusSet, condition, value, 0, 0, conditionParams);
    }

    public List<EdocRecieveRecord> getRecieveEdocs(long userId, long orgId, Set<Integer> statusSet, String condition, String[] value, int listType, int dateType, Map<String, Object> conditionParams) throws Exception {
        String accountIds = null;
        String depIds = null;
        //将兼职单位，兼职部门的也查找出来
        int listValue = (conditionParams == null || conditionParams.get("listValue") == null) ? EdocNavigationEnum.LIST_TYPE_EX_RECIEVE : (Integer) conditionParams.get("listValue");//列表类型,签收/登记
        if (listValue != EdocNavigationEnum.LIST_TYPE_REGISTER) {
            accountIds = EdocRoleHelper.getUserExchangeAccountIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
            depIds = EdocRoleHelper.getUserExchangeDepartmentIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        }
        if (conditionParams == null) {
            conditionParams = new HashMap<String, Object>();
            conditionParams.put("userId", userId);
        }
        List<EdocRecieveRecord> list = recieveEdocManager.findEdocRecieveRecords(accountIds, depIds, statusSet, condition, value, listType, dateType, conditionParams);
        if (list != null) {
            for (EdocRecieveRecord r : list) {
                //根据签收记录的签收人ID，查找签收人姓名，用于前台显示
                V3xOrgMember member = orgManager.getMemberById(r.getRecUserId());
                if (null != member) {
                    r.setRecUser(member.getName());
                }
                EdocSummary summary = edocSummaryManager.findById(r.getEdocId());
                r.setCopies(summary.getCopies());
            }
        }
        return list;
    }

    public List<EdocRecieveRecord> getToRegisterEdocs(long userId) {
        return edocRecieveRecordDao.getToRegisterEdocs(userId);
    }

    public void registerEdoc(long id) throws Exception {
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager
                .getEdocRecieveRecord(id);
        if (edocRecieveRecord == null
                || edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Recieved) {
            return;
        }
        edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
        recieveEdocManager.update(edocRecieveRecord);
    }

    public EdocSendRecord getSendRecordById(long id) {
        return sendEdocManager.getEdocSendRecord(id);
    }

    public EdocRecieveRecord getReceivedRecord(long id) {

        EdocRecieveRecord ret = recieveEdocManager.getEdocRecieveRecord(id);
        if (null == ret) {
            return ret;
        }
        EdocSummary s = edocSummaryManager.findById(ret.getEdocId());

        ret.setCopies(s.getCopies());

        return ret;

    }

    public EdocRecieveRecord getReceivedRecordByEdocId(long edocId) {

        EdocRecieveRecord ret = recieveEdocManager.getEdocRecieveRecordByEdocId(edocId);
        return ret;

    }

    public void deleteByType(String id, String type) throws Exception {
        String[] ids = id.split(",");
        if (null != type && "send".equals(type)) {//已发送下删除
            for (int i = 0; i < ids.length; i++) {
                sendEdocManager.delete(Long.valueOf(ids[i]), EdocSendRecord.Exchange_iStatus_Send_Delete);
            }
        } else if (null != type && "presend".equals(type)) {//待发送下删除
            for (int i = 0; i < ids.length; i++) {
                sendEdocManager.delete(Long.valueOf(ids[i]), EdocSendRecord.Exchange_iStatus_ToSend_Delete);
            }
        } else {//已签收下删除
            for (int i = 0; i < ids.length; i++) {
                recieveEdocManager.delete(Long.valueOf(ids[i]));
            }
        }
    }

    public AffairManager getAffairManager() {
        return affairManager;
    }

    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    public EdocSummaryManager getEdocSummaryManager() {
        return edocSummaryManager;
    }

    public void setEdocSummaryManager(EdocSummaryManager edocSummaryManager) {
        this.edocSummaryManager = edocSummaryManager;
    }

    public UserMessageManager getUserMessageManager() {
        return userMessageManager;
    }

    public void setUserMessageManager(UserMessageManager userMessageManager) {
        this.userMessageManager = userMessageManager;
    }

    public List<EdocSendDetail> createSendRecord(Long sendRecordId, String typeAndIds) throws ExchangeException {
        return sendEdocManager.createSendRecord(sendRecordId, typeAndIds);
    }

    /**
     * Ajax判断某个公文收发员是否有待交换和待签收的Affair事项。
     *
     * @param userId :用户ID
     * @return
     */
    public String checkEdocExchangeHasPendingAffair(Long userId) {
        List<ApplicationCategoryEnum> appEnums = new ArrayList<ApplicationCategoryEnum>();
        List<StateEnum> statesEnums = new ArrayList<StateEnum>();
        appEnums.add(ApplicationCategoryEnum.exSend);
        appEnums.add(ApplicationCategoryEnum.exSign);
        statesEnums.add(StateEnum.edoc_exchange_send);
        statesEnums.add(StateEnum.edoc_exchange_receive);
        //以下有编译错误，重新修改-杨帆
//		int countEdocSend=affairManager.getCountAffairsByAppsAndStatesAndMemberId(appEnums,statesEnums,userId);
//		if(countEdocSend>0) return "1";
//		  else
        return "0";
    }

    public OperationlogManager getOperationlogManager() {
        return operationlogManager;
    }

    public void setOperationlogManager(OperationlogManager operationlogManager) {
        this.operationlogManager = operationlogManager;
    }

    public EdocObjTeamManager getEdocObjTeamManager() {
        return edocObjTeamManager;
    }

    public void setEdocObjTeamManager(EdocObjTeamManager edocObjTeamManager) {
        this.edocObjTeamManager = edocObjTeamManager;
    }


    public boolean changeDistributeEdocPerson(String registerId,
                                              String newDistributeUserId, String newDistributeUserName,
                                              String changeOperUserName, String changeOperUserID) throws Exception {
        if (!EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(), Long.parseLong(newDistributeUserId),
                EdocEnum.edocType.distributeEdoc.ordinal())) {
            return false;
        }
        EdocRegister register = edocRegisterManager.findRegisterById(Long.parseLong(registerId));
        // 旧的分发人，用来发送消息
        Long oldDistributeUserId = register.getDistributerId();
        // 结束旧的分发人的事项
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("app", ApplicationCategoryEnum.edocRecDistribute.key());
        conditions.put("objectId", register.getId());//待分发affair数据objectId是登记id
        conditions.put("subObjectId", register.getDistributeEdocId());
        conditions.put("memberId", oldDistributeUserId);
        conditions.put("finish", false);
        conditions.put("delete", false);

        List<CtpAffair> affairList = affairManager.getByConditions(null, conditions);
        if (null != affairList && affairList.size() > 0) {
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            // 分发，只有一个分发人，所以第一位就是旧登记人
            CtpAffair af = affairList.get(0);
            if (oldDistributeUserId.equals(af.getMemberId()) && !af.isDelete()) {
                Long senderId = af.getSenderId();
                receivers.add(new MessageReceiver(af.getId(), af.getMemberId()));
                af.setFinish(true);
                af.setDelete(true);
                affairManager.updateAffair(af);
                // 给旧的登记人发送消息
                if (null != receivers && receivers.size() > 0) {
                    MessageContent messageContent = new MessageContent("exchange.changeDistributer", af.getSubject(), newDistributeUserName, af.getApp());
                    messageContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
                    if (null != af.getTempleteId()) {
                        messageContent.setTemplateId(af.getTempleteId());
                    }
                    userMessageManager.sendSystemMessage(messageContent,
                            ApplicationCategoryEnum.edocRec, Long
                                    .parseLong(changeOperUserID),
                            receivers, EdocMessageFilterParamEnum.exchange.key);
                }

                // 设置新的分发人
                register.setDistributerId(Long.parseLong(newDistributeUserId));
                register.setDistributer(newDistributeUserName);
                edocRegisterManager.update(register);

                // 为新的分发人生成事项
                CtpAffair reAffair = new CtpAffair();
                reAffair.setIdIfNew();
                reAffair.setApp(ApplicationCategoryEnum.edocRecDistribute.getKey());
                reAffair.setSubject(register.getSubject());
                reAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
                reAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
                reAffair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
                reAffair.setMemberId(register.getDistributerId());
                reAffair.setObjectId(register.getId());
                reAffair.setSubObjectId(register.getDistributeEdocId());
                reAffair.setSenderId(senderId);
                reAffair.setState(StateEnum.col_pending.getKey());
                reAffair.setSubState(SubStateEnum.col_normal.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来
                reAffair.setTrack(null);

                if (register.getUrgentLevel() != null && !"".equals(register.getUrgentLevel())) {

                    reAffair.setImportantLevel(Integer.parseInt(register.getUrgentLevel()));
                }
                //首页栏目的扩展字段设置--公文文号、发文单位等--start
                EdocSummary summary = edocSummaryManager.findById(register.getEdocId());
                Map<String, Object> extParam = EdocUtil.createExtParam(register.getDocMark(), register.getSendUnit(), summary.getSendUnitId());
                if (null != extParam) AffairUtil.setExtProperty(reAffair, extParam);
                //首页栏目的扩展字段设置--公文文号、发文单位等--end
                affairManager.save(reAffair);

                /*
                 * 发消息给待登记人
                 */
                String url = "message.link.exchange.distribute";
                String key = "exchange.changeDistributer.distribute";
                MessageReceiver receiver = new MessageReceiver(
                        reAffair.getId(),
                        reAffair.getMemberId(),
                        url,
                        com.seeyon.ctp.common.usermessage.Constants.LinkOpenType.href,
                        String.valueOf(EdocEnum.edocType.recEdoc.ordinal()), String.valueOf(register.getId()), ""
                );

                MessageContent messageContent = new MessageContent(key, reAffair.getSubject(), changeOperUserName);
                messageContent.setResource("com.seeyon.v3x.edoc.resources.i18n.EdocResource");
                if (null != reAffair.getTempleteId()) {
                    messageContent.setTemplateId(reAffair.getTempleteId());
                }
                userMessageManager.sendSystemMessage(messageContent,
                        ApplicationCategoryEnum.edocRegister, Long
                                .parseLong(changeOperUserID), receiver, EdocMessageFilterParamEnum.exchange.key);

            }
        }

        return true;
    }


    public boolean changeRegisterEdocPerson(String edocRecieveRecordId,
                                            String newRegisterUserId, String newRegisterUserName,
                                            String changeOperUserName, String changeOperUserID)
            throws Exception {
        //GOV-5030 【公文管理】-【收文管理】-【签收-已签收】列表对未登记数据进行变更登记人操作时没有对收文登记权限进行判断 start
        //获取具有收文登记权的人员
        if (!EdocRoleHelper.isEdocCreateRole(AppContext.getCurrentUser().getLoginAccount(), Long.parseLong(newRegisterUserId),
                EdocEnum.edocType.recEdoc.ordinal())) {
            return false;
        }
        //GOV-5030 【公文管理】-【收文管理】-【签收-已签收】列表对未登记数据进行变更登记人操作时没有对收文登记权限进行判断 end

        EdocRecieveRecord edocRecieveRecord = recieveEdocManager
                .getEdocRecieveRecord(Long.parseLong(edocRecieveRecordId));
        EdocRegister register = edocRegisterManager.findRegisterByRecieveId(Long.parseLong(edocRecieveRecordId));
        if (register != null) {
            //更改登记人后，登记数据的Distributer也要一起修改
            register.setDistributerId(Long.parseLong(newRegisterUserId));
            register.setDistributer(newRegisterUserName);
            edocRegisterManager.update(register);
        }

        // 旧的登记人，用来发送消息
        Long oldRegisterUserId = edocRecieveRecord.getRegisterUserId();
        // 结束旧的登记人的事项
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("app", ApplicationCategoryEnum.edocRegister.key());
        conditions.put("objectId", edocRecieveRecord.getEdocId());
        conditions.put("subObjectId", edocRecieveRecord.getId());
        conditions.put("finish", false);
        conditions.put("delete", false);
        conditions.put("state", StateEnum.col_pending.key());
        List<CtpAffair> affairList = affairManager.getByConditions(null, conditions);
        if (null != affairList && affairList.size() > 0) {
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            String subject = "";
            int app = 0;
            Long senderId = 0L;
            //当待分发退回后，登记就可能是竞争执行了
            for (CtpAffair af : affairList) {
                if ((oldRegisterUserId.equals(af.getMemberId()) || oldRegisterUserId.longValue() == 0) && !af.isDelete()) {
                    subject = af.getSubject();
                    app = af.getApp();
                    senderId = af.getSenderId();
                    receivers.add(new MessageReceiver(af.getId(), af.getMemberId()));
                    af.setFinish(true);
                    af.setDelete(true);
                    affairManager.updateAffair(af);
                }
            }
            // 给旧的登记人发送消息
            if (null != receivers && receivers.size() > 0) {
                MessageContent content = new MessageContent(
                        "exchange.changeRegister", subject,
                        newRegisterUserName, app);
                if (null != affairList && affairList.size() > 0 && null != affairList.get(0).getTempleteId()) {
                    content.setTemplateId(affairList.get(0).getTempleteId());
                }
                userMessageManager.sendSystemMessage(content,
                        ApplicationCategoryEnum.edocRec, Long
                                .parseLong(changeOperUserID),
                        receivers, EdocMessageFilterParamEnum.exchange.key);
            }

            // 设置新的登记人
            edocRecieveRecord.setRegisterUserId(Long.parseLong(newRegisterUserId));
            edocRecieveRecord.setRegisterName(newRegisterUserName);
            recieveEdocManager.update(edocRecieveRecord);
            // 为新的登记人生成事项
            CtpAffair reAffair = new CtpAffair();
            reAffair.setIdIfNew();
            reAffair.setApp(ApplicationCategoryEnum.edocRegister.getKey());
            reAffair.setSubject(edocRecieveRecord.getSubject());
            reAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
            reAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
            reAffair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
            reAffair.setMemberId(edocRecieveRecord.getRegisterUserId());
            reAffair.setObjectId(edocRecieveRecord.getEdocId());
            reAffair.setSubObjectId(edocRecieveRecord.getId());
            reAffair.setSenderId(senderId);
            reAffair.setState(StateEnum.edoc_exchange_register.getKey());
            reAffair.setSubState(SubStateEnum.col_normal.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来
            reAffair.setTrack(null);
            if (edocRecieveRecord.getUrgentLevel() != null && !"".equals(edocRecieveRecord.getUrgentLevel())) {

                reAffair.setImportantLevel(Integer.parseInt(edocRecieveRecord.getUrgentLevel()));
            }
            //首页栏目的扩展字段设置--公文文号、发文单位等--start
            EdocSummary summary = edocSummaryManager.findById(edocRecieveRecord.getEdocId());
            Map<String, Object> extParam = EdocUtil.createExtParam(edocRecieveRecord.getDocMark(), edocRecieveRecord.getSendUnit(), summary.getSendUnitId());
            if (null != extParam) AffairUtil.setExtProperty(reAffair, extParam);
            //首页栏目的扩展字段设置--公文文号、发文单位等--end
            affairManager.save(reAffair);

            String url = "";
            List<String> messageParam = new ArrayList<String>();
            if (EdocHelper.isG6Version()) {
                url = "message.link.exchange.register.govpending";
                messageParam.add(reAffair.getSubObjectId().toString());
                messageParam.add(reAffair.getObjectId().toString());
                messageParam.add(String.valueOf(summary.getOrgAccountId()));
            } else {
                url = "message.link.exchange.register.pending";
                messageParam.add(String.valueOf(EdocEnum.edocType.recEdoc.ordinal()));
                messageParam.add(reAffair.getSubObjectId().toString());
                messageParam.add(reAffair.getObjectId().toString());
            }


            /*
             * 发消息给待登记人
             */
            String key = "exchange.changeRegister.register";
            MessageReceiver receiver = new MessageReceiver(
                    reAffair.getId(),
                    reAffair.getMemberId(),
                    url,
                    com.seeyon.ctp.common.usermessage.Constants.LinkOpenType.href,
                    messageParam.get(0), messageParam.get(1), messageParam.get(2));
            MessageContent content = new MessageContent(key,
                    reAffair.getSubject(), changeOperUserName);
            if (null != reAffair.getTempleteId()) {
                content.setTemplateId(reAffair.getTempleteId());
            }
            userMessageManager.sendSystemMessage(content,
                    ApplicationCategoryEnum.edocRegister, Long
                            .parseLong(changeOperUserID), receiver, EdocMessageFilterParamEnum.exchange.key);
        }
        return true;
    }

    /**
     * ajax调用：是否已经被登记
     */
    public boolean isBeRegistered(String edocRecieveRecordId) {
        boolean isCanBeRegisted = true;
        EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(edocRecieveRecordId));
        if (record.getStatus() == EdocRecieveRecord.Exchange_iStatus_Registered) {// 公文已经登记
            isCanBeRegisted = false;
        }
        return isCanBeRegisted;
    }

    /**
     * ajax调用：是否已经被分发
     */
    public boolean isBeDistributed(String registerId) {
        boolean isCanBeDistributed = true;
        EdocRegister register = edocRegisterManager.findRegisterById(Long.parseLong(registerId));
        if (register.getDistributeEdocId() != -1) {//公文已经被分发
            isCanBeDistributed = false;
        }
        return isCanBeDistributed;
    }

    /**
     * ajax调用：登记开关由打开变为关闭时，需要判断该单位是否有待登记数据
     */
    public boolean isHaveWaitRegisters() {
        Long accountId = AppContext.getCurrentUser().getLoginAccount();
        boolean flag = false;
        int waitToRegisterDataCount = recieveEdocManager.findEdocRecieveRecordCountByStatusAndAccountId(1, accountId);
        if (waitToRegisterDataCount > 0) {
            flag = true;
        } else {
            //如果没有待登记的数据，还需要判断是否有登记待发的数据
            int count = edocRegisterManager.findWaitRegisterCountByAccountId(accountId);
            if (count > 0) {
                flag = true;
            }
        }
        return flag;
    }


    private void membersToMessageReceiver(List<V3xOrgMember> members, long referenceAffairId, List<MessageReceiver> receivers) {
        if (members != null && members.size() > 0) {
            for (V3xOrgMember m : members) {
                MessageReceiver receiver = new MessageReceiver(referenceAffairId, m.getId());
                receivers.add(receiver);
            }
        }
    }


    /**
     * 登记退件箱/待登记退回到签收退件箱
     *
     * @param id
     * @param referenceAffairId
     * @param stepBackInfo
     * @throws Exception
     */
    public void stepBackRegisterEdoc(EdocRegister edocRegister, long id, long referenceAffairId, String stepBackInfo, String competitionAction) throws Exception {
        // 回退时，不区分内部外部系统公文，在待签收回退的时候，加入防护，当edocRecieveRecord.getFromInternal()为true时，内部公文才允许回退
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(id);
        //这个还要判断状态是3的也不能return，因为3的代表从分发退回的数据
        if (edocRecieveRecord == null ||
                (edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Recieved
                        && edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Receive_StepBacked
                        && edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Registered
                        && edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Receive_Delete////已签收中被删除了
                        && edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_RegisterToWaitSend)) {//保存待发

            return;
        }

        User user = AppContext.getCurrentUser();
        edocRecieveRecord.setIsRetreat(EdocRecieveRecord.Receive_Retreat_Yes);
        //5.0 签收页签没有退件箱了，从待登记退回后就到 待签收列表了
        edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Receive_Retreat);
        edocRecieveRecord.setStepBackInfo(stepBackInfo);
        if (!"no".equals(competitionAction)) {
            edocRecieveRecord.setRecUserId(0);
        }


        EdocSendDetail detail = sendEdocManager.getSendRecordDetail(Long.parseLong(edocRecieveRecord.getReplyId()));
        EdocSendRecord edocSendRecord = sendEdocManager.getEdocSendRecord(detail.getSendRecordId());

        if (detail != null) {
            detail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_Torecieve);
            sendEdocManager.updateSendRecordDetail(detail);
        }

        List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
        List<MessageReceiver> agentReceivers = new ArrayList<MessageReceiver>();
        if (!"no".equals(competitionAction)) {
            if (edocRecieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Org) {
                List<V3xOrgMember> members = EdocRoleHelper.getAccountExchangeUsers(edocRecieveRecord.getExchangeOrgId());
                membersToMessageReceiver(members, referenceAffairId, receivers);
            } else {
                V3xOrgDepartment dep = orgManager.getDepartmentById(edocRecieveRecord.getExchangeOrgId());
                List<V3xOrgMember> members = EdocRoleHelper.getDepartMentExchangeUsers(dep.getOrgAccountId(), dep.getId());
                membersToMessageReceiver(members, referenceAffairId, receivers);
            }
        } else {
            //发出消息，被回退（只发给签收人）
            MessageReceiver receiver = new MessageReceiver(referenceAffairId, edocRecieveRecord.getRecUserId());
            receivers.add(receiver);
        }
        if (receivers != null && receivers.size() > 0) {
            for (MessageReceiver r : receivers) {
                Long agentMemberId = MemberAgentBean.getInstance().getAgentMemberId(ApplicationCategoryEnum.edoc.key(), r.getReceiverId());
                if (agentMemberId != null) {
                    MessageReceiver receiver1 = new MessageReceiver(r.getReferenceId(), agentMemberId);
                    agentReceivers.add(receiver1);
                }
            }
        }
        ApplicationCategoryEnum msgType = ApplicationCategoryEnum.edocRegister;
        //G6版本 当登记数据是自动登记时，待分发回退，消息类型记录为待分发
        if (EdocHelper.isG6Version() && edocRegister != null && Integer.valueOf(EdocRegister.AUTO_REGISTER).equals(edocRegister.getAutoRegister())) {
            msgType = ApplicationCategoryEnum.edocRecDistribute;
        }

        // 是否需要回退信息
        userMessageManager.sendSystemMessage(new MessageContent("exchange.stepback", edocRecieveRecord.getSubject(), user.getName(), stepBackInfo), msgType, user.getId(), receivers, EdocMessageFilterParamEnum.exchange.key);

        //如果此签收人有代理人的话，要给代理人发消息
        if (agentReceivers != null && agentReceivers.size() > 0) {
            userMessageManager.sendSystemMessage(new MessageContent("exchange.stepback", edocRecieveRecord.getSubject(), user.getName(),
                    stepBackInfo).add("col.agent"), msgType, user.getId(), agentReceivers, EdocMessageFilterParamEnum.exchange.key);
        }


        Map<String, Object> columns = new Hashtable<String, Object>();
        columns.put("delete", true);
        affairManager.update(columns, new Object[][]{
                {"app", ApplicationCategoryEnum.edocRegister.getKey()},
                {"objectId", edocRecieveRecord.getEdocId()},
                {"subObjectId", edocRecieveRecord.getId()}});

        //将已登记数据删除
        if (edocRegister != null) {
            //标识收文登记退回到收文签收
//			edocRegisterManager.updateEdocRegisterState(edocRegister.getId(), EdocNavigationEnum.RegisterState.Register_StepBacked.ordinal());

            edocRegisterManager.deleteEdocRegister(edocRegister);
        }
        recieveEdocManager.update(edocRecieveRecord);


        //设置签收人退件箱中待签收数据
        if ("no".equals(competitionAction)) {
            CtpAffair affair = new CtpAffair();
            affair.setIdIfNew();
            affair.setApp(ApplicationCategoryEnum.exSign.getKey());
            affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
            affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
            affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
            affair.setSubject(edocRecieveRecord.getSubject());
            affair.setMemberId(edocRecieveRecord.getRecUserId());

            if (Strings.isNotBlank(edocRecieveRecord.getUrgentLevel())) {
                affair.setImportantLevel(Integer.parseInt(edocRecieveRecord.getUrgentLevel()));
            }

            affair.setFinish(false);
            affair.setObjectId(edocRecieveRecord.getEdocId());
            affair.setSubObjectId(edocRecieveRecord.getId());
            affair.setSenderId(edocSendRecord.getSendUserId());
            affair.setState(StateEnum.edoc_exchange_receive.key());
            affair.setSubState(SubStateEnum.col_normal.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来
            //首页栏目的扩展字段设置--公文文号、发文单位等--start
            Map<String, Object> extParam = new HashMap<String, Object>();
            extParam.put(AffairExtPropEnums.edoc_edocMark.name(), edocRecieveRecord.getDocMark()); //公文文号
            extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), edocRecieveRecord.getSendUnit());//发文单位
            extParam.put(AffairExtPropEnums.edoc_edocExSendRetreat.name(), edocRecieveRecord.getRegisterUserId());//G6原来set到此字段的值,一起加
            AffairUtil.setExtProperty(affair, extParam);
            //首页栏目的扩展字段设置--公文文号、发文单位等--end
            affairManager.save(affair);

        } else {
            List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
            if (edocRecieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Dept) {//部门交换
                memberList = orgManager.getMembersByRole(edocRecieveRecord.getExchangeOrgId(), OrgConstants.Role_NAME.Departmentexchange.name());
            } else if (edocRecieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Org) {//单位交换
                memberList = orgManager.getMembersByRole(edocRecieveRecord.getExchangeOrgId(), OrgConstants.Role_NAME.Accountexchange.name());
            }
            if (Strings.isNotEmpty(memberList)) {
                List<CtpAffair> affairList = new ArrayList<CtpAffair>();
                for (V3xOrgMember v3xOrgMember : memberList) {
                    CtpAffair affair = new CtpAffair();
                    affair.setIdIfNew();
                    affair.setApp(ApplicationCategoryEnum.exSign.getKey());
                    affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
                    affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
                    affair.setSubject(edocRecieveRecord.getSubject());
                    affair.setMemberId(v3xOrgMember.getId());

                    if (Strings.isNotBlank(edocRecieveRecord.getUrgentLevel())) {
                        affair.setImportantLevel(Integer.parseInt(edocRecieveRecord.getUrgentLevel()));
                    }
                    affair.setFinish(false);
                    affair.setObjectId(edocRecieveRecord.getEdocId());
                    affair.setSubObjectId(edocRecieveRecord.getId());
                    affair.setSenderId(edocSendRecord.getSendUserId());
                    affair.setState(StateEnum.edoc_exchange_receive.key());
                    affair.setSubState(SubStateEnum.col_normal.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来
                    //首页栏目的扩展字段设置--公文文号、发文单位等--start
                    Map<String, Object> extParam = new HashMap<String, Object>();
                    extParam.put(AffairExtPropEnums.edoc_edocMark.name(), edocRecieveRecord.getDocMark()); //公文文号
                    extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), edocRecieveRecord.getSendUnit());//发文单位
                    extParam.put(AffairExtPropEnums.edoc_edocExSendRetreat.name(), edocRecieveRecord.getRegisterUserId());//G6原来set到此字段的值,一起加
                    AffairUtil.setExtProperty(affair, extParam);
                    //首页栏目的扩展字段设置--公文文号、发文单位等--end
                    affairList.add(affair);
                }
                if (Strings.isNotEmpty(affairList)) {
                    affairManager.saveAffairs(affairList);
                }
            }

        }
    }

    /**
     * 待登记退回到待签收
     *
     * @param id
     * @param referenceAffairId
     * @param stepBackInfo
     * @throws Exception
     */
    public void stepBackRecievedEdoc(long id, long referenceAffairId, String stepBackInfo) throws Exception {
        // 回退时，不区分内部外部系统公文，在待签收回退的时候，加入防护，当edocRecieveRecord.getFromInternal()为true时，内部公文才允许回退
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(id);

        //这个还要判断状态是3的也不能return，因为3的代表从分发退回的数据
        if (edocRecieveRecord == null
                || (edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Recieved
                && edocRecieveRecord.getStatus() != EdocRecieveRecord.Exchange_iStatus_Receive_StepBacked)) {
            return;
        }

        User user = AppContext.getCurrentUser();
        String senderName = edocRecieveRecord.getSender();
        // 1恢复exchange_recieve_edoc表中的记录，status为0 待签收
        edocRecieveRecord.setRemark(null);
        // 签收人
        // edocRecieveRecord.setRecUserId();
        edocRecieveRecord.setRecTime(null);
        // 登记人，因为是回退，这里记录的是回退用户ID
        // edocRecieveRecord.setRegisterUserId(stepBackUserId);
        edocRecieveRecord.setRecNo(null);
        edocRecieveRecord.setKeepPeriod(null);
        //把待登记退回到代签收的isRetreat设置为1
        edocRecieveRecord.setIsRetreat(EdocRecieveRecord.Receive_Retreat_Yes);
        edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Receive_Retreat);
        edocRecieveRecord.setStepBackInfo(stepBackInfo);
        recieveEdocManager.update(edocRecieveRecord);

        // 2恢复exchange_send_edoc_detail表中的对应记录，status为0 待签收
        String replyId = edocRecieveRecord.getReplyId();
        EdocSendDetail edocSendDetail = edocSendDetailDao.get(Long.valueOf(replyId));
        if (edocSendDetail != null) {
            edocSendDetail.setContent(null);
            edocSendDetail.setRecNo(null);
            edocSendDetail.setRecUserName(null);
            edocSendDetail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_Torecieve);
            edocSendDetailDao.update(edocSendDetail);
        }
        // 3结束已经发送的affair（待登记）
        // 批量更新
        Map<String, Object> columns = new Hashtable<String, Object>();
        columns.put("isDelete", true);
        affairManager.update(columns, new Object[][]{
                {"app", ApplicationCategoryEnum.edocRegister.getKey()},
                {"state", StateEnum.edoc_exchange_register.key()},
                {"objectId", edocRecieveRecord.getEdocId()}});

        // 4发出消息，被回退（只发给签收人）
        MessageReceiver receiver = new MessageReceiver(referenceAffairId, edocRecieveRecord.getRecUserId());
        // 是否需要回退信息？
        userMessageManager.sendSystemMessage(new MessageContent("exchange.stepback", edocRecieveRecord.getSubject(), user.getName(), stepBackInfo), ApplicationCategoryEnum.edocRegister, user.getId(), receiver, EdocMessageFilterParamEnum.exchange.key);

        // 5增加操作人当前单位的公文收发员的待签收的affair
        String key = "exchange.sign";
        List<V3xOrgMember> member = null;
        if (edocSendDetail.getRecOrgType() == 1) {
            // 内部单位
            member = EdocRoleHelper.getAccountExchangeUsers(user.getLoginAccount());
        } else if (edocSendDetail.getRecOrgType() == 2) {
            // 内部部门
            member = EdocRoleHelper.getDepartMentExchangeUsers(user.getLoginAccount(), Long.valueOf(user.getLoginAccount()));
        }
        CtpAffair affair = null;
        for (V3xOrgMember m : member) {
            affair = new CtpAffair();
            affair.setIdIfNew();
            affair.setApp(ApplicationCategoryEnum.exSign.getKey());
            affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
            affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
            affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
            affair.setSubject(edocRecieveRecord.getSubject());
            affair.setMemberId(m.getId());
            affair.setFinish(false);
            affair.setObjectId(edocRecieveRecord.getEdocId());
            affair.setSubObjectId(edocRecieveRecord.getId());
            affair.setSenderId(user.getId());
            affair.setState(StateEnum.edoc_exchange_receive.key());
            affair.setSubState(SubStateEnum.col_normal.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来
            //首页栏目的扩展字段设置--公文文号、发文单位等--start
            Map<String, Object> extParam = new HashMap<String, Object>();
            extParam.put(AffairExtPropEnums.edoc_edocMark.name(), edocRecieveRecord.getDocMark()); //公文文号
            extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), edocRecieveRecord.getSendUnit());//发文单位
            AffairUtil.setExtProperty(affair, extParam);
            //首页栏目的扩展字段设置--公文文号、发文单位等--end

            affairManager.save(affair);
            MessageReceiver receiver_a = new MessageReceiver(affair.getId(), affair.getMemberId(), "message.link.exchange.receive", affair.getSubObjectId().toString());
            userMessageManager.sendSystemMessage(new MessageContent(key, affair.getSubject(), senderName), ApplicationCategoryEnum.exSign, user.getId(), receiver_a, EdocMessageFilterParamEnum.exchange.key);
        }
    }

    //已交换出去的公文取回
    public void takeBackEdoc(Long edocId,
                             Long currentUserId, String currentUserName,
                             EdocSummary stepBackEdocSummary) throws Exception {
        // 取得待 取回的公文对象（待签收公文）
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager
                .getEdocRecieveRecordByEdocId(edocId);

        // 对此待签收公文进行回执
        Long replyId = Long.parseLong(edocRecieveRecord.getReplyId());
        EdocSendDetail edocSendDetail = edocSendDetailDao.get(replyId);
        // 取回步骤1更新待取回公文的detail
        if (edocSendDetail != null) {
            edocSendDetail.setRecUserName(currentUserName);
            long l = System.currentTimeMillis();
            edocSendDetail.setRecTime(new Timestamp(l));
            edocSendDetail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_Torecieve);
            edocSendDetailDao.update(edocSendDetail);
        }

        // 取回步骤2，删除此待签收公文
        edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Receive_StepBacked);
        recieveEdocManager.delete(edocRecieveRecord);

        // 取得待取回公文的recode
        EdocSendRecord edocSendRecord = sendEdocManager.getEdocSendRecord(edocSendDetail.getSendRecordId());
        // 取回步骤3更新取回公文的record中状态
        edocSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Tosend);
        sendEdocManager.update(edocSendRecord);

        // 回退步骤4 给公文的发文单位的公文交换员发消息
        CtpAffair affair = null;
        affair = new CtpAffair();
        affair.setIdIfNew();
        affair.setApp(ApplicationCategoryEnum.exSend.getKey());
        affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
        affair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
        affair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
        affair.setSubject(stepBackEdocSummary.getSubject());
        affair.setMemberId(edocSendRecord.getSendUserId());
        affair.setFinish(false);
        affair.setObjectId(stepBackEdocSummary.getId());
        affair.setSubObjectId(edocSendRecord.getId());
        affair.setSenderId(currentUserId);
        affair.setState(StateEnum.edoc_exchange_send.key());
        affair.setSubState(SubStateEnum.col_normal.key());//OA-39223待办栏目统计图，办理状态图，待登记的被统计在待办中，但是通过点击图，却筛选不出来。。
        //首页栏目的扩展字段设置--公文文号、发文单位等--start
        Map<String, Object> extParam = new HashMap<String, Object>();
        extParam.put(AffairExtPropEnums.edoc_edocMark.name(), stepBackEdocSummary.getDocMark()); //公文文号
        extParam.put(AffairExtPropEnums.edoc_sendUnit.name(), stepBackEdocSummary.getSendUnit());//发文单位
        AffairUtil.setExtProperty(affair, extParam);
        //首页栏目的扩展字段设置--公文文号、发文单位等--end
        affairManager.save(affair);

    }

    @Override
    public boolean exchangeEdocCancelById(Long exchangeRecieveEdocId) {
        EdocRecieveRecord edocRecieveRecord = this.getReceivedRecord(exchangeRecieveEdocId);
        if (null == edocRecieveRecord) {
            return true;
        }
        return false;
    }

    /*
     * G6 V1.0 SP1后续功能_签收时自动登记功能
     * @see com.seeyon.v3x.exchange.manager.EdocExchangeManager#getDistributeUser(ConfigManager, java.lang.Long, java.lang.Long)
     */
    @Override
    public Long getDistributeUser(ConfigManager configManager, Long userId, Long accountId) {
        try {
            String edocRecDistributeCreates = "";
            String[] edocRecDistributeCreatesArray = null;
            String edocRecDistributeCreatesFirst = null;
            if (EdocRoleHelper.isEdocCreateRole(accountId, userId, EdocEnum.edocType.distributeEdoc.ordinal())) {
                return userId;
            }
            ConfigItem edocRecDistributeItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC_DISTRIBUTE, accountId);
            if (null == edocRecDistributeItem || null == edocRecDistributeItem.getExtConfigValue() || "".equals(edocRecDistributeItem.getExtConfigValue())) {
                return null;
            }
            edocRecDistributeCreates = edocRecDistributeItem.getExtConfigValue();
            edocRecDistributeCreatesArray = edocRecDistributeCreates.split(",");
            edocRecDistributeCreatesFirst = edocRecDistributeCreatesArray[0];//取得第一个
            String orgType = edocRecDistributeCreatesFirst.split("\\|")[0];//第一个组织的类型（Member|Department|Post|Level）
            Long id = Long.parseLong(edocRecDistributeCreatesFirst.split("\\|")[1]);//第一个组织的ID
            if ("Member".equals(orgType)) {//收文分发权限的第一个是'人员'，不是岗位或职务，就直接返回第一个人
                return Long.parseLong(edocRecDistributeCreatesFirst.split("\\|")[1]);
            } else {
                for (String str : edocRecDistributeCreatesArray) {
                    List<V3xOrgMember> list = null;
                    orgType = str.split("\\|")[0];
                    id = Long.parseLong(str.split("\\|")[1]);
                    Long memberId = null;
                    if ("Department".equals(orgType)) {
                        list = this.orgManager.getMembersByDepartment(id, false);
                    } else if ("Post".equals(orgType)) {
                        list = this.orgManager.getMembersByPost(id);
                    } else if ("Level".equals(orgType)) {
                        list = this.orgManager.getMembersByLevel(id);
                    } else if ("Member".equals(orgType)) {
                        memberId = id;
                    }
                    if (list != null && list.size() > 0)
                        return list.get(0).getId();
                    else if (null != memberId)
                        return memberId;
                    else
                        continue;
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.error("", e);
        } catch (BusinessException e) {
            LOGGER.error("", e);
        }
        return null;
    }

    /*
     * G6 V1.0 SP1后续功能_签收时自动登记功能
     * @see com.seeyon.v3x.exchange.manager.EdocExchangeManager#hasDistributeUserByAccountId(java.lang.Long)
     */
    @Override
    public boolean hasDistributeUserByAccountId(Long accountId) {
        if (null != accountId) {
            ConfigItem edocRecDistributeItem = configManager.getConfigItem(IConfigPublicKey.EDOC_CREATE_KEY, IConfigPublicKey.EDOC_CREATE_ITEM_KEY_REC_DISTRIBUTE, accountId);
            if (null == edocRecDistributeItem || null == edocRecDistributeItem.getExtConfigValue() || "".equals(edocRecDistributeItem.getExtConfigValue())) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查如果不是自己单位建的签收编号，如果不是的话是没有修改权限的
     *
     * @param edocRecieveRecordId
     * @return
     */
    public String isEditEdocMark(String markId, String accountId) {
        return edocSendDetailDao.isEditEdocMark(markId, accountId);
    }

    /**
     * 查询本单位的签收编号
     */
    public List<EdocMarkDefinition> getMarkList(Long accountId, Long depId) throws BusinessException {
        List<EdocMarkDefinition> list = new ArrayList<EdocMarkDefinition>();
        User user = CurrentUser.get();
        List<Long> deptIdList = new ArrayList<Long>();
        deptIdList.add(user.getAccountId());//本单位
        deptIdList.add(user.getDepartmentId());//本部门

        List<V3xOrgDepartment> allParentDepartment = orgManager.getAllParentDepartments(user.getDepartmentId());
        if (Strings.isNotEmpty(allParentDepartment)) {
            for (V3xOrgDepartment dept : allParentDepartment) {
                deptIdList.add(dept.getId());
            }
        }

        //兼职单位
		/*if(user.getAccountId().longValue() != user.getLoginAccount().longValue()) {
			deptIdList.add(user.getLoginAccount());
		}*/
        List<V3xOrgAccount> concurrentAccountList = orgManager.concurrentAccount(user.getId());
        for (V3xOrgAccount concurrentAccount : concurrentAccountList) {
            if (!deptIdList.contains(concurrentAccount.getId())) {
                deptIdList.add(concurrentAccount.getId());
            }
        }

        //兼职部门
        V3xOrgMember memberById = orgManager.getMemberById(user.getId());
        V3xOrgDepartment dept = null;
        List<MemberPost> concurrent_post = memberById.getConcurrent_post();
        for (MemberPost memberPost : concurrent_post) {
            dept = orgManager.getDepartmentById(memberPost.getDepId());
            if (dept != null) {
                deptIdList.add(dept.getId());
            }
        }
        //副岗部门
        List<MemberPost> sec_post = memberById.getSecond_post();
        for (MemberPost memberPost : sec_post) {
            dept = orgManager.getDepartmentById(memberPost.getDepId());
            if (dept != null) {
                deptIdList.add(dept.getId());
            }
        }
        List<Object[]> result = edocSendDetailDao.getMarkList(deptIdList);
        StringBuilder ids = new StringBuilder("");
        Calendar cal = Calendar.getInstance();
        String yearNo = String.valueOf(cal.get(Calendar.YEAR));
        for (int i = 0; i < result.size(); i++) {
            int n = 0;
            Object[] object = (Object[]) result.get(i);
            String id = String.valueOf(object[0]);
            if (ids.indexOf(id) == -1) {
                EdocMarkDefinition def = new EdocMarkDefinition();
                Long definitionId = (Long) object[n++];
                def.setId(definitionId);
                String wordNo = (String) object[n++];
                String expression = (String) object[n++];
                boolean yearEnabled = (Boolean) object[n++];
                int maxNo = (Integer) object[n++];
                int length = (Integer) object[n++];
                int currentNo = (Integer) object[n++];
                String mark = getMark(wordNo, expression, yearEnabled, maxNo, length, currentNo, null, yearNo);
                String selectV = definitionId + "|" + mark + "|" + currentNo + "|" + com.seeyon.v3x.edoc.util.Constants.EDOC_MARK_EDIT_SELECT_NEW;
                def.setSelectV(selectV);
                def.setWordNo(mark);
                list.add(def);
            }
            ids.append(id).append(",");
        }
        return list;
    }

    public String getMark(String wordNo, String expression, boolean yearEnabled, int maxNo, int length, int currentNo, Integer curentno, String yearNo) {
        String yearNoStr = yearNo;
        if (yearNoStr == null || "".equals(yearNoStr)) {
            Calendar cal = Calendar.getInstance();
            yearNoStr = String.valueOf(cal.get(Calendar.YEAR));
        }
        if (Strings.isNotBlank(wordNo)) {
            if (wordNo.indexOf("\\") >= 0) wordNo = wordNo.replaceAll("\\\\", "\\\\\\\\");
            if (wordNo.indexOf("$") >= 0) wordNo = wordNo.replaceAll("\\$", "\\\\\\$");
            expression = expression.replaceFirst("\\$WORD", wordNo);
        }
        if (yearEnabled) {
            expression = expression.replaceFirst("\\$YEAR", yearNoStr);
        }
        StringBuilder flowNo = new StringBuilder(String.valueOf(currentNo));
        int curNoLen = String.valueOf(currentNo).length();
        int maxNoLen = String.valueOf(maxNo).length();
        if (length > 0 && length == maxNoLen) {
            flowNo = new StringBuilder("");
            for (int j = curNoLen; j < length; j++) {
                flowNo.append("0");
            }
            if (curentno != null) {
                flowNo.append(String.valueOf(curentno));
            } else {
                flowNo.append(String.valueOf(currentNo));
            }
        }
        expression = expression.replaceFirst("\\$NO", flowNo.toString());
        if (Strings.isNotBlank(expression)) {
            expression = expression.replaceFirst("\\$WORD", wordNo);
        }
        return expression;
    }

    /**
     * 检查签收编号是否重复
     *
     * @param edocRecieveRecordId
     * @return
     */
    public String isEditEdocMarkExist(String mark, String recieveId, String accountId, String depId) {
        EdocMarkModel em = EdocMarkModel.parse(mark);
        mark = em.getMark();
        List<EdocRecieveRecord> list = edocSendDetailDao.findReceiveRecordsByEdocMark(mark, Long.valueOf(recieveId));
        //OA-44275 A、B收发员使用同一个签收编号9号，签收成功公文了
        //将当前签收编号为 recNo的记录都取出来
        //签收编号重复的标准是：不同单位的可以用相同的签收编号，同一个单位下不能使用相同的签收编号(不同部门之间也不能使用) 这也是测试点
        String isExist = "false";
        if (list.size() > 0) {
            for (EdocRecieveRecord record : list) {
                int type = record.getExchangeType();
                //交换类型为部门
                if (type == 2) {
                    long exchangeAccountId = 0;
                    //获取部门对应的单位id
                    try {
                        V3xOrgDepartment dep = orgManager.getDepartmentById(record.getExchangeOrgId());
                        exchangeAccountId = dep.getOrgAccountId();
                    } catch (Exception e) {
                        LOGGER.error("根据部门id获取部门对象出错 ", e);
                    }
                    if (exchangeAccountId == Long.parseLong(accountId)) {
                        isExist = "true";
                        break;
                    }
                } else {
                    if (record.getExchangeOrgId() == Long.parseLong(accountId)) {
                        isExist = "true";
                        break;
                    }
                }
            }
        }
        return isExist;
    }

    public boolean isExist(List<String> str, String mark) {
        boolean flag = false;
        for (int i = 0; i < str.size(); i++) {
            if (str.get(i) != null) {
                if (str.get(i).equals(mark)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    /********************************************公文交换列表       start*************************************************************/
    /**
     * 读取当前用户的交换列表(待发送，已发送，待签收，已签收)- V5SP1
     *
     * @param type
     * @param conditioin
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public List findEdocExchangeRecordList(int type, Map<String, Object> condition) throws BusinessException {
        String accountIds = EdocRoleHelper.getUserExchangeAccountIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        String departIds = EdocRoleHelper.getUserExchangeDepartmentIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        condition.put("accountIds", accountIds);
        condition.put("departIds", departIds);
        String listType = (String) condition.get("listType");

        List<Integer> statusList = new ArrayList<Integer>();
        String substate = EdocNavigationEnum.EdocV5ListTypeEnum.getSubStateName(listType);
        if (Strings.isNotBlank(substate)) {
            String[] substates = substate.split(",");
            for (String s : substates) {
                statusList.add(Integer.parseInt(s));
            }
        }
        condition.put("statusList", statusList);
        if (type == EdocNavigationEnum.LIST_TYPE_EX_SEND) {
            List<EdocSendRecord> srs = sendEdocManager.findEdocSendRecordList(type, condition);

            for (EdocSendRecord re : srs) {
                if (re.getIsTurnRec() == null) {
                    re.setIsTurnRec(0);
                }
            }
            return srs;
        } else {
            return recieveEdocManager.findEdocRecieveRecordList(type, condition);
        }
    }

    /**
     * 逻辑删除交换数据
     *
     * @param id
     * @param type
     * @throws BusinessException
     */
    public void deleteExchangeDataByType(String listType, Map<Long, String> map) throws BusinessException {
        int type = EdocNavigationEnum.EdocV5ListTypeEnum.getTypeName(listType);
        if (type == EdocNavigationEnum.LIST_TYPE_EX_SEND) {//已发送、待发送列表删除
            sendEdocManager.deleteEdocSendRecordByLogic(listType, map);
        } else {//已签收列表删除
            recieveEdocManager.deleteEdocRecieveRecordByLogic(listType, map);
        }
    }

    /**
     * 签收回退到发文分发
     * 通过接口生成的数据将代签收列表数据逻辑清除，并将待办事项状态更新为已办
     *
     * @param stepBackEdocId      被回退的公文发文记录ID
     * @param stepBackInfo        回退说明
     * @param memberId            公文发文人ID
     * @param currentUserName     操作用户名
     * @param stepBackEdocSummary 被回退的公文对象
     * @param accountId           退回人所在单位ID
     * @throws Exception
     */
    public boolean stepBackEdoc(Long stepBackEdocId, String stepBackInfo, boolean oneself) throws BusinessException {
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(stepBackEdocId);
        boolean fromInternal = edocRecieveRecord.getFromInternal();//公文是否来自内部交换（同一套系统）
        if (!fromInternal) {//来自接口
            //更改签收数据状态为已回退
            this.updateRecieveRecordStatus(edocRecieveRecord);
            //删除签收待办表数据
            this.deleteRecieveRecordAffair(edocRecieveRecord);
            return true;
        }
        Long replyId = Long.parseLong(edocRecieveRecord.getReplyId());
        EdocSendDetail edocSendDetail = edocSendDetailDao.get(replyId);
        if (edocSendDetail == null) {
            return false;
        }
        EdocSendRecord edocSendRecord = sendEdocManager.getEdocSendRecord(edocSendDetail.getSendRecordId());
        if (edocSendRecord == null) {
            return false;
        }
        User user = CurrentUser.get();
        Long accountId = user.getAccountId();
        Long currentUserId = user.getId();
        String currentUserName = user.getName();
        /** 1、修改发文纪录详细信息状态为已退回 */
        if (edocSendDetail != null) {//回退时，不要写签收编号
            // 也不写签收人和签收时间（?）
            edocSendDetail.setRecUserName(currentUserName);
            edocSendDetail.setRecTime(new Timestamp(System.currentTimeMillis()));
            edocSendDetail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_StepBacked);
            edocSendDetailDao.update(edocSendDetail);
        }

        /** 2、更改签收数据状态为已回退 */
        this.updateRecieveRecordStatus(edocRecieveRecord);

        /** 3、保存退回附言 */
        String hasStepBackInfo = edocSendRecord.getStepBackInfo();
        String totalStepBackInfo = "";
        if (Strings.isBlank(hasStepBackInfo)) {//如果退回附言为空，显示
            if (edocRecieveRecord.getExchangeOrgId() == accountId) {
                totalStepBackInfo = accountId + "|" + stepBackInfo;
            } else {
                totalStepBackInfo = edocRecieveRecord.getExchangeOrgId() + "|" + stepBackInfo;
            }
        } else {
            if (edocRecieveRecord.getExchangeOrgId() == accountId) {
                totalStepBackInfo = hasStepBackInfo + "^" + accountId + "|" + stepBackInfo;
            } else {
                totalStepBackInfo = hasStepBackInfo + "^" + edocRecieveRecord.getExchangeOrgId() + "|" + stepBackInfo;
            }
        }
        edocSendRecord.setStepBackInfo(totalStepBackInfo);
        edocSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Send_StepBacked);
        sendEdocManager.update(edocSendRecord);

        /** 4、删除签收待办表数据 */
        List<MessageReceiver> deleteExSignreceivers = this.deleteRecieveRecordAffair(edocRecieveRecord);

        /** 5、产生新的待发送数据 */
        String sendedTypeIds = "";
        int assignType = EdocSendRecord.Exchange_Assign_To_Member;
        if (!oneself) {//不是退给收发员本人的
            assignType = EdocSendRecord.Exchange_Assign_To_All;
        }
        int newExchangeType = edocSendDetail.getRecOrgType();
        long newExchangeOrgId = Long.parseLong(edocSendDetail.getRecOrgId());
        EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecordById(edocSendRecord.getId());
        if (sendRecord == null) return false;
        EdocSendRecord newSendRecord = new EdocSendRecord();
        switch (newExchangeType) {
            case EdocSendDetail.Exchange_SendDetail_iAccountType_Org://单位交换
                sendedTypeIds = "Account|" + newExchangeOrgId;
                break;
            case EdocSendDetail.Exchange_SendDetail_iAccountType_Dept://部门交换
                sendedTypeIds = "Department|" + newExchangeOrgId;
                break;
        }
        newSendRecord.setContentNo(sendRecord.getContentNo());
        newSendRecord.setCopies(sendRecord.getCopies());
        newSendRecord.setDocMark(sendRecord.getDocMark());
        newSendRecord.setDocType(sendRecord.getDocType());
        newSendRecord.setEdocId(sendRecord.getEdocId());
        newSendRecord.setIssueDate(sendRecord.getIssueDate());
        newSendRecord.setIssuer(sendRecord.getIssuer());
        newSendRecord.setKeywords(sendRecord.getKeywords());
        newSendRecord.setSecretLevel(sendRecord.getSecretLevel());
        newSendRecord.setSubject(sendRecord.getSubject());
        newSendRecord.setUrgentLevel(sendRecord.getUrgentLevel());
        newSendRecord.setSendUnit(sendRecord.getSendUnit());
        newSendRecord.setSendUserId(sendRecord.getSendUserId());
        newSendRecord.setAssignType(assignType);
        newSendRecord.setIsBase(EdocSendRecord.Exchange_Base_NO);//退回表示：非原发文
        newSendRecord.setId(UUIDLong.longUUID());
        newSendRecord.setSendedTypeIds(sendedTypeIds);
        newSendRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newSendRecord.setExchangeType(sendRecord.getExchangeType());
        newSendRecord.setExchangeOrgId(sendRecord.getExchangeOrgId());
        newSendRecord.setExchangeAccountId(sendRecord.getExchangeAccountId());
        newSendRecord.setExchangeOrgName(sendRecord.getExchangeOrgName());
        newSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Send_New_StepBacked);
        newSendRecord.setIsTurnRec(sendRecord.getIsTurnRec());
        sendEdocManager.create(newSendRecord);

        /** 6、生成待发送新数据，含有退回标识 */
        CtpAffair cloneAffair = null;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("app", ApplicationCategoryEnum.exSend.key());
        params.put("objectId", sendRecord.getEdocId());
        params.put("subObjectId", sendRecord.getId());
        List<CtpAffair> senderAffairList = affairManager.getByConditions(null, params);
        if (Strings.isNotEmpty(senderAffairList)) {
            try {
                for (CtpAffair affair : senderAffairList) {
                    cloneAffair = (CtpAffair) affair.clone();
                    break;
                }
            } catch (CloneNotSupportedException cnse) {
                LOGGER.error("签收回退到发文分发抛出异常", cnse);
            }
        }
        List<V3xOrgMember> memberList = new ArrayList<V3xOrgMember>();
        if (newSendRecord.getAssignType() == EdocSendRecord.Exchange_Assign_To_All) {//竞争执行
            if (newSendRecord.getExchangeType() == EdocSendRecord.Exchange_Send_iExchangeType_Dept) {//部门交换
                memberList = orgManager.getMembersByRole(newSendRecord.getExchangeOrgId(), OrgConstants.Role_NAME.Departmentexchange.name());
            } else if (newSendRecord.getExchangeType() == EdocSendRecord.Exchange_Send_iExchangeType_Org) {//单位交换
                memberList = orgManager.getMembersByRole(newSendRecord.getExchangeOrgId(), OrgConstants.Role_NAME.Accountexchange.name());
            }
        } else {//指定人执行->实际发送人
            V3xOrgMember member = orgManager.getMemberById(newSendRecord.getSendUserId());
            memberList.add(member);
        }

        List<MessageReceiver> sendReceivers = new ArrayList<MessageReceiver>();
        Set<Long> newExSendMemberIds = new HashSet<Long>();
        if (Strings.isNotEmpty(memberList)) {
            EdocSummary summary = edocSummaryManager.findById(newSendRecord.getEdocId());
            for (V3xOrgMember v3xOrgMember : memberList) {
                CtpAffair newAffair = cloneAffair;
                if (newAffair == null) {
                    newAffair = new CtpAffair();
                    newAffair.setApp(ApplicationCategoryEnum.exSend.getKey());
                    newAffair.setSubject(newSendRecord.getSubject());
                    newAffair.setFinish(Boolean.FALSE);

                    if (Strings.isNotBlank(newSendRecord.getUrgentLevel())) {
                        newAffair.setImportantLevel(Integer.parseInt(newSendRecord.getUrgentLevel()));
                    }

                    newAffair.setSubState(SubStateEnum.col_normal.key());
                    newAffair.setObjectId(newSendRecord.getEdocId());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_edocMark, newSendRecord.getDocMark());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendUnit, newSendRecord.getSendUnit());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendAccountId, summary.getSendUnitId());
                }
                newAffair.setId(UUIDLong.longUUID());
                newAffair.setDelete(Boolean.FALSE);
                newAffair.setState(StateEnum.edoc_exchange_send.key());
                newAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
                newAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
                newAffair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
                newAffair.setSubObjectId(newSendRecord.getId());
                //OA-45746待发送数据的发起人显示不一致
                newAffair.setSenderId(summary.getStartUserId());
                newAffair.setMemberId(v3xOrgMember.getId());
                newAffair.setSubState(SubStateEnum.col_normal.key());
                AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_edocExSendRetreat, currentUserId);//G6原来set到此字段的值,一起加
                affairManager.save(newAffair);
                sendReceivers.add(new MessageReceiver(newAffair.getId(), newAffair.getMemberId(), "message.link.exchange.sent", sendRecord.getId(), newAffair.getId()));
                newExSendMemberIds.add(newAffair.getMemberId());
            }
            if (Strings.isNotEmpty(deleteExSignreceivers)) {
                for (Iterator<MessageReceiver> it = deleteExSignreceivers.iterator(); it.hasNext(); ) {
                    MessageReceiver r = it.next();
                    if (newExSendMemberIds.contains(Long.valueOf(r.getReceiverId()))) {
                        it.remove();
                    }
                }
            }
            /** 7、给公文的发文单位的公文交换员发退回消息 */
            if (sendReceivers.size() > 0) {
                MessageContent content = new MessageContent("exchange.stepback", newSendRecord.getSubject(), currentUserName, stepBackInfo);
                if (null != summary && null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content,
                        ApplicationCategoryEnum.exSign,
                        currentUserId,
                        sendReceivers,
                        EdocMessageFilterParamEnum.exchange.key);
            }

            /** 8、（代签收的人员）发消息给其他公文收发员，公文已回退 */
            if (null != deleteExSignreceivers && deleteExSignreceivers.size() > 0) {
                MessageContent content = new MessageContent("exchange.stepback", newSendRecord.getSubject(), currentUserName, stepBackInfo);
                if (null != summary && null != summary.getTempleteId()) {
                    content.setTemplateId(summary.getTempleteId());
                }
                userMessageManager.sendSystemMessage(content,
                        ApplicationCategoryEnum.edocRec,
                        currentUserId,
                        deleteExSignreceivers,
                        EdocMessageFilterParamEnum.exchange.key);
            }
        }

        /***** 9、记录新的交换纪录与撤销的那个交换记录的关系 ****/
        EdocSendRecordReference recordReference = new EdocSendRecordReference();
        recordReference.setId(UUIDLong.longUUID());
        recordReference.setNewSendRecodId(newSendRecord.getId());
        recordReference.setReferenceSendRecodId(sendRecord.getId());

        edocSendRecordReferenceDao.save(recordReference);

        return true;
    }

    /**
     * 2、更改签收数据状态为已回退
     *
     * @param edocRecieveRecord
     * @throws BusinessException
     */
    private void updateRecieveRecordStatus(EdocRecieveRecord edocRecieveRecord) throws BusinessException {
        edocRecieveRecord.setStatus(EdocRecieveRecord.Exchange_iStatus_Receive_StepBacked);
        recieveEdocManager.update(edocRecieveRecord);
    }

    /**
     * 删除签收待办表数据
     *
     * @param edocRecieveRecord
     * @throws BusinessException
     */
    private List<MessageReceiver> deleteRecieveRecordAffair(EdocRecieveRecord edocRecieveRecord) throws BusinessException {
        User user = CurrentUser.get();
        List<MessageReceiver> deleteExSignreceivers = new ArrayList<MessageReceiver>();
        Map<String, Object> conditions = new HashMap<String, Object>();
        conditions.put("app", ApplicationCategoryEnum.exSign.key());
        conditions.put("objectId", edocRecieveRecord.getEdocId());
        conditions.put("subObjectId", edocRecieveRecord.getId());
        List<CtpAffair> affairList = affairManager.getByConditions(null, conditions);
        if (null != affairList && affairList.size() > 0) {
            for (CtpAffair af : affairList) {
                if (af.getMemberId().longValue() != user.getId() && af.isDelete() != true) {
                    deleteExSignreceivers.add(new MessageReceiver(af.getId(), af.getMemberId()));
                }
            }
            // 批量更新,待签收公文变成已经发送
            Map<String, Object> columns = new Hashtable<String, Object>();
            columns.put("finish", true);
            columns.put("delete", true);
            columns.put("state", StateEnum.edoc_exchange_received.getKey());
            Object[][] wheres = new Object[3][2];
            wheres[0][0] = "app";
            wheres[0][1] = ApplicationCategoryEnum.exSign.key();
            wheres[1][0] = "objectId";
            wheres[1][1] = edocRecieveRecord.getEdocId();
            wheres[2][0] = "subObjectId";
            wheres[2][1] = edocRecieveRecord.getId();
            affairManager.update(columns, wheres);
        }
        return deleteExSignreceivers;
    }

    /**
     * ajax请求：撤销交换记录
     *
     * @param sendRecordId 公文交换纪录ID
     * @param detailId     公文交换纪录详情ID
     * @return
     * @throws Exception
     */
    public String withdraw(String sendRecordId, String detailId, String accountId, String sendCancelInfo) throws BusinessException {
        try {
            if (Strings.isBlank(detailId)) {
                return "";
            }
            User user = AppContext.getCurrentUser();

            int newExchangeType = 0;
            long newExchangeOrgId = 0;

            /***** 1、 删除待签收记录(物理删除) ****/
            EdocRecieveRecord record = recieveEdocManager.getReceiveRecordByReplyId(Long.valueOf(detailId).longValue());
            if (null == record) {
                return String.valueOf(EdocSendDetail.Exchange_iStatus_SendDetail_Cancel);
            }

            //验证签收状态
            EdocSendDetail detail = sendEdocManager.getSendRecordDetail(Long.parseLong(detailId));
            if (detail == null) {//已撤销
                return String.valueOf(EdocSendDetail.Exchange_iStatus_SendDetail_Cancel);
            } else if (detail.getStatus() == EdocSendDetail.Exchange_iStatus_SendDetail_StepBacked //已退回
                    || detail.getStatus() == EdocSendDetail.Exchange_iStatus_SendDetail_Cancel //已撤销
                    || detail.getStatus() == EdocSendDetail.Exchange_iStatus_SendDetail_Recieved) {//已签收
                return String.valueOf(detail.getStatus());
            }

            recieveEdocManager.delete(record);

            /***** 2、删除待办纪录(逻辑删除) ****/
            Map<String, Object> conditions = new HashMap<String, Object>();
            conditions.put("app", ApplicationCategoryEnum.exSign.key());
            conditions.put("objectId", record.getEdocId());
            conditions.put("subObjectId", record.getId());
            List<CtpAffair> affairList = affairManager.getByConditions(null, conditions);
            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();//删除完毕后，更新每一个签收的待办事项
            if (null != affairList && affairList.size() > 0) {
                for (CtpAffair af : affairList) {
                    if (af.getMemberId().longValue() != user.getId() && (af.isDelete() != true)) {
                        receivers.add(new MessageReceiver(af.getId(), af.getMemberId()));
                    }
                    af.setState(StateEnum.edoc_exchange_withdraw.getKey()); //代办事项状态：被撤销
                    af.setDelete(true);
                    af.setFinish(true);
                    affairManager.updateAffair(af);
                }
            }

            /***** 3、修改交换纪录详细信息状态为已撤销 ****/
            String sendedTypeIds = "";
            if (detail != null) {
                newExchangeType = detail.getRecOrgType();
                newExchangeOrgId = Long.parseLong(detail.getRecOrgId());
                detail.setSendRecordId(Long.parseLong(sendRecordId));
                detail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_Cancel);
                sendEdocManager.updateSendRecordDetail(detail);
            }

            EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecordById(Long.parseLong(sendRecordId));
            if (sendRecord != null) {
                /** 4、保存撤销附言 */
                String hasStepBackInfo = sendRecord.getStepBackInfo();
                String totalStepBackInfo = "";
                if (Strings.isBlank(hasStepBackInfo)) {//如果退回附言为空，显示
                    if (detail.getRecOrgId().equals(accountId)) {
                        totalStepBackInfo = user.getId() + "*" + accountId + "|" + sendCancelInfo;
                    } else {
                        totalStepBackInfo = user.getId() + "*" + detail.getRecOrgId() + "|" + sendCancelInfo;
                    }
                } else {
                    if (detail.getRecOrgId().equals(accountId)) {
                        totalStepBackInfo = hasStepBackInfo + "^" + user.getId() + "*" + accountId + "|" + sendCancelInfo;
                    } else {
                        totalStepBackInfo = hasStepBackInfo + "^" + user.getId() + "*" + detail.getRecOrgId() + "|" + sendCancelInfo;
                    }
                }
                sendRecord.setStepBackInfo(totalStepBackInfo);
                sendEdocManager.update(sendRecord);

                /***** 5、生成新的交换纪录 ****/
                EdocSendRecord newSendRecord = new EdocSendRecord();
                switch (newExchangeType) {
                    case EdocSendDetail.Exchange_SendDetail_iAccountType_Org://单位交换
                        sendedTypeIds = "Account|" + newExchangeOrgId;
                        break;
                    case EdocSendDetail.Exchange_SendDetail_iAccountType_Dept://部门交换
                        sendedTypeIds = "Department|" + newExchangeOrgId;
                        break;
                }
                newSendRecord.setContentNo(sendRecord.getContentNo());
                newSendRecord.setCopies(sendRecord.getCopies());
                newSendRecord.setDocMark(sendRecord.getDocMark());
                newSendRecord.setDocType(sendRecord.getDocType());
                newSendRecord.setEdocId(sendRecord.getEdocId());
                newSendRecord.setIssueDate(sendRecord.getIssueDate());
                newSendRecord.setIssuer(sendRecord.getIssuer());
                newSendRecord.setKeywords(sendRecord.getKeywords());
                newSendRecord.setSecretLevel(sendRecord.getSecretLevel());
                newSendRecord.setSubject(sendRecord.getSubject());
                newSendRecord.setUrgentLevel(sendRecord.getUrgentLevel());
                newSendRecord.setSendUnit(sendRecord.getSendUnit());
                newSendRecord.setSendUserId(user.getId());
                newSendRecord.setAssignType(EdocSendRecord.Exchange_Assign_To_Member);//撤销表示，指定交换，交换人为user.getId()
                newSendRecord.setIsBase(EdocSendRecord.Exchange_Base_NO);//补发表示：非原发文
                newSendRecord.setId(UUIDLong.longUUID());
                newSendRecord.setSendedTypeIds(sendedTypeIds);
                newSendRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
                newSendRecord.setExchangeType(sendRecord.getExchangeType());
                newSendRecord.setExchangeOrgId(sendRecord.getExchangeOrgId());
                newSendRecord.setExchangeAccountId(sendRecord.getExchangeAccountId());
                newSendRecord.setExchangeOrgName(sendRecord.getExchangeOrgName());
                newSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
                newSendRecord.setIsTurnRec(sendRecord.getIsTurnRec());
                sendEdocManager.create(newSendRecord);

                /** 5、生成待发送新数据 */
                CtpAffair cloneAffair = null;
                List<CtpAffair> sendAffairList = affairManager.getAffairs(ApplicationCategoryEnum.exSend, newSendRecord.getEdocId());
                if (Strings.isNotEmpty(sendAffairList)) {
                    for (CtpAffair affair : sendAffairList) {
                        cloneAffair = (CtpAffair) affair.clone();
                        break;
                    }
                }
                CtpAffair newAffair = cloneAffair;
                if (newAffair == null) {
                    newAffair = new CtpAffair();
                    newAffair.setApp(ApplicationCategoryEnum.exSend.getKey());
                    newAffair.setSubject(newSendRecord.getSubject());
                    newAffair.setFinish(Boolean.FALSE);

                    if (newSendRecord.getUrgentLevel() != null) {

                        newAffair.setImportantLevel(Integer.parseInt(newSendRecord.getUrgentLevel()));
                    }

                    newAffair.setSubState(SubStateEnum.col_normal.key());
                    newAffair.setObjectId(newSendRecord.getEdocId());
                    EdocSummary summary = edocSummaryManager.findById(newSendRecord.getEdocId());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendAccountId, summary.getSendUnitId());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_edocMark, newSendRecord.getDocMark());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendUnit, newSendRecord.getSendUnit());
                }
                newAffair.setId(UUIDLong.longUUID());
                newAffair.setDelete(Boolean.FALSE);
                newAffair.setState(StateEnum.edoc_exchange_send.key());
                newAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
                newAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
                newAffair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
                newAffair.setSubObjectId(newSendRecord.getId());
                newAffair.setSenderId(user.getId());
                newAffair.setMemberId(user.getId());
                newAffair.setSubState(SubStateEnum.col_normal.key());
                affairManager.save(newAffair);

                /***** 记录新的交换纪录与撤销的那个交换记录的关系 ****/
                EdocSendRecordReference recordReference = new EdocSendRecordReference();
                recordReference.setId(UUIDLong.longUUID());
                recordReference.setNewSendRecodId(newSendRecord.getId());
                recordReference.setReferenceSendRecodId(sendRecord.getId());

                edocSendRecordReferenceDao.save(recordReference);
            }


            /***** 6、操作日志：交换撤销 ****/
            operationlogManager.insertOplog(record.getId(), ApplicationCategoryEnum.exchange, EactionType.LOG_EXCHANGE_WITHDRAW, EactionType.LOG_EXCHANGE_WITHDRAWD_DESCRIPTION, user.getName(), record.getSubject());

            /***** 7、给签收人发送撤销消息 ****/
            if (null != receivers && receivers.size() > 0) {
                MessageContent content = new MessageContent("exchange.withdraw", affairList.get(0).getSubject(), user.getName(), affairList.get(0).getApp());
                if (null != affairList.get(0).getTempleteId()) {
                    content.setTemplateId(affairList.get(0).getTempleteId());
                }
                userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.exSign, user.getId(), receivers, EdocMessageFilterParamEnum.exchange.key);
            }

            /***** 8、如果收文交换类型，还需要删除转收文信息的 对应主送单位 ****/

            /*****
             这段代码保留，怕需求又变动回来：OA-78471

             if(sendRecord.getIsTurnRec() != null && sendRecord.getIsTurnRec() == 1){
             long edocId = sendRecord.getEdocId();
             EdocExchangeTurnRecManager edocExchangeTurnRecManager = (EdocExchangeTurnRecManager)AppContext.getBean("edocExchangeTurnRecManager");
             EdocExchangeTurnRec turnRec = edocExchangeTurnRecManager.findEdocExchangeTurnRecByEdocId(edocId);
             //原来的主送单位
             String typeAndIds = turnRec.getTypeAndIds();
             String[] tAttr = typeAndIds.split("[,]");
             String newTypeAndIds = "";
             for(String typeAndId : tAttr){
             if(typeAndId.indexOf(detail.getRecOrgId())==-1){
             newTypeAndIds += typeAndId+",";
             }
             }
             if(Strings.isNotBlank(newTypeAndIds)){
             newTypeAndIds = newTypeAndIds.substring(0,newTypeAndIds.length()-1);
             }
             turnRec.setTypeAndIds(newTypeAndIds);
             edocExchangeTurnRecManager.updateTurnRec(turnRec);
             }
             *****************/

        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return String.valueOf(EdocSendDetail.Exchange_iStatus_SendDetail_Torecieve);
    }

    /**
     * ajax请求：验证是否可撤销交换
     *
     * @param replyId
     * @param detailId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public boolean canWithdraw(String sendRecordId, String detailId) throws BusinessException {
        boolean bool = true;
        //设置默认变量 ，可以撤销true
        if (Strings.isBlank(sendRecordId)) return false;
        EdocSendRecord edocSendRecord = sendEdocManager.getEdocSendRecord(Long.valueOf(sendRecordId));
        //查找出公文交换记录
        Set<EdocSendDetail> sendDetails = (Set<EdocSendDetail>) edocSendRecord.getSendDetails();
        //根据公文交换记录得到交换的回执记录
        if (sendDetails == null || sendDetails.size() == 0) {
            return false;
            //如果回执记录为空，返回false;
        }
        Iterator it = sendDetails.iterator();
        //迭代绘制记录集合
        while (it.hasNext()) {
            EdocSendDetail sendDetail = (EdocSendDetail) it.next();
            if (sendDetail.getId().longValue() == Long.valueOf(detailId).longValue() && sendDetail.getStatus() == EdocSendDetail.Exchange_iStatus_SendDetail_Recieved) {
                bool = false;
                //如果其中状态为已签收的回执记录，bool -> false(不可撤销)
            }
        }
        return bool;
    }

    /**
     * 客开：徐矿集团  chenqiang 2019年3月19日
     * ajax请求：全部撤销
     *
     * @param replyId
     * @param detailId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public String withdrawAll(String param, String sendCancelInfo) throws BusinessException {
        Set<String> allSendedTypes = new HashSet<String>();
        JSONObject paramJob = JSONObject.parseObject(param);
        JSONArray jArr = (JSONArray) paramJob.get("xkjtCheckedArr");
        EdocSendRecord newSendRecord = null;//新的待发送对象
        CtpAffair newAffair = null;//待办事项
        String sendRecordId = "";
        for (Object object : jArr) {
            List<String> sendedTypeIdsList = new ArrayList<String>();//存放发送部门

            String cancelInfo = "";//撤销附言
            try {
                JSONObject job = (JSONObject) object;
                sendRecordId = String.valueOf(job.get("sendRecordId"));
                String detailId = String.valueOf(job.get("detailId"));
                String accountId = String.valueOf(job.get("accountId"));
                String leaderId = String.valueOf(job.get("leaderId"));//人员ID
                String daiyueId = String.valueOf(job.get("daiyueId"));//待阅文件Id

                //客开：徐矿集团【撤销领导人的待阅】 chenqiang 20190412 start
                if (Strings.isNotBlank(daiyueId)) {
                    EdocSendRecord sendRecord1 = sendEdocManager.getEdocSendRecordById(Long.parseLong(sendRecordId));
                    sendedTypeIdsList.add("Member|" + leaderId);
                    XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
                    List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = xkjtManager.findXkjtLeaderDaiYueById(Long.valueOf(daiyueId));
                    if (xkjtLeaderDaiYues.size() != 0) {

                        //update  chenq 只生成一条待发送数据   20200805   start
                        allSendedTypes.add("Member|" + leaderId);
                        //update  chenq 只生成一条待发送数据   20200805   end
                        XkjtLeaderDaiYue xkjtLeaderDaiYue = xkjtLeaderDaiYues.get(0);
                        xkjtLeaderDaiYue.setStatus(13);
                        xkjtManager.updateXkjtLeaderDaiYue(xkjtLeaderDaiYue);
                        LOGGER.info("chenq---撤销人员的待阅件");
                    }
                } else {


                    //客开：徐矿集团【撤销领导人的待阅】 chenqiang 20190412 end

                    User user = AppContext.getCurrentUser();

                    int newExchangeType = 0;
                    long newExchangeOrgId = 0;

                    /***** 1、 删除待签收记录(物理删除) ****/
                    EdocRecieveRecord record = recieveEdocManager.getReceiveRecordByReplyId(Long.valueOf(detailId).longValue());
                    if (null == record) {
//				    return String.valueOf(EdocSendDetail.Exchange_iStatus_SendDetail_Cancel);
                        continue;
                    }

                    //验证签收状态
                    EdocSendDetail detail = sendEdocManager.getSendRecordDetail(Long.parseLong(detailId));
                    //客开：徐矿集团【撤销已签收】 chenqiang 20190411 start
                    if (detail == null) {//已撤销
                        return String.valueOf(EdocSendDetail.Exchange_iStatus_SendDetail_Cancel);
                    } else if (detail.getStatus() == EdocSendDetail.Exchange_iStatus_SendDetail_StepBacked //已退回
                            || detail.getStatus() == EdocSendDetail.Exchange_iStatus_SendDetail_Cancel //已撤销
//						|| detail.getStatus() == EdocSendDetail.Exchange_iStatus_SendDetail_Recieved
                    ) {//已签收
                        return String.valueOf(detail.getStatus());
                    }
                    //客开：徐矿集团【撤销已签收】 chenqiang 20190411 end
                    recieveEdocManager.delete(record);

                    //客开：徐矿集团【已签收待登记公文撤销】 chenqiang 20190411 start
                    EdocRegisterManager edocRegisterManager = (EdocRegisterManager) AppContext.getBean("edocRegisterManager");
                    EdocRegister edocRegister = edocRegisterManager.findRegisterByRecieveId(record.getId());
                    if (edocRegister != null && edocRegister.getState() == 2) {//已签收待登记
                        edocRegister.setState(SubStateEnum.col_done_stepStop.key());//去除待登记记录
                        edocRegisterManager.update(edocRegister);//更新状态
                    }

                    //客开：徐矿集团【已签收待登记公文撤销】 chenqiang 20190411 end

                    /***** 2、删除待办纪录(逻辑删除) ****/
                    Map<String, Object> conditions = new HashMap<String, Object>();
                    conditions.put("app", ApplicationCategoryEnum.exSign.key());
                    conditions.put("objectId", record.getEdocId());
                    conditions.put("subObjectId", record.getId());
                    List<CtpAffair> affairList = affairManager.getByConditions(null, conditions);
                    List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();//删除完毕后，更新每一个签收的待办事项
                    if (null != affairList && affairList.size() > 0) {
                        for (CtpAffair af : affairList) {
                            if (af.getMemberId().longValue() != user.getId() && (af.isDelete() != true)) {
                                receivers.add(new MessageReceiver(af.getId(), af.getMemberId()));
                            }

                            af.setState(StateEnum.edoc_exchange_withdraw.getKey()); //代办事项状态：被撤销

                            af.setDelete(true);
                            af.setFinish(true);
                            affairManager.updateAffair(af);
                        }
                    }

                    /***** 3、修改交换纪录详细信息状态为已撤销 ****/
                    String sendedTypeIds = "";
                    if (detail != null) {
                        newExchangeType = detail.getRecOrgType();
                        newExchangeOrgId = Long.parseLong(detail.getRecOrgId());
                        detail.setSendRecordId(Long.parseLong(sendRecordId));
                        detail.setStatus(EdocSendDetail.Exchange_iStatus_SendDetail_Cancel);
                        sendEdocManager.updateSendRecordDetail(detail);
                    }

                    EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecordById(Long.parseLong(sendRecordId));
                    if (sendRecord != null) {
	/*					//客开：徐矿集团【撤销领导人的待阅】 chenqiang 20190412 start  废弃单独撤销
					if(StringUtils.isNoneBlank(leaderId)){
						XkjtManager xkjtManager = (XkjtManager) AppContext.getBean("xkjtManager");
						List<XkjtLeaderDaiYue> xkjtLeaderDaiYues =  xkjtManager.findXkjtLeaderDaiYueByEdocId(sendRecord.getEdocId(),Long.valueOf(leaderId));
						if(xkjtLeaderDaiYues.size()!=0){
							XkjtLeaderDaiYue xkjtLeaderDaiYue = xkjtLeaderDaiYues.get(0);
							xkjtLeaderDaiYue.setStatus(13);
							xkjtLeaderDaiYue.setCancelInfo(sendRecord.getStepBackInfo());
							LOGGER.info("chenq---撤销人员的待阅件");
						}
					}


					//客开：徐矿集团【撤销领导人的待阅】 chenqiang 20190412 end
	*/                    /** 4、保存撤销附言 */
                        String hasStepBackInfo = sendRecord.getStepBackInfo();
                        String totalStepBackInfo = "";
                        if (Strings.isBlank(hasStepBackInfo)) {//如果退回附言为空，显示
                            if (detail.getRecOrgId().equals(accountId)) {
                                totalStepBackInfo = user.getId() + "*" + accountId + "|" + sendCancelInfo;
                            } else {
                                totalStepBackInfo = user.getId() + "*" + detail.getRecOrgId() + "|" + sendCancelInfo;
                            }
                        } else {
                            // 客开：徐矿【全部撤销时屏蔽这个地方  原逻辑会导致字段不够长报错的问题】 chenqiang  20200609 start
						/*if(detail.getRecOrgId().equals(accountId)) {
							totalStepBackInfo = hasStepBackInfo+"^"+user.getId()+"*"+accountId+"|"+sendCancelInfo;
						} else {
							totalStepBackInfo = hasStepBackInfo+"^"+user.getId()+"*"+detail.getRecOrgId()+"|"+sendCancelInfo;
						}*/
                            // 客开：徐矿【全部撤销时屏蔽这个地方  原逻辑会导致字段不够长报错的问题】 chenqiang  20200609 end
                        }
                        sendRecord.setStepBackInfo(totalStepBackInfo);
                        sendEdocManager.update(sendRecord);

                        /***** 5、生成新的交换纪录 ****/
                        switch (newExchangeType) {
                            case EdocSendDetail.Exchange_SendDetail_iAccountType_Org://单位交换
                                sendedTypeIds = "Account|" + newExchangeOrgId;
                                break;
                            case EdocSendDetail.Exchange_SendDetail_iAccountType_Dept://部门交换
                                sendedTypeIds = "Department|" + newExchangeOrgId;
                                break;
                        }
                        //update 只生成一条待签收数据  chenq 20200805 start
                        allSendedTypes.add(sendedTypeIds);
                        //update 只生成一条待签收数据  chenq 20200805 end
                        sendedTypeIdsList.add(sendedTypeIds);
                        if (newSendRecord == null) {
                            newSendRecord = new EdocSendRecord();
                            newSendRecord.setContentNo(sendRecord.getContentNo());
                            newSendRecord.setCopies(sendRecord.getCopies());
                            newSendRecord.setDocMark(sendRecord.getDocMark());
                            newSendRecord.setDocType(sendRecord.getDocType());
                            newSendRecord.setEdocId(sendRecord.getEdocId());
                            newSendRecord.setIssueDate(sendRecord.getIssueDate());
                            newSendRecord.setIssuer(sendRecord.getIssuer());
                            newSendRecord.setKeywords(sendRecord.getKeywords());
                            newSendRecord.setSecretLevel(sendRecord.getSecretLevel());
                            newSendRecord.setSubject(sendRecord.getSubject());
                            newSendRecord.setUrgentLevel(sendRecord.getUrgentLevel());
                            newSendRecord.setSendUnit(sendRecord.getSendUnit());
                            newSendRecord.setSendUserId(user.getId());
                            newSendRecord.setAssignType(EdocSendRecord.Exchange_Assign_To_Member);//撤销表示，指定交换，交换人为user.getId()
                            newSendRecord.setIsBase(EdocSendRecord.Exchange_Base_NO);//补发表示：非原发文
                            newSendRecord.setId(UUIDLong.longUUID());
                            newSendRecord.setSendedTypeIds(sendedTypeIds);
                            newSendRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
                            newSendRecord.setExchangeType(sendRecord.getExchangeType());
                            newSendRecord.setExchangeOrgId(sendRecord.getExchangeOrgId());
                            newSendRecord.setExchangeAccountId(sendRecord.getExchangeAccountId());
                            newSendRecord.setExchangeOrgName(sendRecord.getExchangeOrgName());
                            newSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
                            newSendRecord.setIsTurnRec(sendRecord.getIsTurnRec());
                        }


                        /** 5、生成待发送新数据 */
                        CtpAffair cloneAffair = null;
                        List<CtpAffair> sendAffairList = affairManager.getAffairs(ApplicationCategoryEnum.exSend, newSendRecord.getEdocId());
                        if (Strings.isNotEmpty(sendAffairList)) {
                            for (CtpAffair affair : sendAffairList) {
                                cloneAffair = (CtpAffair) affair.clone();
                                break;
                            }
                        }

                        if (newAffair == null) {
                            newAffair = cloneAffair;
                            newAffair = new CtpAffair();
                            newAffair.setApp(ApplicationCategoryEnum.exSend.getKey());
                            newAffair.setSubject(newSendRecord.getSubject());
                            newAffair.setFinish(Boolean.FALSE);

                            if (newSendRecord.getUrgentLevel() != null) {
                                newAffair.setImportantLevel(Integer.parseInt(newSendRecord.getUrgentLevel()));
                            }

                            newAffair.setSubState(SubStateEnum.col_normal.key());
                            newAffair.setObjectId(newSendRecord.getEdocId());
                            EdocSummary summary = edocSummaryManager.findById(newSendRecord.getEdocId());
                            AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendAccountId, summary.getSendUnitId());
                            AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_edocMark, newSendRecord.getDocMark());
                            AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendUnit, newSendRecord.getSendUnit());
                            newAffair.setId(UUIDLong.longUUID());
                            newAffair.setDelete(Boolean.FALSE);
                            newAffair.setState(StateEnum.edoc_exchange_send.key());
                            newAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
                            newAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
                            newAffair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
                            newAffair.setSubObjectId(newSendRecord.getId());
                            newAffair.setSenderId(user.getId());
                            newAffair.setMemberId(user.getId());
                            newAffair.setSubState(SubStateEnum.col_normal.key());
                        }


                        /***** 记录新的交换纪录与撤销的那个交换记录的关系 ****/
                        EdocSendRecordReference recordReference = new EdocSendRecordReference();
                        recordReference.setId(UUIDLong.longUUID());
                        recordReference.setNewSendRecodId(newSendRecord.getId());
                        recordReference.setReferenceSendRecodId(sendRecord.getId());

                        edocSendRecordReferenceDao.save(recordReference);
                        /*客开：徐矿集团【撤销已签收已登记的公文】 chenqiang 20190411 start*/
                        //更新事项信息
                        AffairDao affairDao = (AffairDao) AppContext.getBean("affairDao");
                        List<CtpAffair> edocRecAffairList = new ArrayList();

                        if (record.getReciveEdocId() == null) {
                            edocRecAffairList = affairManager.getAffairs(ApplicationCategoryEnum.edocRegister, record.getEdocId());//待登记
                        } else {
                            edocRecAffairList = affairDao.getAffairsByAppAndObjectId(ApplicationCategoryEnum.edocRec, record.getReciveEdocId());//已登记
                            //更新公文信息 chenqiang
                            EdocSummaryDao edocSummaryDao = (EdocSummaryDao) AppContext.getBean("edocSummaryDao");
                            EdocSummary xkjtEdoc = edocSummaryDao.get(record.getReciveEdocId());
                            xkjtEdoc.setSubject(xkjtEdoc.getSubject() + "(已收回)");
                            xkjtEdoc.setState(CollaborationEnum.flowState.terminate.ordinal());
                            edocSummaryDao.update(xkjtEdoc);
                        }

                        for (CtpAffair ctpAffair : edocRecAffairList) {
                            if (ctpAffair.getState() == 3) {//待办
                                ctpAffair.setState(StateEnum.col_done.key());
                                ctpAffair.setSubState(SubStateEnum.col_done_stepStop.key());
                                affairManager.updateAffair(ctpAffair);
                                IndexManager indexManager = (IndexManager) AppContext.getBean("indexManager");
                                indexManager.delete(ctpAffair.getId(), ctpAffair.getApp());
                            }
                        }


                        /*客开：徐矿集团【撤销已签收已登记的公文】 chenqiang 20190411 end*/
                    }


                    /***** 6、操作日志：交换撤销 ****/
                    operationlogManager.insertOplog(record.getId(), ApplicationCategoryEnum.exchange, EactionType.LOG_EXCHANGE_WITHDRAW, EactionType.LOG_EXCHANGE_WITHDRAWD_DESCRIPTION, user.getName(), record.getSubject());

                    /***** 7、给签收人发送撤销消息 ****/
                    if (null != receivers && receivers.size() > 0) {
                        MessageContent content = new MessageContent("exchange.withdraw", affairList.get(0).getSubject(), user.getName(), affairList.get(0).getApp());
                        if (null != affairList.get(0).getTempleteId()) {
                            content.setTemplateId(affairList.get(0).getTempleteId());
                        }
                        userMessageManager.sendSystemMessage(content, ApplicationCategoryEnum.exSign, user.getId(), receivers, EdocMessageFilterParamEnum.exchange.key);
                    }

                    /***** 8、如果收文交换类型，还需要删除转收文信息的 对应主送单位 ****/

                    /*****
                     这段代码保留，怕需求又变动回来：OA-78471

                     if(sendRecord.getIsTurnRec() != null && sendRecord.getIsTurnRec() == 1){
                     long edocId = sendRecord.getEdocId();
                     EdocExchangeTurnRecManager edocExchangeTurnRecManager = (EdocExchangeTurnRecManager)AppContext.getBean("edocExchangeTurnRecManager");
                     EdocExchangeTurnRec turnRec = edocExchangeTurnRecManager.findEdocExchangeTurnRecByEdocId(edocId);
                     //原来的主送单位
                     String typeAndIds = turnRec.getTypeAndIds();
                     String[] tAttr = typeAndIds.split("[,]");
                     String newTypeAndIds = "";
                     for(String typeAndId : tAttr){
                     if(typeAndId.indexOf(detail.getRecOrgId())==-1){
                     newTypeAndIds += typeAndId+",";
                     }
                     }
                     if(Strings.isNotBlank(newTypeAndIds)){
                     newTypeAndIds = newTypeAndIds.substring(0,newTypeAndIds.length()-1);
                     }
                     turnRec.setTypeAndIds(newTypeAndIds);
                     edocExchangeTurnRecManager.updateTurnRec(turnRec);
                     }
                     *****************/
                    String sendedTypeIdsStr = "";
                    if (sendedTypeIdsList.size() == 1) {
                        sendedTypeIdsStr = sendedTypeIdsList.get(0);
                    } else {
                        for (int i = 0; i < sendedTypeIdsList.size(); i++) {
                            if (i == 0) {
                                sendedTypeIdsStr += sendedTypeIdsList.get(i);
                            } else {
                                sendedTypeIdsStr += "," + sendedTypeIdsList.get(i);
                            }
                        }
                    }


                }


            } catch (Exception e) {
                LOGGER.error("全部撤销失败", e);
            }

        }
        //update chenq 只生成一条待发送数据   20200805 start
        try {
            StringBuffer sb = new StringBuffer();
            if (allSendedTypes.size() > 0) {

                Iterator<String> it = allSendedTypes.iterator();
                while (it.hasNext()) {
                    String str = it.next();
                    if (sb.length() == 0) {
                        sb.append(str);
                    } else {
                        sb.append("," + str);
                    }

                }


                EdocSendRecord sendRecord = sendEdocManager.getEdocSendRecordById(Long.parseLong(sendRecordId));
                if (newSendRecord == null) {
                    newSendRecord = new EdocSendRecord();
                    newSendRecord.setContentNo(sendRecord.getContentNo());
                    newSendRecord.setCopies(sendRecord.getCopies());
                    newSendRecord.setDocMark(sendRecord.getDocMark());
                    newSendRecord.setDocType(sendRecord.getDocType());
                    newSendRecord.setEdocId(sendRecord.getEdocId());
                    newSendRecord.setIssueDate(sendRecord.getIssueDate());
                    newSendRecord.setIssuer(sendRecord.getIssuer());
                    newSendRecord.setKeywords(sendRecord.getKeywords());
                    newSendRecord.setSecretLevel(sendRecord.getSecretLevel());
                    newSendRecord.setSubject(sendRecord.getSubject());
                    newSendRecord.setUrgentLevel(sendRecord.getUrgentLevel());
                    newSendRecord.setSendUnit(sendRecord.getSendUnit());
                    newSendRecord.setSendUserId(AppContext.currentUserId());
                    newSendRecord.setAssignType(EdocSendRecord.Exchange_Assign_To_Member);//撤销表示，指定交换，交换人为user.getId()
                    newSendRecord.setIsBase(EdocSendRecord.Exchange_Base_NO);//补发表示：非原发文
                    newSendRecord.setId(UUIDLong.longUUID());
                    newSendRecord.setSendedTypeIds(sb.toString());
                    newSendRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    newSendRecord.setExchangeType(sendRecord.getExchangeType());
                    newSendRecord.setExchangeOrgId(sendRecord.getExchangeOrgId());
                    newSendRecord.setExchangeAccountId(sendRecord.getExchangeAccountId());
                    newSendRecord.setExchangeOrgName(sendRecord.getExchangeOrgName());
                    newSendRecord.setStatus(EdocSendRecord.Exchange_iStatus_Send_New_Cancel);
                    newSendRecord.setIsTurnRec(sendRecord.getIsTurnRec());
                }
                newSendRecord.setSendedTypeIds(sb.toString());

                /** 5、生成待发送新数据 */

                if (newAffair == null) {
                    newAffair = new CtpAffair();
                    newAffair.setApp(ApplicationCategoryEnum.exSend.getKey());
                    newAffair.setSubject(newSendRecord.getSubject());
                    newAffair.setFinish(Boolean.FALSE);

                    if (newSendRecord.getUrgentLevel() != null) {
                        newAffair.setImportantLevel(Integer.parseInt(newSendRecord.getUrgentLevel()));
                    }

                    newAffair.setSubState(SubStateEnum.col_normal.key());
                    newAffair.setObjectId(newSendRecord.getEdocId());
                    EdocSummary summary = edocSummaryManager.findById(newSendRecord.getEdocId());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendAccountId, summary.getSendUnitId());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_edocMark, newSendRecord.getDocMark());
                    AffairUtil.addExtProperty(newAffair, AffairExtPropEnums.edoc_sendUnit, newSendRecord.getSendUnit());
                    newAffair.setId(UUIDLong.longUUID());
                    newAffair.setDelete(Boolean.FALSE);
                    newAffair.setState(StateEnum.edoc_exchange_send.key());
                    newAffair.setCreateDate(new Timestamp(System.currentTimeMillis()));
                    newAffair.setReceiveTime(new Timestamp(System.currentTimeMillis()));
                    newAffair.setUpdateDate(new Timestamp(System.currentTimeMillis()));
                    newAffair.setSubObjectId(newSendRecord.getId());
                    newAffair.setSenderId(AppContext.currentUserId());
                    newAffair.setMemberId(AppContext.currentUserId());
                    newAffair.setSubState(SubStateEnum.col_normal.key());
                }

                sendEdocManager.create(newSendRecord);//新生成一条待发送
                affairManager.save(newAffair);//新生成一条待发送事项
            } else {
                LOGGER.info("chenq---没有获取到人员信息");
            }


        } catch (Exception e) {
            LOGGER.error("chenq----只生成一条待发送数据错误 ", e);
        }

        //update chenq 只生成一条待发送数据   20200805 end
        return String.valueOf(EdocSendDetail.Exchange_iStatus_SendDetail_Torecieve);
    }

    /**
     * ajax请求：验证是否可撤销交换
     *
     * @param sendRecordId
     * @param detailId
     * @return
     * @throws Exception
     */
    public String[] checkEdocSendMember(String recieveRecordId) throws BusinessException {
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveRecordId));
        String[] strs = new String[2];
        if (edocRecieveRecord == null) {
            strs[0] = "null";
            return strs;
        }
        EdocSendDetail edocSendDetail = sendEdocManager.getSendRecordDetail(Long.parseLong(edocRecieveRecord.getReplyId()));
        if (edocSendDetail == null) {
            strs[0] = "null";
            return strs;
        }
        EdocSendRecord edocSendRecord = sendEdocManager.getEdocSendRecord(edocSendDetail.getSendRecordId());
        if (edocSendRecord == null) {
            strs[0] = "null";
            return strs;
        }
        if (edocSendRecord.getExchangeType() == EdocSendRecord.Exchange_Send_iExchangeType_Dept) {//部门收发员
            boolean flag = EdocRoleHelper.isDepartmentExchange(edocSendRecord.getSendUserId(), edocSendRecord.getExchangeOrgId());
            strs[0] = String.valueOf(flag);
            if (!flag) {
                String hasMember = EdocRoleHelper.getDepartmentExchangeMember(edocSendRecord.getExchangeOrgId());
                strs[1] = hasMember;
            }
        } else if (edocSendRecord.getExchangeType() == EdocSendRecord.Exchange_Send_iExchangeType_Org) {//内部收发员
            boolean flag = EdocRoleHelper.isAccountExchange(edocSendRecord.getSendUserId(), edocSendRecord.getExchangeOrgId());
            strs[0] = String.valueOf(flag);
            if (!flag) {
                String hasMember = EdocRoleHelper.getAccountExchangeMember(edocSendRecord.getExchangeOrgId());
                strs[1] = hasMember;
            }
        }
        return strs;
    }
    /********************************************公文交换列表       end*************************************************************/

    /**
     * ajax请求：验证是否可撤销交换
     *
     * @param sendRecordId
     * @param detailId
     * @return
     * @throws Exception
     */
    public String[] checkEdocRecieveMember(String recieveRecordId) throws BusinessException {
        EdocRecieveRecord edocRecieveRecord = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveRecordId));
        String[] strs = new String[2];
        if (edocRecieveRecord == null) {
            strs[0] = "null";
            return strs;
        }

        if (edocRecieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Dept) {//部门收发员
            boolean flag = EdocRoleHelper.isDepartmentExchange(edocRecieveRecord.getRecUserId(), edocRecieveRecord.getExchangeOrgId());
            strs[0] = String.valueOf(flag);
            if (!flag) {
                String hasMember = EdocRoleHelper.getDepartmentExchangeMember(edocRecieveRecord.getExchangeOrgId());
                strs[1] = hasMember;
            }
        } else if (edocRecieveRecord.getExchangeType() == EdocRecieveRecord.Exchange_Receive_iAccountType_Org) {//内部收发员
            boolean flag = EdocRoleHelper.isAccountExchange(edocRecieveRecord.getRecUserId(), edocRecieveRecord.getExchangeOrgId());
            strs[0] = String.valueOf(flag);
            if (!flag) {
                String hasMember = EdocRoleHelper.getAccountExchangeMember(edocRecieveRecord.getExchangeOrgId());
                strs[1] = hasMember;
            }
        }
        return strs;
    }

    /**
     * ajax请求：通过登记id 获得签收id(用于收文分发回退)
     *
     * @throws Exception
     */
    public String getRecieveIdByRegisterId(Long registerId) throws BusinessException {
        EdocRegister register = edocRegisterManager.getEdocRegister(registerId);
        String recieveId = "-1";
        if (register != null) {
            recieveId = String.valueOf(register.getRecieveId());
        }
        return recieveId;
    }


    /********************************************公文交换列表       end*************************************************************/

    public String cuiban(String detailIdStr, String cuibanInfo) throws BusinessException {
        String msg = "ok";
        long detailId = Long.parseLong(detailIdStr);
        try {
            EdocSendDetail detail = sendEdocManager.getSendRecordDetail(detailId);
            if (detail.getStatus() != EdocSendDetail.Exchange_iStatus_SendDetail_Torecieve) {
                return "fail";
            }

            //催办次数加1
            detail.setCuibanNum(detail.getCuibanNum() + 1);
            sendEdocManager.updateSendRecordDetail(detail);

            //给签收人员发送催办信息
            //签收人员有哪些 (单人或者所有竞争人员)
            EdocSendRecord send = sendEdocManager.getEdocSendRecordByDetailId(detailId);
            EdocRecieveRecord recieve = null;
            try {
                recieve = recieveEdocManager.getReceiveRecordByReplyId(detailId);
            } catch (Exception e) {
                LOGGER.error("催办时抛出异常", e);
            }

            List<CtpAffair> affairs = affairManager.getAffairs(ApplicationCategoryEnum.exSign, send.getEdocId());

            User user = AppContext.getCurrentUser();

            List<MessageReceiver> receivers = new ArrayList<MessageReceiver>();
            for (CtpAffair af : affairs) {
                if (!af.isDelete() && af.getSubObjectId().longValue() == recieve.getId().longValue()) {
                    receivers.add(new MessageReceiver(af.getId(), af.getMemberId(), "message.link.exchange.receive.hasten", af.getSubObjectId().toString(), user.getId()));
                }
            }

            String key = "exchange.sign.cuiban";
            String userName = user.getName();

            if (null != receivers && receivers.size() > 0) {
                MessageContent msgContent = new MessageContent(key, affairs.get(0).getSubject(), userName, cuibanInfo);
                msgContent.setResource("com.seeyon.v3x.exchange.resources.i18n.ExchangeResource");
                if (null != affairs.get(0).getTempleteId()) {
                    msgContent.setTemplateId(affairs.get(0).getTempleteId());
                }
                userMessageManager.sendSystemMessage(msgContent,
                        ApplicationCategoryEnum.exSign, user.getId(), receivers, EdocMessageFilterParamEnum.exchange.key);
            }
        } catch (BusinessException e) {
            msg = "fail";
            LOGGER.error("催办时抛出异常", e);
        }
        return msg;
    }

    public int findEdocExchangeRecordCount(int type, Map<String, Object> condition) throws BusinessException {
        String accountIds = EdocRoleHelper.getUserExchangeAccountIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        String departIds = EdocRoleHelper.getUserExchangeDepartmentIds(V3xOrgEntity.VIRTUAL_ACCOUNT_ID);
        condition.put("accountIds", accountIds);
        condition.put("departIds", departIds);
        String listType = (String) condition.get("listType");

        List<Integer> statusList = new ArrayList<Integer>();
        String substate = EdocNavigationEnum.EdocV5ListTypeEnum.getSubStateName(listType);
        if (Strings.isNotBlank(substate)) {
            String[] substates = substate.split(",");
            for (String s : substates) {
                statusList.add(Integer.parseInt(s));
            }
        }
        condition.put("statusList", statusList);
        if (type == EdocNavigationEnum.LIST_TYPE_EX_SEND) {
            return sendEdocManager.findEdocSendRecordCount(type, condition);
        } else {
            return recieveEdocManager.findEdocRecieveRecordCount(type, condition);
        }


    }

    /**
     * 签收回退时，先通过ajax进行校验，当签收对象是已签收状态时，就不能进行回退了
     * 操作场景：当同一个单位下的交换员签收后，另个交换员点在已经打开的签收中点回退时
     *
     * @param receiveId
     * @return
     */
    public String receiveRecordIsCanStepBack(String receiveId) {
        EdocRecieveRecord record = this.getReceivedRecord((Long.parseLong(receiveId)));
        String alertNote = "yes";
        if (null == record) {
            alertNote = ResourceUtil.getString("exchange.send.withdrawed");
        } else if (record.getStatus() == EdocRecieveRecord.Exchange_iStatus_Recieved || record.getStatus() == EdocRecieveRecord.Exchange_iStatus_Registered) {//已签收
            alertNote = ResourceUtil.getString("exchange.receiveRecord.receive.already");
        }
        return alertNote;
    }

    @Override
    public String checkRegisterByRegisterEdocId(Long registerId) throws BusinessException {
        EdocRegister register = edocRegisterManager.getEdocRegister(registerId);
        long registerUserId = register.getRegisterUserId();
        boolean hasRegisterPrivilege = false;
        boolean hasOtherRegister = false;
        String resource = "F07_recRegister";
        if (EdocHelper.isG6Version()) {
            resource = "F07_recListRegistering";
        }
        boolean checkRole = privilegeManager.checkByReourceCode(resource, registerUserId, Long.valueOf(register.getOrgAccountId()));
        if (checkRole) {
            hasOtherRegister = true;
            hasRegisterPrivilege = true;
        } else {
            List<V3xOrgMember> members = privilegeManager.getMembersByResource(resource, Long.valueOf(register.getOrgAccountId()));
            if (Strings.isNotEmpty(members)) {
                hasOtherRegister = true;
            }
        }
        return hasRegisterPrivilege + "," + hasOtherRegister;
    }

    @Override
    public String checkTurnRec(String summaryId) throws BusinessException {
        Long edocId = 0L;
        if (Strings.isNotBlank(summaryId)) {
            edocId = Long.valueOf(summaryId);
        }
        EdocExchangeTurnRec turnRec = edocExchangeTurnRecManager.findEdocExchangeTurnRecByEdocId(edocId);
        String retValue = "true";
        if (turnRec == null) {
            retValue = "false";
        }
        return retValue;
    }

}











