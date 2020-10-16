package com.seeyon.ctp.portal.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.trustdo.utils.XRDPropUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.trace.manager.TraceWorkflowManager;
import com.seeyon.ctp.common.trace.vo.TraceWebVO;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete;
import com.seeyon.ctp.portal.section.templete.MultiListTemplete;
import com.seeyon.ctp.portal.section.templete.MultiRowVariableColumnTemplete;
import com.seeyon.ctp.portal.section.templete.mobile.MListTemplete;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;

public class EdocReturnSection extends BaseSectionImpl {

	private static final Log LOGGER = CtpLogFactory.getLog(EdocReturnSection.class);
	public static final String RESOLVE_FUNCTION= "edocReturnSection";
	
	private TraceWorkflowManager traceWorkflowManager;
	private OrgManager orgManager;
	
	public void setTraceWorkflowManager(TraceWorkflowManager traceWorkflowManager) {
		this.traceWorkflowManager = traceWorkflowManager;
	}	
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

    @Override
	public String getResolveFunction(Map<String, String> preference) {
		return MListTemplete.RESOLVE_FUNCTION;
	}

	@Override
	public Integer getTotal(Map<String, String> preference) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getId() {
		return "edocReturnSection";
	}

	@Override
	public String getBaseName(Map<String, String> preference) {
		String name = preference.get("columnsName");
		if (Strings.isBlank(name)) {
			return name = ResourceUtil.getString("edoc.return.title");
		}
		return name;
	}

	@Override
	public String getName(Map<String, String> preference) {
		// 栏目显示的名字，必须实现国际化，在栏目属性的“columnsName”中存储
		String name = preference.get("columnsName");
		if (Strings.isBlank(name)) {
			return name = ResourceUtil.getString("edoc.return.title");
		} else {
			return name;
		}
	}
	
	@SuppressWarnings("unchecked")
	public BaseSectionTemplete projection(Map<String, String> preference) {
		int style = 1;
		if ((preference.get("columnstyle") == null) || ("0".equals(preference.get("columnstyle"))))
			style = 0;
		FlipInfo f = new FlipInfo();
		Map<String, String> map = new HashMap<String, String>();
		map.put("app", String.valueOf(ApplicationCategoryEnum.edoc.getKey()));
		map.put("recordType", "stepBackRecord");
		try {
			traceWorkflowManager.getColPageInfo(f,map);
		} catch (BusinessException e) {
			LOGGER.error(e);
			//e.printStackTrace();
		}
		List<TraceWebVO> curDatas = f.getData();
		if (Strings.equals(Integer.valueOf(style), Integer.valueOf(1))) {
			MultiListTemplete c = new MultiListTemplete();
			List<TraceWebVO> list1 = new ArrayList<TraceWebVO>();
			List<TraceWebVO> list2 = new ArrayList<TraceWebVO>();
			if (CollectionUtils.isNotEmpty(curDatas)) {
				for (int i = 0; i < curDatas.size(); i++) {
					TraceWebVO tVo = (TraceWebVO) curDatas.get(i);
					if (i % 2 == 0)
						list1.add(tVo);
					else {
						list2.add(tVo);
					}
				}
			}
			c.addTemplete(getTemplete(list1, preference));
			c.addTempleteName(getTemplete(list1, preference).getResolveFunction());
			c.addTemplete(getTemplete(list2, preference));
			c.addTempleteName(getTemplete(list2, preference).getResolveFunction());

			c.addBottomButton(ResourceUtil.getString("calendar.more"),
					"/supervise/supervise.do?method=listRecord&app=4&record=stepBackRecord", null, "sectionMoreIco");
			return c;
		}
		MultiRowVariableColumnTemplete c = (MultiRowVariableColumnTemplete) getTemplete(curDatas, preference);
		c.addBottomButton(ResourceUtil.getString("calendar.more"),
				"/supervise/supervise.do?method=listRecord&app=4&record=stepBackRecord", null, "sectionMoreIco");
		return c;
	}

