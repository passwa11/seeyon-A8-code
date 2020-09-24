package com.seeyon.ctp.common.usermessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.seeyon.ctp.util.annotation.AjaxAccess;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.seeyon.apps.uc.api.UcApi;
import com.seeyon.apps.xkjt.manager.XkjtManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ServerState;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.config.PropertiesLoader;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.Constants.login_sign;
import com.seeyon.ctp.common.constants.CustomizeConstants;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.usermessage.Ent_UserMessage;
import com.seeyon.ctp.common.po.usermessage.UserHistoryMessage;
import com.seeyon.ctp.common.security.SecurityHelper;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.common.task.AsynchronousBatchTask;
import com.seeyon.ctp.common.usermessage.Constants.UserMessage_TYPE;
import com.seeyon.ctp.common.usermessage.anonymous.MessageSenderUtil;
import com.seeyon.ctp.common.usermessage.dao.UserMessageDAO;
import com.seeyon.ctp.common.usermessage.util.LoginRemindCacheUtil;
import com.seeyon.ctp.login.CurrentUserToSeeyonApp;
import com.seeyon.ctp.login.online.OnlineManager;
import com.seeyon.ctp.login.online.OnlineRecorder;
import com.seeyon.ctp.login.online.OnlineUser;
import com.seeyon.ctp.login.online.OnlineUser.LoginInfo;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.JSObject;
import com.seeyon.ctp.util.Strings;

/**
 *
 *
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2007-3-8
 */
public class UserMessageManagerImpl extends AsynchronousBatchTask<Object> implements UserMessageManager {
    private static final String NULL_NAME = "";

	private static Log                 log                       = CtpLogFactory.getLog(UserMessageManagerImpl.class);

    /**
     * 链接地址，不需要做缓存
     */
    private static Map<String, String> messageLinkTypes         = new ConcurrentHashMap<String, String>();

    private UserMessageDAO             userMsgDao;

    private OrgManager                 orgManager;

    private AttachmentManager          attachmentManager;

    private OnlineManager              onLineManager;

    private UcApi                      ucApi;

    boolean                            isSendPersonalMsgOfOnline = false;                                          //个人消息在线是否还需要发送

    /**
     * 消息后面意见的字数，不需要做缓存
     */
    private static int                 commentZishu              = 0;
    private UserAndReferenceUpdater userAndReferenceUpdater = new UserAndReferenceUpdater();

