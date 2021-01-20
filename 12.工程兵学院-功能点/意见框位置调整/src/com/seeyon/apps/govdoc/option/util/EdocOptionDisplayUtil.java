package com.seeyon.apps.govdoc.option.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.form.util.FormUtil;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateFormatFactory;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDateFormatSetEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDateModelSetEnum;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionShowNameTypeEnum;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocOpinion.OpinionType;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionBO;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;
import com.seeyon.v3x.system.signet.domain.V3xSignet;
import com.seeyon.v3x.system.signet.enums.V3xHtmSignatureEnum;
import com.seeyon.v3x.system.signet.manager.SignetManager;
import com.seeyon.v3x.system.signet.manager.V3xHtmDocumentSignatManager;
import com.thoughtworks.xstream.core.util.Base64Encoder;

import www.seeyon.com.utils.DateUtil;

public class EdocOptionDisplayUtil {
	
	private static final Log LOGGER = LogFactory.getLog(EdocOptionDisplayUtil.class);
	
	private static String getAttitude(Integer opinionType,int attitude){
		
		String attitudeStr=null;
		String attitudeI18nLabel = "";
		
		//查找国际化标签。
		if (attitude > 0) {
			if(OpinionType.backOpinion.ordinal() == opinionType.intValue()){
				attitudeI18nLabel="common.toolbar.stepBack.label";
			}
			//OA-18228 待办中进行终止操作，终止后到已办理查看，态度显示仍然是普通的，不是终止
			else if(OpinionType.stopOpinion.ordinal() == opinionType.intValue()){
			    attitudeI18nLabel="common.stop.label";
			}
			//OA-19935  客户bug验证：流程是gw1，gw11，m1，串发，m1撤销，gw1在待发直接查看（不是编辑态），文单上丢失了撤销的意见  
			else if(OpinionType.repealOpinion.ordinal() == opinionType.intValue()){
                attitudeI18nLabel="edoc.repeal.2.label";
            }
			else{
			    EnumManager enumManager = (EnumManager)AppContext.getBean("enumManagerNew");
				attitudeI18nLabel = enumManager.getEnumItemLabel(EnumNameEnum.collaboration_attitude, 
						Integer.toString(attitude));
			}
		}
		
		//查找用于显示的前台态度字符串
		if (Strings.isNotBlank(attitudeI18nLabel)) {
			
			attitudeStr = ResourceUtil.getString(attitudeI18nLabel);
		} else if ( attitude == com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL) {
			attitudeStr = null;
		}
		
		if (opinionType == EdocOpinion.OpinionType.senderOpinion.ordinal()) attitudeStr = null;
				
		return attitudeStr;
	}
	/**
	 * 取公文单显示的时候的人名 
	 * @param userId
	 * @param proxyName
	 * @param orgManager
	 * @param popUserInfo 是否新增用户信息选项卡连接 true 添加， false 不添加
	 * @return
	 */
	private static String getOpinionUserName(Long userId,String proxyName,OrgManager orgManager, FormOpinionConfig displayConfig,
	        EdocOpinion edocOpinion, boolean popUserInfo, boolean hasSign){
		String doUserName = "";
		try {
			V3xOrgMember member = orgManager.getMemberById(userId);
			doUserName = member.getName();
			
			//只有在文单内才显示签名 
			if(edocOpinion.isBound() && 
					//贵州专版修改——如果是贵州专版，旧公文的意见，落款如果有个人签名，则直接显示，不需要设置
					(OpinionShowNameTypeEnum.SIGN.getValue().equals(displayConfig.getShowNameType()))
			){//电子签名显示方式
			    V3xHtmDocumentSignatManager v3xHtmDocumentSignatManager = (V3xHtmDocumentSignatManager)AppContext.getBean("v3xHtmDocumentSignatManager");
			    V3xHtmDocumentSignature vSign = v3xHtmDocumentSignatManager.getBySummaryIdAffairIdAndType(edocOpinion.getEdocId(), 
	                    edocOpinion.getAffairId(), 
	                    V3xHtmSignatureEnum.HTML_SIGNATURE_EDOC_FLOW_INSCRIBE.getKey());
			    if(vSign != null){
			    	String imgStr = "";
			    	if(FormUtil.isPhoneLogin()){
				    	byte[] imgarry = hex2byte(vSign.getFieldValue());
	                    if (imgarry != null) {
	                    	Base64Encoder encoder = new Base64Encoder();
	                    	imgStr = encoder.encode(imgarry);
	                        imgStr = imgStr.replaceAll("\r\n", "").replace("\\", "");
	                        imgStr = imgStr.replaceAll("\n", "");
	                        String C_sPicDataFormat_Gif = "data:image/gif;base64,";
	                        String C_sPicDataFormat_Png = "data:image/png;base64,";
	                        String C_sPicDataFormat_Jpeg = "data:image/jpeg;base64,";
	                        if(imgStr.indexOf("iVBO") == 0) {//PNG
	                        	imgStr = C_sPicDataFormat_Png + imgStr;
	                		} else if (imgStr.indexOf("R0lG") == 0){//GIF
	                			imgStr = C_sPicDataFormat_Gif + imgStr;
	                		} else {
	                			imgStr = C_sPicDataFormat_Jpeg + imgStr;
	                		}
	                    }
			    	}else{
			    		HttpServletRequest request = AppContext.getRawRequest();
			    		String basePath = request.getScheme() + "://" + Strings.getServerName(request) + ":" +request.getServerPort() + request.getContextPath();
			    		imgStr = basePath + "/edocController.do?method=showInscribeSignetPic&id="+vSign.getId();
			    	}
			        doUserName = "<IMG alt=\""+Strings.toHTML(doUserName)+"\" style=\"vertical-align: text-bottom;max-width:100%\" oncontextmenu=\"return false;\" src=\"" + imgStr + "\" >";
			    }
			}
			
			User user = AppContext.getCurrentUser();
			if (member.getIsAdmin()) {
				// 如果是管理员终止，不显示管理员名字及时间
				doUserName = "<span> " + doUserName + "</span>";
			} else if(popUserInfo && "pc".equals(user.getUserAgentFrom())) {
				doUserName = "<span style='color:#2490f8;' onclick='javascript:showV3XMemberCard(\""
                        + userId
                        + "\",parent.window)'>"
                        + doUserName + "</span>";
			}
			
			if(FormUtil.isPhoneLogin()){
				if(displayConfig.isHideInscriber() && hasSign){//后台配置隐藏落款，移动端隐藏落款信息(无签批不隐藏)。bug20181127071675
					doUserName = "";
				}
			}

			if (!Strings.isBlank(proxyName)) {
				doUserName += ResourceBundleUtil
						.getString(
								"com.seeyon.v3x.edoc.resources.i18n.EdocResource",
								"edoc.opinion.proxy", proxyName);
			}
		} catch (Exception e) {
			LOGGER.error("取公文单显示的时候的人名 抛出异常",e);
		}
		return doUserName;
	}
	