	private MultiRowVariableColumnTemplete getTemplete(List<TraceWebVO> traceWebVOs, Map<String, String> preference) {
		String property = (String) preference.get("columnproperty");
		if (property == null) {
			property = "0,2,3";
		}
		MultiRowVariableColumnTemplete c = new MultiRowVariableColumnTemplete();
		int count = ParamUtil.getInt(preference, "columnscount", 8).intValue();
		int curSize = count > traceWebVOs.size() ? traceWebVOs.size() : count;
		for (int i = 0; i < curSize; ++i) {
			MultiRowVariableColumnTemplete.Row row = c.addRow();
			TraceWebVO tVo = (TraceWebVO) traceWebVOs.get(i);
			if (tVo == null) {
				break;
			}
			if (property.contains("0")) {
				MultiRowVariableColumnTemplete.Cell subjectCell = row.addCell();
				subjectCell.setCellContent(tVo.getSubject());
				subjectCell.setAlt(tVo.getSubject());
				StringBuilder url = new StringBuilder();
				url.append("/").append(tVo.getDetailPageUrl()).append("&affairId=");
				url.append(tVo.getAffairId()).append("&summaryId=").append(tVo.getObjectId());
				url.append("&trackTypeRecord=").append(tVo.getTrackType()).append("&openFrom=stepBackRecord");
				subjectCell.setLinkURL(url.toString());
				subjectCell.setCellContentWidth(40);
                //设置附件图标
                if(tVo.getHasAttsFlag()){
                    subjectCell.addExtClasses("ico16 vp-attachment");
                }
                //设置重要程度图标
                if(tVo.getImportantLevel() != null && tVo.getImportantLevel() > 1  && tVo.getImportantLevel() < 6){
                    subjectCell.addExtPreClasses("ico16 important"+tVo.getImportantLevel()+"_16");
                }
	            //添加‘正文类型’图标
	            String bodyType = tVo.getBodyType();
	            if(Strings.isNotBlank(bodyType) && !"10".equals(bodyType) && !"30".equals(bodyType)) {
	                String bodyTypeClass = convertPortalBodyType(bodyType);
	                if (!"meeting_video_16".equals(bodyTypeClass)) {
	                	bodyTypeClass = "office" + bodyTypeClass;
	                }
	                if(!"html_16".equals(bodyTypeClass)) {
	                	subjectCell.addExtClasses("ico16 "+bodyTypeClass);
	                }
	            }
			}
			if (property.contains("1")) {
				row.addCell().setCellContent(String.format("%tF", tVo.getSenderTime()));
			}
			if (property.contains("2")) {
				row.addCell().setCellContent(String.format("%tF %tR", tVo.getOperationTime(), tVo.getOperationTime()));
			}
			if (property.contains("3")) {
				row.addCell().setCellContent(tVo.getOperationName());
			}
			if (property.contains("4")) {
				row.addCell().setCellContent(tVo.getSenderName());
			}
		}
		if (curSize % 2 != 0) {
			int propertyFlag = ((MultiRowVariableColumnTemplete.Row) c.getRows().get(0)).getCells().size();
			MultiRowVariableColumnTemplete.Row row = c.addRow();
			for (int i = 0; i < propertyFlag; ++i) {
				MultiRowVariableColumnTemplete.Cell temp0 = row.addCell();
				temp0.setCellContent("");
			}
		}
		c.setDataNum(count);
		return c;
	}
	
	/**
	 * 是否允许添加-使用该栏目，默认允许，如果需要特别控制，需要重载该方法，当前登录信息从CurrentUser中取
	 * @return
	 */
    @Override
	public boolean isAllowUsed() {
		User user = CurrentUser.get();
		try {
			boolean isRole = orgManager.isRole(user.getId(), user.getLoginAccount(), "RecEdocBack");
			CtpLogFactory.getLog(EdocReturnSection.class).info("获取回退公文栏目角色权限：" + isRole);
			//回退公文栏目需要都能用 OA-165147 
			isRole = true;
			return isRole || user.isAdmin() || user.isGroupAdmin();
		} catch(Exception e) {
			LogFactory.getLog(EdocReturnSection.class).error("获取回退公文栏目权限出错", e);
			return false;	
		}
	}
    
    /**
	 * 是否允许添加该栏目，默认允许，如果需要特别控制，需要重载该方法，当前登录信息从CurrentUser中取
	 * 如果不允许，将不出现在备选栏目中；但如果是管理员推送的栏目，可以访问
	 * @return
	 */
	public boolean isAllowUsed(String spaceType) {
		return isAllowUsed();
	}
	
    private String convertPortalBodyType(String bodyType) {
    	String bodyTypeClass = "html_16";
    	if("FORM".equals(bodyType) || "20".equals(bodyType)) {
			bodyTypeClass = "form_text_16";
		} else if("TEXT".equals(bodyType) || "30".equals(bodyType)) {
			bodyTypeClass = "txt_16";
		} else if("OfficeWord".equals(bodyType) || "41".equals(bodyType)) {
			bodyTypeClass = "doc_16";
		} else if("OfficeExcel".equals(bodyType) || "42".equals(bodyType)) {
			bodyTypeClass = "xls_16";
		} else if("WpsWord".equals(bodyType) || "43".equals(bodyType)) {
			bodyTypeClass = "wps_16";
		} else if("WpsExcel".equals(bodyType) || "44".equals(bodyType)) {
			bodyTypeClass = "xls2_16";
		} else if("Pdf".equals(bodyType) || "45".equals(bodyType)) {
			bodyTypeClass = "pdf_16";
		} else if("Ofd".equals(bodyType) || "46".equals(bodyType)) {
            bodyTypeClass = "ofd_16";
        } else if("videoConf".equals(bodyType)) {
			bodyTypeClass = "meeting_video_16";
		}
		return bodyTypeClass;
    }
}