    public void setUcApi(UcApi ucApi) {
        this.ucApi = ucApi;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public void setUserMsgDao(UserMessageDAO dao) {
        userMsgDao = dao;
    }

    public void setAttachmentManager(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    public void setOnlineManager(OnlineManager onLineManager) {
        this.onLineManager = onLineManager;
    }

    public void initMessageState() {
        this.userMsgDao.initMessageState();
    }

    public void init() {
        Properties allProperties = SystemProperties.getInstance().getAllProperties();

        Set<Map.Entry<Object, Object>> entries = allProperties.entrySet();
        for (Iterator<Map.Entry<Object, Object>> iter = entries.iterator(); iter.hasNext();) {
            Map.Entry<Object, Object> entry = iter.next();

            String key = String.valueOf(entry.getKey());

            if (key.startsWith(MessageReceiver.LINK_TYPE_PREFIX)) {
                messageLinkTypes.put(key, String.valueOf(entry.getValue()));
                iter.remove();
            }
        }
        Properties links = PropertiesLoader.load(new File(AppContext.getCfgHome()+"/base/message-link.properties"));
        for (Object o : links.keySet()) {
        	String key = (String) o;
        	messageLinkTypes.put(key, links.getProperty(key));
		}
        Map<String, String> extend = UserMessageLinkExtend.getInstance().getAll();
		log.info(extend.toString());
        messageLinkTypes.putAll(extend);
        initMessageState();
        initMessageLinkConstantsJS();
    }



    public static Map<String, String> getMessageLinkType() {
        return messageLinkTypes;
    }

    public void deleteReadMessage() {
        this.userMsgDao.deleteReadMessage();
    }

    private void initMessageLinkConstantsJS(){
    	/**
		 * TODO:seeyon替换为获取应用contextPath方法
		 */
    	String contextPath = "/seeyon";
    	Map<String, String> message_link_type = this.getMessageLinkType();
		if(message_link_type == null){
			return ;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("var messageLinkConstants = new Properties(); \n");
		Set<Map.Entry<String, String>> entries = message_link_type.entrySet();
		for (Map.Entry<String, String> entry : entries) {
			String link = entry.getValue();
			if(link.indexOf(".do") > 0&&!link.startsWith("javascript")){
				try {
					String[] links = link.split("[?]");

					link = contextPath + links[0] + "?" + links[1];
					sb.append("messageLinkConstants.put(\"").append(entry.getKey()).append("\", \"").append(link).append("\");\n");
				}
				catch (Exception e) {
					log.warn(NULL_NAME, e);
				}
			}
			else{
				sb.append("messageLinkConstants.put(\"").append(entry.getKey()).append("\", \"").append(link).append("\");\n");
			}
		}

		File base = new File(SystemEnvironment.getApplicationFolder());
		File messageLinkJs = new File(base, "messageLinkConstants.js");
		Writer fos = null;
		try{
			fos = new OutputStreamWriter(new FileOutputStream(messageLinkJs, false), "UTF-8");
			fos.write(sb.toString());
            fos.flush();
	    } catch (Exception e) {
	        log.error(e.getLocalizedMessage(),e);;
	    } finally {
	        if (fos != null) {
	            try {
	                fos.close();
	            } catch (IOException e1) {
	            	log.error(e1.getLocalizedMessage(),e1);
	            }
	        }
	    }
    }

    private int getProcessNewUserMessages(long userInternalID, long loginAccountId, StringBuilder o)
            throws BusinessException {
        int count = MessageState.getInstance().getState(userInternalID);
        if (count > 0) {
            try {
            	// List<Ent_UserMessage> ls = userMsgDao.getUnresolvedMessages(userInternalID); //读取最新的
                List<Ent_UserMessage> ls = userMsgDao.getUnresolvedMessagesExceptLoginRemind(userInternalID,false); //读取最新的
                if (ls != null) {
                    o.append("[");
                    int firstMsgNotReadIndex=-1;//记录第一个满足消息未读的索引号,方便后面的拼接消息字符串(支艳强,2017/02/22)
                    for (int i = 0; i < ls.size(); i++) {
                        Ent_UserMessage os = ls.get(i);
                        if(firstMsgNotReadIndex==-1)
                        {
                        	firstMsgNotReadIndex=i;
                        }
                        Long id = os.getId();
                        Long senderId = os.getSenderId();
                        Integer messageType = os.getMessageType();
                        Integer messageCategory = os.getMessageCategory();
                        String messageContent = os.getMessageContent();
                        Integer openType = os.getOpenType();
                        Date creationDate = os.getCreationDate();
                        Long referenceId = os.getReferenceId();
                        Long userHistoryMessageId = os.getUserHistoryMessageId();
                        Integer importantLevel = os.getImportantLevel();
                        String identifier = os.getIdentifier();
                        String linkType = os.getLinkType();
                        if (i>firstMsgNotReadIndex) {
                            o.append(",");
                        }
                        String messageCategroyName = this.getMessageCategoryNameByProduct(messageCategory);
                        o.append("{");
                        o.append("S:\"" + senderId).append("\"");
                        o.append(",T:" + messageType);
                        o.append(",MC:\"" + messageCategroyName).append("\"");
                        o.append(",C:" + JSObject.quote(messageContent));
                        o.append(",I:" + importantLevel);
                        o.append(",O:" + openType);
                        o.append(",AI:" + (Constants.UserMessage_SOURCE_TYPE.AI.ordinal()  == os.getSourceType()));
                        o.append(",D:\"" + creationDate.getTime()).append("\"");
                        o.append(",R:\"" + referenceId).append("\"");
                        o.append(",H:\"" + userHistoryMessageId).append("\"");
                        o.append(",IA:\"" + os.getIsAt()).append("\"");//@我的
                        o.append(",IR:\"" + os.getIsReply()).append("\"");//回复我的
                        o.append(",ITR:\"" + os.getIsTrack()).append("\"");//我跟踪的
                        o.append(",ITM:\"" + os.getIsTemplate()).append("\"");//模板消息
                        o.append(",TMI:\"" + os.getTemplateId()).append("\"");//模板id

						if (senderId != -1) {
							String senderName = showMemberName(senderId);
							o.append(",N:" + JSObject.quote(senderName));
							String accountShortName = getShortNameOfAccount(senderId);
							o.append(",SN:" + JSObject.quote(accountShortName));
						}else{
							String senderName = MessageSenderUtil.getMessageAnonymousName(os);
							if(Constants.UserMessage_SOURCE_TYPE.AI.ordinal()  == os.getSourceType()) {
								senderName = ResourceUtil.getString("application.72.label");
							}
							o.append(",N:" + JSObject.quote(senderName));

						}

                        if (Strings.isNotBlank(linkType)) {
                            o.append(",L:\"" + linkType).append("\"");

                            String openableUrl = messageLinkTypes.get(linkType);
                            String paramIndexStr = null;
                            List<String> paramIndexList = new ArrayList<String>();
                            if(openableUrl != null && openableUrl.trim().length() > 0){
                                int beginIndex = openableUrl.indexOf("v={");
                                int endIndex = 0;
                                if(beginIndex != -1){
                                    beginIndex = beginIndex + 3;
                                    endIndex = openableUrl.indexOf("}", beginIndex);
                                    if(endIndex != -1 && endIndex > beginIndex){
                                        paramIndexStr = openableUrl.substring(beginIndex, endIndex);
                                        if(paramIndexStr.indexOf(",") == -1) {
                                            paramIndexList.add(paramIndexStr.trim());
                                        } else {
                                            String[] paramIndexArray = paramIndexStr.split(",");
                                            for(String paramIndexTmp : paramIndexArray){
                                                paramIndexList.add(paramIndexTmp.trim());
                                            }
                                        }
                                    }
                                }
                            }
                            Map<String, String> paramMap = new LinkedHashMap<String, String>();
                            for (int p = 0; p < 10; p++) {
                                try {
                                    String linkParam = (String) os.getLinkParam(p);
                                    if (linkParam != null) {
                                        paramMap.put(String.valueOf(p), linkParam);
                                        linkParam = JSObject.quote(linkParam);
                                        o.append(",P" + p + ":" + linkParam);
                                		if("message.link.webservice.message".equals(linkType)){
                            				break;
                            			}
                                    } else {
                                        break;
                                    }
                                } catch (Exception e) {
                                    log.error(NULL_NAME,e);
                                }
                            }
                            List<String> vList = new ArrayList<String>();
                            if(paramIndexList.size() > 0){
                                for(String paramIndex : paramIndexList){
                                    String vTmp = paramMap.get(paramIndex);
                                    if(vTmp != null){
                                        vList.add(paramMap.get(paramIndex));
                                    }
                                }
                            }
                            if(vList.size() > 0){
                                if(vList.size() == 1){
                                    o.append(",VC:\"").append(SecurityHelper.func_digest(vList.get(0))).append("\"");
                                } else if(vList.size() == 2){
                                    o.append(",VC:\"").append(SecurityHelper.func_digest(vList.get(0), vList.get(1))).append("\"");
                                } else if(vList.size() == 3){
                                    o.append(",VC:\"").append(SecurityHelper.func_digest(vList.get(0), vList.get(1), vList.get(2))).append("\"");
                                } else if(vList.size() == 4){
                                    o.append(",VC:\"").append(SecurityHelper.func_digest(vList.get(0), vList.get(1), vList.get(2), vList.get(3))).append("\"");
                                } else if(vList.size() == 5){
                                    o.append(",VC:\"").append(SecurityHelper.func_digest(vList.get(0), vList.get(1), vList.get(2), vList.get(3), vList.get(4))).append("\"");
                                }
                            }
                        }

                        boolean isHasAttachments = IdentifierUtil.lookupInner(identifier,
                                Constants.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1');
                        if (isHasAttachments) {
                            List<Attachment> attachment = this.attachmentManager.getByReference(id, id);
                            if (attachment != null && !attachment.isEmpty()) {
                                o.append(",A:[");
                                for (int a = 0; a < attachment.size(); a++) {
                                    Attachment att = attachment.get(a);

                                    if (a > 0) {
                                        o.append(",");
                                    }

                                    o.append("{");
                                    o.append("N:" + JSObject.quote(att.getFilename()) + ",");
                                    o.append("S:" + String.valueOf(att.getSize()) + ",");
                                    o.append("U:\"" + String.valueOf(att.getFileUrl()) + "\",");
                                    o.append("I:\"" + att.getIcon() + "\",");
                                    o.append("D:\"" + String.valueOf(att.getCreatedate().getTime()) + "\"");
                                    o.append("}");
                                }
                                o.append("]");
                            }
                        }

                        o.append("}");
                    }

                    o.append("]");
                }
            } catch (Exception e) {
                log.error(NULL_NAME,e);
                throw new BusinessException(e);
            }
            MessageState.getInstance().setNoMessageState(userInternalID);
        }

        return count;
    }
    /**
     * 根据版本进行分类识别
     * @return
     */
    private String getMessageCategoryNameByProduct(Integer messageCategory){
    	String messageCategroyName = NULL_NAME;
    	if(ApplicationCategoryEnum.doc.ordinal() == messageCategory.intValue()){
    		String productId = AppContext.getSystemProperty("system.ProductId");
    		if("3".equals(productId) || "4".equals(productId)){
    	        //政务版
    			messageCategroyName = messageCategory + "|" + ResourceUtil.getString("system.menuname.DocumentManagement");
    		}else if("0".equals(productId) || "7".equals(productId)){
    			//A6
    			messageCategroyName = messageCategory + "|" + ResourceUtil.getString("system.menuname.DocCenter");
    		}else{
    			messageCategroyName = messageCategory + "|" + Functions.getApplicationCategoryName(messageCategory, null);
    		}
    	}else{
    		messageCategroyName = messageCategory + "|" + Functions.getApplicationCategoryName(messageCategory, null);
    	}
    	return messageCategroyName;
    }
    /**
     * 获取用户未读消息个数
     */
    private int getNotReadSystemMessageCount(long userInternalID) throws BusinessException {
        return userMsgDao.getNotReadSystemMessageCount(userInternalID);
    }

    @AjaxAccess
    public void updateSystemMessageState(long id) throws BusinessException {
        super.addTask(id);
        //更新UC消息
        /* TODO 有性能问题，先注掉，要改成批量的
        if (ucApi != null) {
            ucApi.syncUpdateMessageState(AppContext.currentUserId(), id);
        }
        */
    }
    
    public String getABThreadName(){
        return "HisMsgState";
    }
    
    public int getIntervalTime() {
        return 15;
    }
    
    protected void doBatch(List<Object> objs){
    	Object firstObject= null;
    	if(null!=objs && !objs.isEmpty()){
    		firstObject= objs.get(0);
    	}
        try {
        	if(firstObject instanceof Long){
        		List<Long> ids= new ArrayList<Long>();
        		for (Object object : objs) {
        			ids.add((Long)object);
        		}
        		userMsgDao.updateSystemMessageState(ids);
        	}else if(firstObject instanceof Ent_UserMessage){
        		for (Object object : objs) {
        			Ent_UserMessage message= (Ent_UserMessage)object;
        			this.saveMessage(message);
				}
        	}else if(firstObject instanceof UserHistoryMessage){
        		for (Object object : objs) {
        			if( object instanceof UserHistoryMessage ){
	        			UserHistoryMessage message= (UserHistoryMessage)object;
	        			List<UserHistoryMessage> historyMsgs= new ArrayList<UserHistoryMessage>(1);
	        			historyMsgs.add(message);
	        			this.savePatchHistory(historyMsgs);
        			}
				}
        	}
        }
        catch (BusinessException e) {
            log.error("", e);
        }
    }

    public void updateSystemMessageStateByCategory(long userId, int messageCategory) throws BusinessException {
        userMsgDao.updateSystemMessageStateByCategory(userId, messageCategory);
    }    
    public void updateSystemMessageStateByUser(long userInternalID) throws BusinessException {
        userMsgDao.updateSystemMessageStateByUser(userInternalID);
        //更新UC消息
        /* TODO 有性能问题，先注掉，要改成批量的
        if (ucApi != null) {
            ucApi.syncUpdateMessageState(userInternalID);
        }
        */
    }

    /**
     * 消息显示发送者姓名-不显示单位简称
     * @param senderId
     */
    private String showMemberName(long senderId) {
       return Functions.showMemberNameOnly(Long.valueOf(senderId));
    }

	/**
	 * 根据消息发送者senderId获取单位简称
	 *
	 * @param senderId
	 * @return (单位简称)
	 */
	private String getShortNameOfAccount(long senderId) {
		try {
			V3xOrgMember member = orgManager.getMemberById(senderId);

			if (member == null || !(Boolean) SysFlag.selectPeople_showAccounts.getFlag()) {
				return NULL_NAME;
			}

			User user = AppContext.getCurrentUser();
			if (!user.getLoginAccount().equals(member.getOrgAccountId())) { // 不是同一个单位的
				V3xOrgAccount account = this.orgManager.getAccountById(member.getOrgAccountId());
				if(account==null){
				    return NULL_NAME;
				}
				if (account.isGroup()) {
					return NULL_NAME;
				}

				return "(" + account.getShortName() + ")";
			}
		} catch (Exception e) {
			log.error("获取消息发送者[" + senderId + "]的单位简称时出错：", e);
		}
		return NULL_NAME;
	}

    public List<UserHistoryMessage> getAllSystemMessages(long userInternalID, String condition, String textfield,
            String textfield1) throws BusinessException {
        return getAllSystemMessages(userInternalID, condition, textfield, textfield1, true);
    }

    public List<UserHistoryMessage> getAllSystemMessages(long userInternalID, String condition, String textfield,
            String textfield1, Boolean isPage) throws BusinessException {
        return getAllSystemMessages(userInternalID, condition, textfield, textfield1, isPage, null);
    }

    public List<UserHistoryMessage> getAllSystemMessages(long userInternalID, String condition, String textfield,
            String textfield1, Boolean isPage, String readType) throws BusinessException {
        try {

            List<UserHistoryMessage> ls = userMsgDao.getAllSystemMessages(userInternalID, condition, textfield, textfield1, isPage, readType);
            if (ls != null && !ls.isEmpty()) {
                for (UserHistoryMessage message : ls) {
                    if (message.getSenderId() != -1) {
                        String senderAccountShortname = null;
                        Long senderId = message.getSenderId();

                        String senderName = Strings.escapeNULL(this.showMemberName(senderId), "-");

                        message.setSenderName(senderName);
                        message.setSenderAccountShortname(senderAccountShortname);
                        //做一些事情 得到连接地址和打开方式????
                    }
                }

                return ls;
            }
        } catch (Exception e) {
            log.error(NULL_NAME,e);
            throw new BusinessException(e);
        }

        return null;
    }

    public String toJSLink(UserHistoryMessage message) {
        StringBuffer sb = new StringBuffer();
        sb.append(message.getLinkType());
        if (!Strings.isNotBlank(message.getLinkParam0())) {
            sb.append("|").append(message.getLinkParam0());
        }
        if (!Strings.isNotBlank(message.getLinkParam1())) {
            sb.append("|").append(message.getLinkParam1());
        }
        if (!Strings.isNotBlank(message.getLinkParam2())) {
            sb.append("|").append(message.getLinkParam2());
        }
        if (!Strings.isNotBlank(message.getLinkParam3())) {
            sb.append("|").append(message.getLinkParam3());
        }
        if (!Strings.isNotBlank(message.getLinkParam4())) {
            sb.append("|").append(message.getLinkParam4());
        }
        if (!Strings.isNotBlank(message.getLinkParam5())) {
            sb.append("|").append(message.getLinkParam5());
        }
        if (!Strings.isNotBlank(message.getLinkParam6())) {
            sb.append("|").append(message.getLinkParam6());
        }
        if (!Strings.isNotBlank(message.getLinkParam7())) {
            sb.append("|").append(message.getLinkParam7());
        }
        if (!Strings.isNotBlank(message.getLinkParam8())) {
            sb.append("|").append(message.getLinkParam8());
        }
        if (!Strings.isNotBlank(message.getLinkParam9())) {
            sb.append("|").append(message.getLinkParam9());
        }
        return sb.toString();
    }

    public List<UserHistoryMessage> getAllPersonMessages(long userInternalID, String condition, String textfield,
            String textfield1) throws BusinessException {
        return getAllPersonMessages(userInternalID, condition, textfield, textfield1, true);
    }

    public List<UserHistoryMessage> getAllPersonMessages(long userInternalID, String condition, String textfield,
            String textfield1, Boolean isPage) throws BusinessException {
        List<UserHistoryMessage> list = userMsgDao.getAllPersonMessages(userInternalID, condition, textfield,
                textfield1, isPage);
        this.setNameForMessage(list, userInternalID);
        return list;
    }

    public int getThisHistoryMessage(Constants.UserMessage_TYPE type, Long userId, Long id, String createDate)
            throws BusinessException {
        return userMsgDao.getThisHistoryMessage(type, userId, id, createDate);
    }

    public List<UserHistoryMessage> getThisHistoryMessage(Constants.UserMessage_TYPE type, Long userId, Long id,
            String createDate, int start, int pageSize) throws BusinessException {
        List<UserHistoryMessage> list = userMsgDao.getThisHistoryMessage(type, userId, id, createDate, start, pageSize,
                false);
        this.setNameForMessage(list, userId);
        return list;
    }

    public List<UserHistoryMessage> getHistoryMessageTree(Long userId) throws BusinessException {
        return userMsgDao.getHistoryMessageTree(userId);
    }

    public List<UserHistoryMessage> getAllHistoryMessage(Constants.UserMessage_TYPE type, Long userId, Long id,
            boolean search, String area, String time, String content) throws BusinessException {
        return this.getAllHistoryMessage(type, userId, id, search, area, time, content, true);
    }

    public List<UserHistoryMessage> getAllHistoryMessage(Constants.UserMessage_TYPE type, Long userId, Long id,
            boolean search, String area, String time, String content, boolean isPage) throws BusinessException {
        List<UserHistoryMessage> list = userMsgDao.getAllHistoryMessage(type, userId, id, search, area, time, content,
                isPage);
        this.setNameForMessage(list, userId);
        return list;
    }

    public void deleteMessage(Long userId, String deleteType, List<Long> ids) throws BusinessException {
        userMsgDao.deleteMessage(userId, deleteType, ids);
    }

    /**
     * 为消息设置发送者姓名、接收者姓名
     */
    private void setNameForMessage(List<UserHistoryMessage> list, Long userId) throws BusinessException {
        try {
            if (CollectionUtils.isNotEmpty(list)) {
                Long loginAccountId = orgManager.getMemberById(userId).getOrgAccountId();
                String systemName = ResourceBundleUtil.getString(
                        "com.seeyon.ctp.organization.resources.i18n.OrganizationResources",
                        "org.account_form.systemAdminName.value");
                String auditName = ResourceBundleUtil.getString(
                        "com.seeyon.ctp.organization.resources.i18n.OrganizationResources", "org.auditAdminName.value");
                for (UserHistoryMessage message : list) {
                    Long senderId = message.getSenderId();
                    if (senderId != -1) {
                        String senderName = "-";
                        if (senderId.equals(V3xOrgEntity.CONFIG_SYSTEM_ADMIN_ID)) {
                            senderName = systemName;
                        } else if (senderId.equals(V3xOrgEntity.CONFIG_AUDIT_ADMIN_ID)) {
                            senderName = auditName;
                        } else if (senderId.equals(V3xOrgEntity.CONFIG_SYSTEM_AUTO_TRIGGER_ID)) {
                            senderName = ResourceBundleUtil.getString(
                                    "com.seeyon.ctp.organization.resources.i18n.OrganizationResources",
                                    "org.system.auto.trigger");
                        } else {
                            try {
                                V3xOrgMember member = orgManager.getMemberById(senderId);
                                if (member != null) {
                                    senderName = member.getName();
                                    if (!loginAccountId.equals(member.getOrgAccountId())) {
                                        V3xOrgAccount account = orgManager.getAccountById(member.getOrgAccountId());
                                        if (account != null) {
                                            message.setSenderAccountShortname(account.getShortName());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.error("读取消息发送者姓名[" + senderId + "]出错：", e);
                            }
                        }
                        message.setSenderName(senderName);
                    }
                    if (message.getReceiverId() != -1) {
                        message.setReceiverName(showMemberName(message.getReceiverId()));
                    }
                }
            }
        } catch (Exception e) {
            log.error(NULL_NAME,e);
            throw new BusinessException(e);
        }
    }

    public void saveMessage(Ent_UserMessage msg) throws BusinessException {
        this.userMsgDao.saveMessage(msg);
    }

    public void sendPersonMessage(String content, long senderId, long... receiverIds) throws BusinessException {
        if (receiverIds == null || receiverIds.length == 0) {
            return;
        }

        List<Long> receivers = new ArrayList<Long>(receiverIds.length);
        for (long receiverId : receiverIds) {
            receivers.add(receiverId);
        }

        this.sendPersonMessage(Constants.UserMessage_TYPE.PERSON, -1L, content, senderId, receivers, null);
    }

    public void sendPersonMessage(Constants.UserMessage_TYPE messageType, Long referenceId, String content,
            long senderId, List<Long> receiverIds, String creationDate) throws BusinessException {
        this.sendIMMessage(0, messageType, referenceId, content, senderId, receiverIds, creationDate);
    }

    public void sendIMMessage(int type, Constants.UserMessage_TYPE messageType, Long referenceId, String content,
            long senderId, List<Long> receiverIds, String creationDate) throws BusinessException {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return;
        }

        List<MessageReceiver> mreceivers = new ArrayList<MessageReceiver>(receiverIds.size());

        for (Long receiverId : receiverIds) {
            MessageReceiver receiver = new MessageReceiver(referenceId, receiverId);
            mreceivers.add(receiver);
        }

        Date creationDatetime = null;
        if (StringUtils.isNotBlank(creationDate)) {
            creationDatetime = Datetimes.parseDatetime(creationDate);
        } else {
            creationDatetime = new Date();
        }

        UserMessage userMessage = new UserMessage(senderId, ApplicationCategoryEnum.communication.key(), messageType,
                MessageContent.get(content), 0, creationDatetime, mreceivers);

        if (type != 0) {
            userMessage.setSendType(type);
        }
        this.sendMessage(userMessage);
    }

    public void sendSystemMessage(MessageContent content, ApplicationCategoryEnum messageCategroy, long senderId,
            MessageReceiver receiver, Object... messageFilterArgs) throws BusinessException {
        Collection<MessageReceiver> mreceivers = new ArrayList<MessageReceiver>(1);

        mreceivers.add(receiver);

        this.sendSystemMessage(content, messageCategroy, senderId, mreceivers, messageFilterArgs);
    }

    public void sendSystemMessage(MessageContent content, ApplicationCategoryEnum messageCategroy, long senderId,
            Collection<MessageReceiver> receivers, Object... messageFilterArgs) throws BusinessException {
        this.sendSystemMessage(content, messageCategroy.key(), senderId, receivers, messageFilterArgs);
    }

    public void sendSystemMessage(MessageContent content, int messageCategroy, long senderId, Date creationDate,
            Collection<MessageReceiver> receivers, Object... messageFilterArgs) throws BusinessException {
        if (receivers == null || receivers.isEmpty()) {
            return;
        }

        UserMessage userMessage = new UserMessage(senderId, messageCategroy, Constants.UserMessage_TYPE.SYSTEM,
                content, 0, creationDate, receivers, messageFilterArgs);

        this.sendMessage(userMessage);
    }

    public void sendSystemMessage(MessageContent content, int messageCategroy, long senderId,
            Collection<MessageReceiver> receivers, Object... messageFilterArgs) throws BusinessException {
        this.sendSystemMessage(content, messageCategroy, senderId, new Date(), receivers, messageFilterArgs);
    }

    public void sendMessage(UserMessage userMessage) throws BusinessException {
        //		try {
        //			this.taskManager
        //					.sendTaskToQueue(UserMessageTask.class, userMessage);
        //		}
        //		catch (BusinessException e) {
        //			throw new BusinessException(e);
        //		}

        UserMessageWorker.getInstance().addMessage(userMessage);
    }

    /**
     * 获取等待解析的队列长度
     * @return
     */
    public int getWaitingParseQLength() {
        return UserMessageWorker.getInstance().getQueueLengh();
    }

    /**
     * 获取等待入库的队列长度
     * @return
     */
    public int getWaitingSaveQLength() {
        return UserHistoryMessageTask.getInstance().getQueueLength();
    }

    /**
     * 用户消息是否被缓存
     * @param userInternalID
     * @return
     */
    public boolean isCachedUserMessage(long userInternalID) {
        return MessageState.getInstance().isUserCached(userInternalID);
    }

    /**
     * 消息缓存中已缓存的用户个数
     * @return
     */
    public int getCachedUserCount() {
        return MessageState.getInstance().getCachedUserCount();
    }

    public void removeAllMessages(long userInternalID, int messageType) throws BusinessException {
        this.userMsgDao.removeAllMessages(userInternalID, messageType);
    }

    public List<Ent_UserMessage> getUnresolvedMessagesForMB(final long userInternalID) throws BusinessException {
        return this.userMsgDao.getUnresolvedMessagesForMB(userInternalID);
    }

    @SuppressWarnings("unchecked")
    public Map countMessage() throws BusinessException {
        Map<String, Vector<Integer>> countMap = new HashMap<String, Vector<Integer>>();
        Vector<Integer> systemVec = new Vector<Integer>();
        Vector<Integer> personVec = new Vector<Integer>();
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        //本月系统消息
        Integer systemMonth = this.userMsgDao.countMessageByTime("messageType",
                String.valueOf(UserMessage_TYPE.SYSTEM.ordinal()),
                Datetimes.formatDate(Datetimes.getFirstDayInMonth(curTime)),
                Datetimes.formatDate(Datetimes.getLastDayInMonth(curTime)));
        systemVec.add(systemMonth);
        //本季度系统消息
        Integer systemQuarter = this.userMsgDao.countMessageByTime("messageType",
                String.valueOf(UserMessage_TYPE.SYSTEM.ordinal()),
                Datetimes.formatDate(Datetimes.getFirstDayInSeason(curTime)),
                Datetimes.formatDate(Datetimes.getLastDayInSeason(curTime)));
        systemVec.add(systemQuarter);
        //本年系统消息
        Integer systemYear = this.userMsgDao.countMessageByTime("messageType",
                String.valueOf(UserMessage_TYPE.SYSTEM.ordinal()),
                Datetimes.formatDate(Datetimes.getFirstDayInYear(curTime)),
                Datetimes.formatDate(Datetimes.getLastDayInYear(curTime)));
        systemVec.add(systemYear);
        //本月个人消息
        Integer personMonth = this.userMsgDao.countMessageByTime("messageType",
                String.valueOf(UserMessage_TYPE.PERSON.ordinal()),
                Datetimes.formatDate(Datetimes.getFirstDayInMonth(curTime)),
                Datetimes.formatDate(Datetimes.getLastDayInMonth(curTime)));
        personVec.add(personMonth);
        //本季度个人消息
        Integer personQuarter = this.userMsgDao.countMessageByTime("messageType",
                String.valueOf(UserMessage_TYPE.PERSON.ordinal()),
                Datetimes.formatDate(Datetimes.getFirstDayInSeason(curTime)),
                Datetimes.formatDate(Datetimes.getLastDayInSeason(curTime)));
        personVec.add(personQuarter);
        //本年个人消息
        Integer personYear = this.userMsgDao.countMessageByTime("messageType",
                String.valueOf(UserMessage_TYPE.PERSON.ordinal()),
                Datetimes.formatDate(Datetimes.getFirstDayInYear(curTime)),
                Datetimes.formatDate(Datetimes.getLastDayInYear(curTime)));
        personVec.add(personYear);
        countMap.put("system", systemVec);
        countMap.put("person", personVec);
        return countMap;
    }

    public void removeMessage(String startTime, String endTime) throws BusinessException {
        this.userMsgDao.removeMessage(startTime, endTime);
    }

    public void removeMessage(String condition, Long longfield) throws BusinessException {
        this.userMsgDao.removeMessage(condition, longfield);
    }

    public List<String> getMessageAttachmentNames(long senderId, long recieverId, int size) {
        return this.userMsgDao.getMessageAttachmentNames(senderId, recieverId, size);
    }

    public String getNewMessagesAndOnlineSize() {
        User user = AppContext.getCurrentUser();
        this.onLineManager.updateOnlineState(user);
        StringBuilder result = new StringBuilder();

        try {
            StringBuilder userMessages = new StringBuilder();
            int count = this.getProcessNewUserMessages(user.getId(), user.getLoginAccount(), userMessages);
            result.append("{");
            result.append("N:" + onLineManager.getOnlineNumber());
            if (count > 0) {
                //性能优化:去掉右下角系统消息的未读数量
                //int notReadCount = this.getNotReadSystemMessageCount(user.getId());
                result.append(",M:" + userMessages);
                result.append(",C:" + count);
                //result.append(",R:" + notReadCount);
            }
            result.append(",I:'" + user.getId() + "'");
            result.append(",K:'" + Strings.escapeJavascript( user.getName()) + "'");
            result.append("}");
        } catch (Exception e) {
            log.error("获得异常:", e);
        }

        return result.toString();
    }

    /* (non-Javadoc)
     * @see com.seeyon.ctp.common.usermessage.UserMessageManager#countNewMessage(java.lang.Long)
     */
    public Integer[] countNewMessage(Long memberId) throws BusinessException {
        return userMsgDao.countNewMessage(memberId);
    }

    public void setMessageReadedSate(Long memberId, int msgTypte) {
        userMsgDao.setMessageReadedSate(memberId, msgTypte);
    }

    public static int getCommentZishu() {
        return commentZishu;
    }

    public static void setCommentZishu(int commentZishu) {
        UserMessageManagerImpl.commentZishu = commentZishu;
    }

    public String getUserOnlineMessage() throws BusinessException {
        User user = AppContext.getCurrentUser();
        // 在线状态
        String message1 = CurrentUserToSeeyonApp.getUserOnlineMessage();

        if (message1 != null) {
            //记录退出日志
            if (user != null) {
                OnlineRecorder.logoutUser(user);
            }

            return "[LOGOUT]" + message1;
        }

        ServerState serverState = ServerState.getInstance();
        if (serverState.isShutdown()) {
            if (serverState.isForceLogout()) {
                if (user != null) {
                    OnlineRecorder.logoutUser(user);
                }

                //强制下线
                String _message = ResourceUtil.getString("ServerState.shutdown", serverState.getComment());

                return "[LOGOUT]" + _message;
            } else if (serverState.isShutdownWarn(user.getId())) {
                //给出警告
                int second = serverState.getShutdownTime();
                String _message = ResourceUtil.getString("ServerState.shutdown.warn", second / 60, second % 60,
                        serverState.getComment());
                return "[LOGWARN]" + _message;
            }
        }
        CacheMap<String, String>  loginRemindCache= LoginRemindCacheUtil.getLoginRemindCache();
		if (!loginRemindCache.isEmpty()) {
			String mobileMark = loginRemindCache.get("mobileLogin-" + user.getLoginName());
			if (Strings.isNotBlank(mobileMark) && "mobileLogin".equals(mobileMark)) {// 移动端进行了登录
				// 移除属性
				loginRemindCache.remove("mobileLogin-" + user.getLoginName());
				String mobileLoginRemind = user.getCustomize(CustomizeConstants.MOBILE_LOGIN_REMIND);
				//默认弹出提醒，如果未设置则弹出提示
				String isRemind = "enable";
				if (Strings.isNotBlank(mobileLoginRemind) && "disable".equals(mobileLoginRemind)) {
					isRemind = "disable";
				}
				OnlineUser onlineUser = OnlineRecorder.getOnlineUser(AppContext.getCurrentUser());
				Map<login_sign, LoginInfo> loginInfoMapper = onlineUser.getLoginInfoMap();
				String mobileType = "";
				String loginTime = "";
				for (Map.Entry entity : loginInfoMapper.entrySet()) {
					login_sign key = (login_sign) entity.getKey();
					if (key != null && login_sign.phone.name().equals(key.name())) {// 移动类型
						OnlineUser.LoginInfo value = (OnlineUser.LoginInfo) entity.getValue();
						if(value != null && value.getLoginTime() != null){
							mobileType = value.getLoginType();
							loginTime = Datetimes.formatDatetimeWithoutSecond(value.getLoginTime());
						}
					}
				}
				//如果需要提醒
				if("enable".equals(isRemind) && Strings.isNotBlank(mobileType) && Strings.isNotBlank(loginTime)){
					return "[MOBILElOGIN]_" + isRemind + "_" + mobileType+"_"+loginTime;
				}
			}}

        return this.getNewMessagesAndOnlineSize();
    }

    @Override
    public void updateMessageFlagByZhixin(List<Long> messageIdArray) throws Exception {
        userMsgDao.updateMessageFlayByZhixin(messageIdArray);
    }

	@Override
	public Map<String, String> getMessageLinkTypes(){
		return getMessageLinkType();
	}

	public void savePatchHistory(List<UserHistoryMessage> ms){
		userMsgDao.savePatchHistory(ms);
	}
	
	@Override
    public void updateSystemMessageStateByUserAndReference(long userInternalID, long referenceId) {
        userAndReferenceUpdater.addTask(new Long[]{userInternalID,referenceId});
    }
    class UserAndReferenceUpdater extends AsynchronousBatchTask<Long[]>{
        @Override
        public int getIntervalTime() {
            return 10;
        }
        @Override
        protected void doBatch(List<Long[]> e) {
            userMsgDao.updateSystemMessageStateByUserAndReference(e);
        }
        
    }   

	@Override
	public void processPcAndMobileOnlineAtSameTime(Long userId, String pLang, String userAgentFrom,
			Map<login_sign, LoginInfo> loginInfoMapper,String loginType) {
		try{
			if (loginInfoMapper != null && com.seeyon.ctp.common.constants.Constants.login_useragent_from.pc.name().equals(userAgentFrom)) {// 如果当前登录是pc
				pLang = Strings.escapeNULL(pLang, "zh_CN");
				Locale locale = LocaleContext.parseLocale(pLang);
				String content = ResourceUtil.getStringByParams(locale,"pc.login.remind");
				for (Map.Entry entity : loginInfoMapper.entrySet()) {
					com.seeyon.ctp.common.constants.Constants.login_sign key = (com.seeyon.ctp.common.constants.Constants.login_sign) entity.getKey();
					OnlineUser.LoginInfo value = (OnlineUser.LoginInfo) entity.getValue();
					if (key != null && login_sign.phone.name().equals(key.name())) { // 若移动端已在线，发送系统消息到移动端
						Ent_UserMessage message = new Ent_UserMessage();
						message.setIdIfNew();
						message.setUserId(userId);
						message.setCreationDate(new Date());
						message.setMessageCategory(ApplicationCategoryEnum.statusRemind.getKey());
						message.setMessageContent(content);
						message.setSenderId(userId);
						message.setOpenType(com.seeyon.ctp.common.usermessage.Constants.PC_LOGIN_REMIND_TYPE);
						// 取移动端类型
						if ("M1".equals(loginType)) {// 类型为M1
							super.addTask(message);
						} else {// 移动端类型不是M1,只保存历史消息(m3使用时直接调历史消息)
							UserHistoryMessage historyMessage = message.toUserHistoryMessage();
							super.addTask(historyMessage);
						}
					}
				}
			}
		}catch(Throwable e){
			log.error("登录时PC和移动端同时在线时的消息提醒", e);
		}
	}

	@Override
	public List<UserHistoryMessage> getSystemHistoryMessages(FlipInfo fi,Map<String, String> params)
			throws BusinessException {
		return userMsgDao.getSystemHistoryMessages(fi,params);
	}

	@Override
	public UserHistoryMessage getUserHistoryMsgById(Long id) throws BusinessException {
		return userMsgDao.getUserHistoryMsgById(id);
	}
}