	/**
     * 将16进制字符串转换成byte数组
     * 
     * @Author : xuqiangwei
     * @Date : 2014年11月9日上午1:44:17
     * @param str
     * @return
     */
    public static byte[] hex2byte(String str) { // 字符串转二进制
        if (str == null)
            return null;
        str = str.trim();
        int len = str.length();
        if (len == 0 || len % 2 == 1)
            return null;
        byte[] b = new byte[len / 2];
        try {
            for (int i = 0; i < str.length(); i += 2) {
                b[i / 2] = (byte) Integer.decode("0X" + str.substring(i, i + 2)).intValue();
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }
	
	/**
	 * 将意见对象转化为前台展现的JS串。
	 * @param map
	 * @param signatuers 
	 * @param hasSignature 
	 * @return
	 */
	public static Map<String,Object> convertOpinionToString(Map<String,EdocOpinionModel> map,
			FormOpinionConfig displayConfig, CtpAffair currentAffair, boolean isFromPending,
			List<V3xHtmDocumentSignature> signatuers,List<Comment> commentList) {
		
		Map<Long, StringBuilder> senderAttMap = new HashMap<Long, StringBuilder>();
		List<EdocOpinion> senderOpinions = new ArrayList<EdocOpinion>();
		
		//根据affairId临时存放意见
		int backOpinonType = displayConfig.getBackOpinionType();//是否不显示回退的意见，option过滤了，comment还没过滤
		Map<Long, List<Comment>> commentMap = new HashMap<Long, List<Comment>>();
		if(Strings.isNotEmpty(commentList)) {
			for(Comment comment : commentList) {
				List<Comment> comments = new ArrayList<Comment>();
				if(Strings.isNotEmpty(commentMap.get(comment.getAffairId()))) {
					comments = commentMap.get(comment.getAffairId());
				}
				if(backOpinonType == 1 && "common.toolbar.stepBack.label".equals(comment.getExtAtt3())) {
					continue;
				}
				comments.add(comment);
				commentMap.put(comment.getAffairId(), comments);
			}
		}
		
		Map<String,Object> jsMap = new HashMap<String,Object>();
		String fileUrlStr = "";
		OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
		for(Iterator<String> it = map.keySet().iterator();it.hasNext();){
			//公文单上元素位置
			String element = it.next();
			EdocOpinionModel model = map.get(element);
			List<EdocOpinion> opinions = model.getOpinions();
			for(EdocOpinion opinion : opinions) {
				//取回或者暂存待办的意见回写到意见框中，所以要跳过；其他情况下显示到意见区域
				if (opinion.getOpinionType().intValue() == EdocOpinion.OpinionType.provisionalOpinoin.ordinal()
						|| opinion.getOpinionType().intValue() == EdocOpinion.OpinionType.draftOpinion.ordinal()) 
					continue;
				
				//单位管理员的意见数据不显示在文单中
				try {
					V3xOrgMember member = orgManager.getMemberById(opinion.getCreateUserId());
					if(member.getIsAdmin()) {
						continue;
					}
				} catch (BusinessException e) {
				}
				
				//公文单不显示暂存待办意见
				StringBuilder sb  = new StringBuilder();
				String value = (String)jsMap .get(element);
				if(value!=null){
					sb.append(value);
				}
				boolean hasSignature=false;
				if(signatuers!=null&&signatuers.size()>0){
					for (V3xHtmDocumentSignature signature:signatuers) {
						if(signature!=null){
							if(null!=signature.getAffairId()&&signature.getAffairId().equals(opinion.getAffairId())){
								hasSignature=true;
							}
						}
					}
				}
				String attitude = "";
				String tempAttitude = "";
				switch(opinion.getAttribute()){
				case 1:
					tempAttitude = "haveRead";
					break;
				case 2:
					tempAttitude = "agree";
					break;
				case 3:
					tempAttitude = "disagree";
					break;
				}
				
				//这段代码实属无奈，需要从结构上重构才能完美解决
				Comment comment = null;
				List<Comment> comments = commentMap.get(opinion.getAffairId());
				if(Strings.isNotEmpty(comments)) {
					if(comments.size() == 1) {
						comment = comments.get(0);
					}else {
						/*根据时间匹配，匹配规则：时间完全相同的>误差两秒内任意>任意*/
						//时间完全相同
						for(Comment tempComment : comments) {
							if(tempComment.getCreateDate().equals(opinion.getCreateTime())) {
								comment = tempComment;
								break;
							}
						}
						//误差两秒内任意
						if(comment == null) {
							Date beforeTwoSecond = Datetimes.addSecond(opinion.getCreateTime(), -3);
							Date afterTwoSecond = Datetimes.addSecond(opinion.getCreateTime(), 3);
							for(Comment tempComment : comments) {
								if(Datetimes.between(tempComment.getCreateDate(), beforeTwoSecond, afterTwoSecond, false)) {
									comment = tempComment;
									break;
								}
							}
						}
						//任意
						if(comment == null) {
							comment = comments.get(0);
						}
					}
				}
				
				if(comment != null) {
					if(tempAttitude.equals(comment.getExtAtt4()==null?"":comment.getExtAtt4())) {
						attitude = comment.getExtAtt1I18n();
					}
					//有意见也显示
					if(Strings.isNotBlank(comment.getExtAtt1())) {
						attitude = comment.getExtAtt1I18n();
					}
				}
				
				sb.append(displayOpinionContent(displayConfig, opinion, hasSignature, true,attitude));
				//附件显示
				List<Attachment> tempAtts = null;
				if(null != opinion.getPolicy() && opinion.getPolicy().equals(EdocOpinion.FEED_BACK)){
					Long subOpinionId = opinion.getSubOpinionId();
					if(subOpinionId != null){
						tempAtts = GovdocHelper.getOpinionAttachmentsNotRelationDoc(opinion.getSubEdocId(),subOpinionId);
					}
				}else{
					tempAtts = opinion.getOpinionAttachments();
				}
				if (tempAtts != null&&displayConfig.isShowAtt()) {
					StringBuilder attSb = new StringBuilder();
					attSb.append("<div style='clear:both;'>");
					for (Attachment att : tempAtts) {
						// 不管文件名有多长，显示整体的文件名。yangzd
						//sb.append("<br>");//前端附件使用的是DIV，会自动换行
						fileUrlStr += att.getFileUrl()+",";
						String s = "";
						String attJson = JSONUtil.toJSONString(att);
						Map<String,Object> maps = (Map<String, Object>) JSONUtil.parseJSONString(attJson);
						maps.put("command", "131");
						maps.put("name", att.getFilename());
						attJson = JSONUtil.toJSONString(maps);
						String attType = maps.get("icon") != null ? maps.get("icon").toString() :"";
						String miniType = "";
						if(!"".equals(attType) && attType.contains(".")){
							miniType = attType.split("\\.")[0];
						}
						if(FormUtil.isPhoneLogin()){
							String iconClass = "";
							if(Strings.isNotBlank(miniType)) {
								iconClass = getIconClass(miniType);
							}
							String icon="<i class=\'attachment-icon "+iconClass+"\'></i>";
							//文单上显示附件
							s = "<div style='display:block'>"+ icon + "<a class='document-content allow-click-attachment' style='text-decoration:underline;font-size:12px;color:#005599' see-att-data = '"+attJson+"'>"+att.getFilename()+"</a></div>";
						}else{
							String imageStr = "<span style=\"word-wrap:break-word; word-break:break-all;position:relative;top:-1px;\" class=\"ico16 "+miniType+"_16 margin_r_5\"></span>";
							String str = com.seeyon.ctp.common.filemanager.manager.Util.AttachmentToHtmlWithShowAllFileName(att,true, false);
							int imgBegin = str.indexOf("<img");
							s = str.substring(0, imgBegin) + imageStr + str.substring(str.indexOf("<", imgBegin + 4), str.length());
						}
						sb.append(s);
						attSb.append(s);
					}
					attSb.append("</div>");
					
					if("senderOpinion".equals(element)) {
					   senderAttMap.put(opinion.getId(), attSb);
					}
				}
				
				//发起人附言如果没有绑定不向前台显示。前台页面通过下面的对象，有代码+标签的形式展示。
				if("senderOpinion".equals(element)) {
					senderOpinions.add(opinion);
					continue;
				}
				sb.append("\n");
				jsMap.put(element, sb.toString());
			}
		}
		if(Strings.isNotBlank(fileUrlStr) && fileUrlStr.length()>1){
			fileUrlStr = fileUrlStr.substring(0,fileUrlStr.length()-1);
		}
		AppContext.putRequestContext("fileUrlStr", fileUrlStr);
		jsMap.put("senderOpinionAttStr",senderAttMap );
		jsMap.put("senderOpinionList", senderOpinions);
		return jsMap;	
	}
	
	
	
	/**
	 * 
	 * @Description : 将字符串替换成空格，双字节替换成全角空格，单字节替换成半角空格
	 * @Author      : xuqiangwei
	 * @Date        : 2014年11月14日上午1:04:43
	 * @param src : 需要替换的字符串
	 * @param defualt : 如果字符串为null或为空时默认返回字符串
	 * @return
	 */
	private static String replaceStr2Blank(String src, String defualt){

	    StringBuilder ret = new StringBuilder();
	    
	    if(Strings.isBlank(src) && defualt != null){
	        ret.append(defualt);
	    }else {
	        for(int i = 0; i < src.length(); i++){
	            char c = src.charAt(i);
	            if((int)c > 0 && (int)c < 255){//普通字符0x00 ~ 0xff
	                ret.append(ResourceUtil.getString("govdoc.space"));//半角空格
	            }else {
                    ret.append(ResourceUtil.getString("govdoc.space.quan"));//全角空格
                }
	        }
        }
	    return ret.toString();
	}
	
	/**
	 * 
	 * @Date        : 2015年5月19日下午5:48:53
	 * @param displayConfig
	 * @param opinion
	 * @param hasSignature
	 * @param popUserInfo 是否新增用户信息选项卡连接 true 添加， false 不添加
	 * @return
	 */
	private static String displayOpinionContent(FormOpinionConfig displayConfig, EdocOpinion opinion, boolean hasSignature, boolean popUserInfo,String attitude) {

	    OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");

		StringBuilder sb = new StringBuilder();

		//显示内容：态度，用户名，意见类型，意见
		String attribute = attitude;//getAttitude(opinion.getOpinionType(), opinion.getAttribute());
		String userName = getOpinionUserName(opinion.getCreateUserId(),opinion.getProxyName(),orgManager, displayConfig, opinion, popUserInfo,hasSignature);
		String content = opinion.getContent();
		sb.append("<div id='"+opinion.getAffairId()+"' class='edocOpinion' style='clear:both;'>");
		sb.append("</div>");
		boolean newLine = displayConfig.isInscriberNewLine();
		
		if(newLine){//设置落款换行显示
            sb.append("<div>");
        }
		
		//上报意见不显示态度
		String attrStr = null;
		if (Strings.isNotBlank(attribute)&&attribute != null && (null != opinion.getPolicy() && !opinion.getPolicy().equals(EdocOpinion.REPORT))) {
		    attrStr = "【"+ attribute+"】";
			sb.append(attrStr);
		}
		// 意见排序 ：【态度】 意见 部门 姓名 时间
		sb.append(Strings.toHTML(content));
		if(newLine){//设置落款换行显示
            sb.append("</div>");
        }
		
		if(newLine){//设置落款换行显示, 跳过态度，与意见对齐,没有态度则两个汉字宽度，兼容国际化
		    String defualt = ResourceUtil.getString("govdoc.space.quan2")+ResourceUtil.getString("govdoc.space.quan2");//默认两个全角空格 四个全角为一个回车
		    attrStr = replaceStr2Blank(attrStr, defualt);
            sb.append("<div>").append(Strings.toHTML(attrStr));
		}else {
		    if(Strings.isNotBlank(content)){
		      //意见内容和后面的部门或者人员名称隔开几个空格
	            if(displayConfig.isShowDept()||displayConfig.isShowUnit()){
	            	sb.append("&ensp;&ensp;");
	            }
		    }
        }
		
		//设置了【文单签批后不显示系统落款 】，如果没有签批内容，则也需要显示系统落款。也就是，系统落款和签批内容至少要有一个
		//下面是追加显示单位名称-------魏俊标--2011-10-12
		if(displayConfig.isShowUnit() && !(displayConfig.isHideInscriber() && hasSignature)){
		     if(userName.indexOf("IMG")==-1)
					sb.append("&ensp;").append(opinion.getAccountName());
				else
					sb.append(opinion.getAccountName());
		} 
		if (displayConfig.isShowDept() && !(displayConfig.isHideInscriber() && hasSignature)) {
			if(userName.indexOf("IMG")==-1)
				sb.append("&ensp;").append(opinion.getDepartmentName());
			else
				sb.append(opinion.getDepartmentName());
		}

		if(!(displayConfig.isHideInscriber() && hasSignature)){
			sb.append("&ensp;").append(userName);
		}
		if (OpinionDateFormatSetEnum.DATETIME.getValue().equals(displayConfig.getShowDateType())) {

			if (OpinionDateModelSetEnum.FULL.getValue().equals(displayConfig.getShowDateModel())) {
				Date current = opinion.getCreateTime();
				String c= Datetimes.format(current, ResourceUtil.getString("govdoc.date.format"));
				sb.append("&ensp;").append(c);
			}

			else {
				sb.append("&ensp;").append(Datetimes.formatDatetimeWithoutSecond(opinion.getCreateTime()));
			}
		} else if (OpinionDateFormatSetEnum.DATE.getValue().equals(displayConfig.getShowDateType())) {
			if (OpinionDateModelSetEnum.FULL.getValue().equals(displayConfig.getShowDateModel())) {
				Date current= opinion.getCreateTime();
				String c= Datetimes.format(current, ResourceUtil.getString("govdoc.date.format1"));
				sb.append("&ensp;").append(c);
			}else {
				sb.append("&ensp;").append(Datetimes.formatDate(opinion.getCreateTime()));
			}
		}
		if(newLine){//设置落款换行显示
            sb.append("</div>");
        }
		 sb.append("<br/>");//M3ywg 发现文单签批有问题，pc把字体从14Pt缩小，也会出现格式错乱。添加换行符，解决完整回复意见换个行。

		return sb.toString().replaceAll("&nbsp;", "&ensp;");
	}
	
	private static V3xOrgMember getMember(Long id,OrgManager orgManager){
		V3xOrgMember member = new V3xOrgMember() ;
		try {
			member = orgManager.getMemberById(id);
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			LOGGER.error("", e);
		}
		return member;
	}
	
	/*********************************** 唐桂林 公文意见显示 start *************************************/
	@SuppressWarnings("rawtypes")
	public static String optionToJs(Map<String, Object> hs){
        String key="";
        StringBuilder opinionsJs = new StringBuilder();
        opinionsJs.append("var opinions=[");
        Iterator it = hs.keySet().iterator();
        //添加这个变量主要是用来判断是否加，
        boolean isFirst = true;
        String szTemp=null;
        while(it.hasNext()) {
        	key= (String)it.next();
        	//拟文意见
        	if("senderOpinionList".equals(key)||"senderOpinionAttStr".equals(key))
        		continue;
        	if(isFirst) isFirst = false;
        	else opinionsJs.append(",");
        	szTemp = hs.get(key).toString();
        	//V51-4-18 公文单中处理时插入附件全标题显示,公文附件标题元素也全显示
        	//szTemp = subLargerGuanlanWendang(szTemp);//对于文档名过长的过滤
        	szTemp = Strings.escapeJavascript(szTemp);//转js
        	opinionsJs.append("[\"").append(key).append("\",\"").append(szTemp).append("\"]");
        }
        opinionsJs.append("];");
        opinionsJs.append("\r\n");
        
        String sendOpinionStr = "";
        Object sendOpinionObj = hs.get("senderOpinionList");
        if(sendOpinionObj != null) {
        	sendOpinionStr=sendOpinionObj.toString();
        }
        if("[]".equals(sendOpinionStr)) sendOpinionStr="";
        opinionsJs.append("var sendOpinionStr=\""+sendOpinionStr+"\";");
        opinionsJs.append("var edocDetailURL=\""+ SystemEnvironment.getContextPath() + "/edocController.do\";");
        
        return opinionsJs.toString();
	}
	
	/**
	 * 节点类型(shenpi、huitui...)
	 * @param map
	 * @return
	 */
	public static String getEdocOpinionPolicy(Map<String, EdocOpinionModel> map) {
		Set set=null;
        String policy=null;
        if(map!=null&&!map.isEmpty()){
            set=map.entrySet();
            if(set!=null&&!set.isEmpty()){
                Iterator it=set.iterator();
                while(it.hasNext()){
                    Map.Entry entry = (Map.Entry) it.next();
                    if(entry!=null){
                        policy=entry.getKey().toString();
                    }
                }
            }
        }
        return policy;
	}
	
	public static List<EdocOpinionBO> sortEdocOpinion(List<EdocOpinion> opinions) {
		List<EdocOpinionBO> opinionBOs = new ArrayList<EdocOpinionBO>();
        EdocOpinionBO edocOpinionBO = null;
        try {
        	if(opinions!=null) {
		        for(int i=0; i<opinions.size(); i++) {
		        	edocOpinionBO = new EdocOpinionBO();
		        	org.apache.commons.beanutils.BeanUtils.copyProperties(edocOpinionBO, opinions.get(i));
		        	opinionBOs.add(edocOpinionBO);
		        }
        	}
        } catch(Exception e){}
        return opinionBOs;
	}
	
	public static String getAffairReturnState(Map<String,EdocOpinionModel> map, String policy, CtpAffair currentAffair) throws BusinessException {
		List<EdocOpinion> opinions = null;
        EdocOpinionModel edocOpinionModel = null;
        if(map != null) {
            edocOpinionModel=map.get(policy);
        }
        if(edocOpinionModel!=null) {
            opinions = edocOpinionModel.getOpinions();
        }
        return getAffairReturnState(opinions, currentAffair);
	}
	
	public static String getAffairReturnState(List<EdocOpinion> opinions, CtpAffair currentAffair) throws BusinessException {
		List<EdocOpinionBO> opinionBOs = sortEdocOpinion(opinions);
		AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
		String affairState = "";
        if(opinionBOs != null) {
        	EdocOpinionBO edocOpinion = null;
            CtpAffair affair1 = null;
            CtpAffair affair = null;
            int count = opinionBOs.size();
            long currentUserId = AppContext.getCurrentUser().getId();
            for(int i = count-1; i>=0; i--) {
                edocOpinion = opinionBOs.get(i);
                affair1 = affairManager.get(edocOpinion.getAffairId());
                if(affair1 == null)
                	continue;
                /*if(affair1==null || affair1.getActivityId()==null || currentAffair.getActivityId()==null 
                		|| !affair1.getActivityId().equals(currentAffair.getActivityId())
                		|| affair1.getMemberId().longValue()!=currentUserId) {//非当前节点的意见过滤出去  当前人 代理人TODO
                	break;
                }
                boolean isCurrentAffair = affair1.getState()==StateEnum.col_pending.key() && (affair1.getSubState()==SubStateEnum.col_pending_ZCDB.key() || affair1.getSubState()==SubStateEnum.col_pending_read.key());
                if(!isCurrentAffair) {//并且当前节点，当前人不是在办状态
                	break;
                }
                */
                boolean isCurrentAffair = affair1!=null && affair1.getActivityId()!=null && currentAffair.getActivityId()!=null
                		&& affair1.getActivityId().equals(currentAffair.getActivityId()) //当前节点
                		&& affair1.getMemberId().longValue()==currentUserId;//当前人 代理人TODO
                boolean isCurrentAffairState = affair1.getState()==StateEnum.col_pending.key() && (affair1.getSubState()==SubStateEnum.col_pending_ZCDB.key() || affair1.getSubState()==SubStateEnum.col_pending_read.key());
                
                if(isCurrentAffair && !isCurrentAffairState) {//并且当前节点，当前人不是在办状态
                	affair = affair1;
                	break;
                }
               
            }
            if(affair != null){
                affairState = String.valueOf(affair.getState());
            }
        }
        return affairState;
	}
	/*********************************** 唐桂林 公文意见显示 end *************************************/
	
	//对于关联文档名过长的过滤。
	private static String subLargerGuanlanWendang(String str) {
		int begin=str.indexOf("style='font-size:12px'>");
		int end=str.indexOf("</a>");
		if(begin!=-1||end!=-1)
		{
			String wname=str.substring(begin, end).replace("style='font-size:12px'>", "");
	    	StringBuilder sb=new StringBuilder();
	    	if(wname.length()<30)
	    	{
	    		return str;
	    	}
	    	else
	    	{
	    		sb.append(str.substring(0, begin));
	    		sb.append("style='font-size:12px'>");
	    		//OA-50904
	    		wname = wname.replace("&nbsp;", " ");
	    		if(wname.length() > 30){
	    		    wname = wname.substring(0,30);
	    		}
	    		sb.append(wname);
	    		sb.append("......");
	    		sb.append(str.substring(end));
	    		return sb.toString();
	    	}
		}
		else
		{
			return str;
		}
		
	}
	
	
	//客开 项目名称：贵州省政府 ，作者：dengyd，修改日期：2015-4-17 上午10:33:26，功能[文单中的意见判断如果有签名章，则将人名自动替换为签名章]，start
		/**
		 * 取公文单显示的时候的人名   
		 * @param userId
		 * @param proxyName
		 * @param orgManager
		 * @return
		 */
		private static String getOpinionImgUserNameForGZZB(Long userId,String proxyName,OrgManager orgManager){
			String doUserName = "";
			try {
				V3xOrgMember member = orgManager.getMemberById(userId);
				doUserName = member.getName();
				if (member.getIsAdmin()) {
					// 如果是管理员终止，不显示管理员名字及时间
					doUserName = "";
				} else {
					SignetManager signetManager = (SignetManager) AppContext.getBean("signetManager");
					List<V3xSignet> signets = signetManager.findSignetByMemberId(userId);
					V3xSignet ssi = null;
					if(signets!=null&&signets.size()>0){
						for (int i = 0; i < signets.size(); i++) {
							if(signets.get(i).getMarkType().equals(0)){
								ssi = signets.get(i);
								break;
							}
						}
					}
					//huzy 图片显示
					String namePath =  SystemEnvironment.getContextPath() + "/apps_res/edoc/images/signets/";
					//往本地保存图片
					if(ssi!=null){
						//签名图片样式
						byte[] b = ssi.getMarkBodyByte();
						//拼装文件夹路径
						String path = SystemProperties.getInstance().getProperty("A8.base.folder")+"/";
						path+="../ApacheJetspeed/webapps";
						path+=namePath;
						//看看这个文件夹有没有，没有就创建一个
						File file = new File(path);
						if(!file.exists()){
							file.mkdirs();
						}
						//拼装签名图片路径
						path=path+userId+ssi.getImgType();
						//看看文件夹里有没有这个文件，有的话就不用新创建了
						writeFile(path, b);
						//end
						String spath = namePath+userId+ssi.getImgType()+"?r="+System.currentTimeMillis();
						//客开BUG 项目名称：贵州省政府 # 157 打印时处理笺打印时来文单位下图标不消失  dengyd 20150615 start
						doUserName = "<img name='signimg' src="+spath
								//style='vertical-align: text-top;'
						//客开BUG 项目名称：贵州省政府 # 157 打印时处理笺打印时来文单位下图标不消失  dengyd 20150615 end
								+" class='color_blue'  onclick='javascript:showV3XMemberCard(\""
								+ userId
								+ "\")'>"
								+ "</img>";
					}else{//此人没有签名图片
						doUserName = "<span class='color_blue' onclick='javascript:showV3XMemberCard(\""
							+ userId
							+ "\")'>"
							+ doUserName + "</span>";
					}
					
				}

				if (!Strings.isBlank(proxyName)) {
					doUserName += ResourceBundleUtil
							.getString(
									"com.seeyon.v3x.edoc.resources.i18n.EdocResource",
									"edoc.opinion.proxy", proxyName);
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
			}
			doUserName = "<br>&nbsp;&nbsp;&nbsp;&nbsp;"+doUserName;
			return doUserName;
		}
		
		/**
	     * 根据扩展名，返回图标class
	     * @param extension
	     * @returns {string}
	     * @private
	     */
	    private static String getIconClass (String type) {
	    	if("txt".equals(type)){
	    		 return "see-icon-v5-form-ic-txt-fill";
	    	}else if("jpg".equals(type) || "png".equals(type)|| "icon".equals(type)|| "gif".equals(type)){
	    		 return "see-icon-v5-form-ic-image-fill";
	    	}else if("doc".equals(type) || "docx".equals(type)){
	    		 return "see-icon-v5-form-ic-doc-fill";
	    	}else if("xls".equals(type) || "xlsx".equals(type)){
	    		 return "see-icon-v5-form-ic-xls-fill";
	    	}else if("ppt".equals(type) || "pptx".equals(type)){
	    		return "see-icon-v5-form-ic-ppt-fill";
	    	}else if("wps".equals(type)){
	    		 return "see-icon-v5-form-ic-wps-fill";
	    	}else if("vsd".equals(type)){
	    		return "see-icon-v5-form-ic-vsd-fill";
	    	}else if("pdf".equals(type)){
	    		 return "see-icon-v5-form-ic-pdf-fill";
	    	}else if("et".equals(type)){
	    		return "see-icon-v5-form-ic-et-fill";
	    	}else if("htm".equals(type) || "html".equals(type) || "sht".equals(type) || "shtm".equals(type)
	    			|| "shtml".equals(type) || "asp".equals(type)|| "aspx".equals(type) || "php".equals(type) 
	    			|| "jsp".equals(type)){
	    		return "see-icon-v5-form-ic-html-fill";
	    	}else if("mp3".equals(type)){
	    		 return "see-icon-v5-form-ic-mp3-fill";
	    	}else if("avi".equals(type) || "mp4".equals(type) || "wmv".equals(type) || "rm".equals(type)
	    			|| "rmvb".equals(type)|| "mpg".equals(type)|| "mkv".equals(type)|| "flv".equals(type)){
	    		 return "see-icon-v5-form-ic-mp4-fill";
	    	}else if("wma".equals(type) || "wav".equals(type) || "amr".equals(type) || "ACT".equals(type)
	    			 || "ava".equals(type)){
	    		 return "see-icon-v5-form-ic-voice-fill";
	    	}else if("zip".equals(type) || "rar".equals(type) || "jar".equals(type) || "tar".equals(type)
	    			 || "7z".equals(type)){
	    		 return "see-icon-v5-form-ic-zip-fill";
	    	}else if("collaboration".equals(type)){
	    		return "see-icon-v5-form-ic-col-fill";
	    	}else if("edoc".equals(type)){
	    		return "see-icon-v5-form-ic-offdoc-fill";
	    	}else if("km".equals(type)){
	    		return "see-icon-v5-form-relation-doc";
	    	}else if("exe".equals(type)) {
	    		return "cmp-icon-document exe";
	    	}else{
	    		return "see-icon-v5-form-ic-unknown-fill";
	    	}
	    }
		
		/**
		 * 将文件流写入对于文件夹
		 * @author 
		 * @param name 文件名
		 * @param b 文件流
		 * @author huzy
		 */
		public static void writeFile(String url,byte[] b) {
			File file = new File(url);
			BufferedOutputStream stream = null;
			FileOutputStream fstream = null;
			try {
				if(file.exists()){
					file.delete();
				}
				file.createNewFile();
				fstream = new FileOutputStream(file);
				stream = new BufferedOutputStream(fstream);
				stream.write(b);
			} catch (FileNotFoundException e) {
				LOGGER.error(e);
				//e.printStackTrace();
			} catch (IOException e) {
				LOGGER.error(e);
				//e.printStackTrace();
			} finally {
				try {
					if(stream!=null){
						stream.close();
					}
				} catch (IOException e) {
					LOGGER.error(e);
					//e.printStackTrace();
				}
				try {
					if(fstream!=null){
						fstream.close();
					}
				} catch (IOException e) {
					LOGGER.error(e);
					//e.printStackTrace();
				}
			}
		}
		//客开 项目名称：贵州省政府 ，作者：dengyd，修改日期：2015-4-17 上午10:33:26，功能[文单中的意见判断如果有签名章，则将人名自动替换为签名章]，end
}
