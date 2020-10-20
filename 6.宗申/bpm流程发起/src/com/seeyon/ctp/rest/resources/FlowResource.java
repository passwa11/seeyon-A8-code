package com.seeyon.ctp.rest.resources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;

import com.seeyon.apps.cip.api.CipApi;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.cap4.form.api.FormApi4Cap4;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.form.api.FormApi4Cap3;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormFieldBean;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.services.CTPLocator;
import com.seeyon.ctp.services.ErrorServiceMessage;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.supernode.manager.WorkflowSuperNodeApi;
import com.seeyon.oainterface.common.IDataPojo;
import com.seeyon.oainterface.common.OAInterfaceException;
import com.seeyon.oainterface.common.PropertyList;
import com.seeyon.v3x.edoc.dao.EdocOpinionDao;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.project.manager.ProjectManager;
import com.seeyon.v3x.services.document.DocumentFactory;
import com.seeyon.v3x.services.flow.FlowFactory;
import com.seeyon.v3x.services.flow.FlowService;
import com.seeyon.v3x.services.flow.FlowUtil;
import com.seeyon.v3x.services.flow.bean.FlowExport;
import com.seeyon.v3x.services.flow.bean.TextExport;
import com.seeyon.v3x.services.flow.bean.TextHtmlExport;
import com.seeyon.v3x.services.flow.log.FlowLog;
import com.seeyon.v3x.services.flow.log.FlowLogFactory;
import com.seeyon.v3x.services.form.FormUtils;
import com.seeyon.v3x.services.form.bean.FormExport;
@Path("/flow")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class FlowResource extends BaseResource{
private FlowLogFactory flowLog = FlowLogFactory.getInstance(FlowResource.class);
	private static Log log = LogFactory.getLog(FlowResource.class);
	private FlowFactory     flowFactory;
	private FormApi4Cap3 formApi4Cap3;
	private DocumentFactory documentFactory;
	private FlowService flowService;
	private TemplateManager  templateManager;
	private EdocOpinionDao edocOpinionDao;
	private AffairManager affairManager;
	private OrgManager orgManager;
	private OrgManagerDirect orgManagerDirect;
	private CommentManager   ctpCommentManager;
	private  ColManager colManager;
	private  ProjectManager projectManager;
	private CipApi cipFlowEventApi;
	public void setEdocOpinionDao(EdocOpinionDao edocOpinionDao) {
		this.edocOpinionDao = edocOpinionDao;
	}

	
	public void setFlowFactory(FlowFactory flowFactory) {
		this.flowFactory = flowFactory;
	}
	
	public FormApi4Cap3 getFormApi4Cap3() {
	    if(formApi4Cap3 == null){
	        formApi4Cap3 = (FormApi4Cap3) AppContext.getBean("formApi4Cap3");
        }
        return formApi4Cap3;
    }

	public void setTemplateManager(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	public void setFlowService(FlowService flowService) {
		this.flowService = flowService;
	}
    public AffairManager getAffairManager() {
        if(affairManager == null){
        	affairManager = (AffairManager) AppContext.getBean("affairManager");
        }
        return affairManager;
    }
	public OrgManager getOrgManager() {
		if (orgManager == null) {
			orgManager = (OrgManager) AppContext.getBean("orgManager");
		}
		return orgManager;
	}
	public FlowFactory getFlowFactory() {
        if (flowFactory == null) {

            flowFactory = (FlowFactory) AppContext.getBean("flowFactory");
        }
		return flowFactory;
	}
	
	public OrgManagerDirect getOrgManagerDirect() {
		if(orgManagerDirect==null){
			orgManagerDirect = (OrgManagerDirect) AppContext.getBean("orgManagerDirect");
		}
		return orgManagerDirect;
	}

	public CommentManager getCtpCommentManager() {
		 if (ctpCommentManager == null) {
			 ctpCommentManager = (CommentManager) AppContext.getBean("ctpCommentManager");
	     }
		return ctpCommentManager;
	}
	
	public EdocOpinionDao getEdocOpinionDao() {
		 if (edocOpinionDao == null) {
			 edocOpinionDao = (EdocOpinionDao) AppContext.getBean("edocOpinionDao");
	     }
		return edocOpinionDao;
	}
	
	public ColManager getColManager(){
    	if(colManager==null){
    		 colManager=(ColManager)AppContext.getBean("colManager");
    	}
    	return colManager;
    }
	
	public ProjectManager getProjectManager(){
    	if(projectManager==null){
    		projectManager=(ProjectManager)AppContext.getBean("projectManager");
    	}
    	return projectManager;
    }

	private Map<String,Object> jsonValueToXml(Map<String,Object> param, String templateCode) {
		if (templateManager == null) {
			templateManager = (TemplateManager) AppContext.getBean("templateManager");
        }
		String templateXml="";
		
		try {
			templateXml=getFlowFactory().getTemplateXml(templateCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		
		Object data = param.get("data");
		
		CtpTemplate template = templateManager.getTempleteByTemplateNumber(templateCode);
		if(template == null||templateXml.length()==0||"".equals(templateXml)) {
			return null;
		}

		try {
			//将templateXml从表去除，然后根据sub信息重新插入
			List<Element> subDellist = new ArrayList<Element>(); 
			Document document = DocumentHelper.parseText(templateXml); 
			Element priceTmp =  document.getRootElement().element("subForms").element("subForm");
			subDellist = document.selectNodes("//subForms/subForm"); 
			//删除节点  
			for(int i=0;i<subDellist.size();i++){
				Element delSub=subDellist.get(i);
				delSub.getParent().remove(delSub); 
			}
			templateXml= document.asXML();
	        
			//将json的打他转换对象
			Map jsonObject= null;
			if(data instanceof Map || data instanceof LinkedHashMap){
				jsonObject = (Map)data;
			}else{
				jsonObject=(Map) JSONUtil.parseJSONString(String.valueOf(data));
			}
			Object sub1=jsonObject.get("sub");
			List sub = null;
            if(sub1 instanceof List){
            	sub = (List)sub1;
            }else{
            	sub = (List)JSONUtil.parseJSONString(String.valueOf(sub1));
            }
            
			boolean isForm = template.getModuleType() == 1 && Integer.valueOf(template.getBodyType()) == 20;

			if(isForm){
			    FormApi4Cap4 formApi4Cap4 = (FormApi4Cap4) AppContext.getBean("formApi4Cap4");
			    com.seeyon.cap4.form.bean.FormBean fromBean4 = formApi4Cap4.getFormByFormCode(template);
				if(fromBean4 != null){
	                // 根据模板编号、表示名称 找出对应的字段
	                List<com.seeyon.cap4.form.bean.FormFieldBean> allFields = fromBean4.getAllFieldBeans();
	                List<com.seeyon.cap4.form.bean.FormFieldBean> mainFields=new ArrayList<com.seeyon.cap4.form.bean.FormFieldBean>();
	                List<com.seeyon.cap4.form.bean.FormFieldBean> subFields=new ArrayList<com.seeyon.cap4.form.bean.FormFieldBean>();
	                for(com.seeyon.cap4.form.bean.FormFieldBean field : allFields){
	                    if(field.isMasterField()){
	                        mainFields.add(field);
	                    }else{
	                        subFields.add(field);
	                    }
	                }
	                document = DocumentHelper.parseText(templateXml);
	                Element formExportEle = (Element)document.selectSingleNode("/formExport");
	                //循环主表字段
	                for(com.seeyon.cap4.form.bean.FormFieldBean field : mainFields) {
	                    List<Element> valueExportList = formExportEle.selectNodes("./values/column[@name='"+field.getDisplay()+"']");
	                    if(valueExportList != null && valueExportList.size() > 0) {
	                        for(Element ele : valueExportList) {
	                            Element valueEle = ele.element("value");
	                            if(jsonObject.get(field.getDisplay())!=null){
	                                valueEle.setText(jsonObject.get(field.getDisplay()).toString());
	                            }
	                        }
	                    }
	                }
	              //根据sub来插入subForm
	                 Element subForms =  document.getRootElement().element("subForms"); 
	                 List subFormslist = subForms.elements();
	                //循环从表字段
	                 if(sub!=null&&sub.size()>0){
	                     for(int i=0;i<sub.size();i++){
	                            //创建元素
	                             Element subForm = new BaseElement("subForm");
	                             Element values = subForm.addElement("values");
	                             Element row = values.addElement("row");
	                            for(com.seeyon.cap4.form.bean.FormFieldBean field : subFields) {
	                            	Map jsonSub= null;
	                            	if(sub.get(i) instanceof Map){
	                            		jsonSub = (Map)sub.get(i);
	                            	}else{
	                            		
	                            		 jsonSub=(Map) JSONUtil.parseJSONString(sub.get(i).toString());
	                            	}
	                                if(jsonSub.get(field.getDisplay())!=null){
	                                     Element column = row.addElement("column");
	                                     column.addAttribute("name", field.getDisplay());
	                                     Element value = column.addElement("value");
	                                     value.setText(jsonSub.get(field.getDisplay()).toString());
	                                }
	                            }
	                            subFormslist.add(i, subForm);
	                        }
	                 }
				}else{
				    FormBean formBean = getFormApi4Cap3().getFormByFormCode(template);
	                // 根据模板编号、表示名称 找出对应的字段
	                List<FormFieldBean> allFields = formBean.getAllFieldBeans();
	                List<FormFieldBean> mainFields=new ArrayList<FormFieldBean>();
	                List<FormFieldBean> subFields=new ArrayList<FormFieldBean>();
	                for(FormFieldBean field : allFields){
	                    if(field.isMasterField()){
	                        mainFields.add(field);
	                    }else{
	                        subFields.add(field);
	                    }
	                }
	                document = DocumentHelper.parseText(templateXml);
	                Element formExportEle = (Element)document.selectSingleNode("/formExport");
	                //循环主表字段
	                for(FormFieldBean field : mainFields) {
	                    List<Element> valueExportList = formExportEle.selectNodes("./values/column[@name='"+field.getDisplay()+"']");
	                    if(valueExportList != null && valueExportList.size() > 0) {
	                        for(Element ele : valueExportList) {
	                            Element valueEle = ele.element("value");
	                            if(jsonObject.get(field.getDisplay())!=null){
	                                valueEle.setText(jsonObject.get(field.getDisplay()).toString());
	                            }
	                        }
	                    }
	                }
	              //根据sub来插入subForm
	                 Element subForms =  document.getRootElement().element("subForms"); 
	                 List subFormslist = subForms.elements();
	                //循环从表字段
	                 if(sub!=null&&sub.size()>0){
	                     for(int i=0;i<sub.size();i++){
	                            //创建元素
	                             Element subForm = new BaseElement("subForm");
	                             Element values = subForm.addElement("values");
	                             Element row = values.addElement("row");
	                            for(FormFieldBean field : subFields) {
	                            	Map jsonSub= null;
	                            	if(sub.get(i) instanceof Map){
	                            		jsonSub = (Map)sub.get(i);
	                            	}else{
	                            		
	                            		 jsonSub=(Map) JSONUtil.parseJSONString(sub.get(i).toString());
	                            	}
	                                if(jsonSub.get(field.getDisplay())!=null){
	                                     Element column = row.addElement("column");
	                                     column.addAttribute("name", field.getDisplay());
	                                     Element value = column.addElement("value");
	                                     value.setText(jsonSub.get(field.getDisplay()).toString());
	                                }
	                            }
	                            subFormslist.add(i, subForm);
	                        }
	                 }
				}
				
				param.put("data", document.asXML());
				return param;
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return null;	
	}

	/**
	 * 外部发起协同(模板协同(非office正文)，表单协同)
	 * @param templateCode 表单模板编号
	 * @param params Map<String, Object> | 必填  | 其他参数
     * <pre>
     * senderLoginName     String  |  必填       |  发起者登录名
     * subject             String  |  必填       |  流程标题
     * data                String(JSON串)   |  必填   |  表单数据
     * param               String  |  必填       |  状态 (0表示发起，1表示待发)
     * attachments         String  |  非必填     |  流程附件ID数组
     * formcontentatt      String  |  非必填     |  表单正文附件ID数组
     * transfertype        String  |  必填       |  默认为xml(data为XML格式),json(data为json格式)
     * </pre>
     * <pre>
	 * @return Map<String, String>
	 * @throws BusinessException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{templateCode}/{thirdFormId}")
	@RestInterfaceAnnotation
	public Response launchCollaboration(@PathParam("templateCode") String templateCode,@PathParam("thirdFormId") String thirdFormId,Map<String,Object> param)
			throws ServiceException,BusinessException,Exception {
		String transfertype="xml";//默认为xml(data为XML格式)
		transfertype=(String) param.get("transfertype");
		Map<String, Object> mapError = new HashMap<String, Object>();
		if("json".equals(transfertype)){
			//将param中data的JSON 转换为XML
			param=jsonValueToXml(param, templateCode);
			if(param==null){
				mapError.put("success", false);
				mapError.put("msg", "转换JSON失败！");
				return ok(mapError);
			}
		}
		
		long summaryId = sendCollaboration(templateCode, param);
        return ok(summaryId);
		
	}
	
	/**
	 * 外部批量发起协同(模板协同(非office正文)，表单协同)表单导入时枚举只能传入ID或者Enumvalue
	 * @param templateCode 表单模板编号
	 * @param params Map<String, Object> | 必填  | 其他参数
     * <pre>
     * senderLoginName     String  |  必填       |  发起者登录名
     * subject             String  |  必填       |  流程标题
     * data                String(JSON串)   |  必填   |  表单数据
     * param               String  |  必填       |  状态 (0表示发起，1表示待发)
     * attachments         String  |  非必填     |  流程附件ID数组
     * formcontentatt      String  |  非必填     |  表单正文附件ID数组
	 * @return Map<String, String>
	 * @throws BusinessException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("batchlaunch/{templateCode}")
	@RestInterfaceAnnotation
	public Response batchlaunchCollaboration(@PathParam("templateCode") String templateCode,Map<String,Object> param) 
			throws Exception {
		List colInfo=(List) param.get("collList");
		List ruturnInfo=new ArrayList();

		String summaryInfo ;
		for(int i=0;i<colInfo.size();i++){
			summaryInfo = sendCollaborationForDee(templateCode,(Map<String, Object>) colInfo.get(i));
			ruturnInfo.add(summaryInfo);
		}
        return ok(ruturnInfo);
		
	}

	private long sendCollaboration(String templateCode,
			Map<String, Object> param) throws ServiceException, Exception,
			BusinessException {
		if (flowFactory == null) {
            flowFactory = (FlowFactory) AppContext.getBean("flowFactory");
        }
		if (templateManager == null) {
			templateManager = (TemplateManager) AppContext.getBean("templateManager");
        }
	
		//正文内容  keyfile|
		Object bodyContent = null;
		//根据模板id，查询模板
		CtpTemplate template = templateManager.getTempleteByTemplateNumber(templateCode);
		if (template == null||template.getWorkflowId()==null) {
	           throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(),ErrorServiceMessage.flowTempleExist.getValue()+":"+templateCode);
	    }
		String senderLoginName = (String) param.get("senderLoginName");
		
		String subject = (String) param.get("subject");
		//是否草稿状态
		String state = (String) param.get("param");
		String data = (String) param.get("data");
		//如果后续还要增加参数 都通过 Map<String,Object> relevantParam 来传递
		Map<String,Object> relevantParam =new HashMap();
		
		//是否有发起人单位ID
		if(param.get("accountCode")!=null){
			 List<V3xOrgEntity> listEntity = getOrgManagerDirect().getEntityNoRelationDirect("V3xOrgAccount", "code",
					 (String) param.get("accountCode"), null, null);
			 if (listEntity.size() > 0) {
				 relevantParam.put("accountId", listEntity.get(0).getId());
			 }
		}
		
		String affairId = (String) param.get("affairId");
		relevantParam.put("affairId", affairId);
		
		//表单正文附件ID
		if(param.get("formContentAtt")!=null){
				Long[] formcontentatt = list2LongArray((List<Long>) param.get("formContentAtt"));
				relevantParam.put("formContentAtt", formcontentatt);
		}
		Long[] attachments = list2LongArray((List<Long>) param.get("attachments"));
		boolean isForm = template.getModuleType() == 1 && Integer.valueOf(template.getBodyType()) == 20;
		FormExport formExportData = null;
		//枚举值转ID
				data = enumValueToId(data, templateCode);
				
				if(isForm){
					if ("2.0".equals(Double.toString(FormUtils.getXmlVersion(data)))) {
		                formExportData = FormUtils.xmlTransformFormExport(data);
						//bodyContent = formExportData;
					}else {
						formExportData = new FormExport();

						PropertyList propertyList = new PropertyList("FormExport", 1);
						propertyList.loadFromXml(data);
						formExportData.loadFromPropertyList(propertyList);
					}
				}
        long summaryId ;
		if(relevantParam.size()>0){
			summaryId=flowFactory.sendCollaboration(senderLoginName, templateCode, subject, formExportData, attachments, state, null,relevantParam);
		}else if(null == formExportData){
			summaryId = flowFactory.sendCollaboration(senderLoginName, templateCode, subject, data, attachments, state, null);
		}else{
			summaryId = flowFactory.sendCollaboration(senderLoginName, templateCode, subject, formExportData, attachments, state, null);
		}
        //记录日志
        FlowLog l = new FlowLog();
        l.setSubject(subject);
        l.setData(data);
        l.setSenderLoginName(senderLoginName);
        l.setTemplateCode(templateCode);
        flowLog.info(l);
		return summaryId;
	}
	private String enumValueToId(String data, String templateCode) {
		CtpTemplate template = templateManager.getTempleteByTemplateNumber(templateCode);
		if(template == null) {
			return data;
		}
		try {
			boolean isForm = template.getModuleType() == 1 && Integer.valueOf(template.getBodyType()) == 20;

			if(isForm){
				FormBean formBean = getFormApi4Cap3().getFormByFormCode(template);
				// 根据模板编号、表示名称 找出对应的字段
				List<FormFieldBean> allFields = formBean.getAllFieldBeans();
				Map<String, FormFieldBean> columnMap = new LinkedHashMap<String, FormFieldBean>();
				Map<String, FormFieldBean> parentFieldMap = new LinkedHashMap<String, FormFieldBean>();
				Map<String, String> enumFieldValueMap = new LinkedHashMap<String, String>();
				for(FormFieldBean field : allFields) {
					
					if(field.getEnumId() != 0) {
						if(StringUtils.isNotEmpty(field.getEnumParent())) {
							for(FormFieldBean bean : allFields) {
								if(field.getEnumParent().equals(bean.getName())) {
									parentFieldMap.put(field.getDisplay(), bean);
									break;
								}
							}
						}
						columnMap.put(field.getDisplay(), field);
					}
					
				}
				Document document = DocumentHelper.parseText(data);
				Element formExportEle = (Element)document.selectSingleNode("/formExport");
				for(String columnName : columnMap.keySet()) {
					List<Element> valueExportList = formExportEle.selectNodes("./values/column[@name='"+columnName+"']");
					if(valueExportList != null && valueExportList.size() > 0) {
						for(Element ele : valueExportList) {
							Element valueEle = ele.element("value");
							String value = (String)valueEle.getData();
							if(StringUtils.isNotEmpty(value)) {
								String enumValue = getEnumValue(columnMap.get(columnName), value, parentFieldMap, enumFieldValueMap);
								enumFieldValueMap.put(columnName, enumValue);
								valueEle.setText(enumValue);
							}
						}
					}
					List<Element> subordinateFormExportList = formExportEle.selectNodes("./subForms/subForm");
					if(subordinateFormExportList != null && subordinateFormExportList.size() > 0) {
						for (Element subEle : subordinateFormExportList) {
							List<Element> recordExportList = subEle.selectNodes("./values/row");
							for (Element recordEle : recordExportList) {
								Element ele = (Element)recordEle.selectSingleNode("column[@name='"+columnName+"']");
								if(ele != null) {
									Element valueEle = ele.element("value");
									String value = (String)valueEle.getData();
									if(StringUtils.isNotEmpty(value)) {
										String enumValue = getEnumValue(columnMap.get(columnName), value, parentFieldMap, enumFieldValueMap);
										enumFieldValueMap.put(columnName, enumValue);
										valueEle.setText(enumValue);
									}
								}
								
							}
						}
					}
				}
				return document.asXML();
			}
		
		} catch (Exception e) {
			log.error(e);
		}
		return data;
	}
	private String getEnumValue(FormFieldBean fieldBean, String value, Map<String, FormFieldBean> parentFieldMap, Map<String, String> enumValueMap) {
		EnumManager enumManager = null;
		try {
			enumManager = CTPLocator.getInstance().lookup(EnumManager.class);
			List<CtpEnumItem> enumValues = enumManager.getEmumItemByEmumId(fieldBean.getEnumId());
			int level = fieldBean.getEnumLevel();
			for(CtpEnumItem item : enumValues) {
				if((value.equals(item.getEnumvalue())||value.equals(Long.toString(item.getId())))&& level == item.getLevelNum()) {
					FormFieldBean parentFildBean = parentFieldMap.get(fieldBean.getDisplay());
					if(parentFildBean != null) {
						
						String strParentId = String.valueOf(item.getParentId());
						if(strParentId.equals(enumValueMap.get(parentFildBean.getDisplay()))) {
							return String.valueOf(item.getId());
						}
					} else {
						return String.valueOf(item.getId());
					}
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		log.error("请检查oa系统的枚举值是否存在 value="+value + "字段："+fieldBean.getDisplay());
		return value;

	}
	
	private String sendCollaborationForDee(String templateCode,
			Map<String, Object> param) throws ServiceException, Exception,
			BusinessException {
		if (flowFactory == null) {
            flowFactory = (FlowFactory) AppContext.getBean("flowFactory");
        }
		if (templateManager == null) {
			templateManager = (TemplateManager) AppContext.getBean("templateManager");
        }
		
		//正文内容  keyfile|
		Object bodyContent = null;
		//根据模板id，查询模板
		String returninfo = "|ok";
		String keyFiledVal = (String) param.get("keyFieldVal");
		
		CtpTemplate template = templateManager.getTempleteByTemplateNumber(templateCode);
		if (template == null||template.getWorkflowId()==null) {
	          // throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(),ErrorServiceMessage.flowTempleExist.getValue()+":"+templateCode);
	           returninfo = "|" + ErrorServiceMessage.flowTempleExist.getValue()+":"+templateCode;
				return keyFiledVal+returninfo;
	    }
		String senderLoginName = (String) param.get("senderLoginName");
		String subject = (String) param.get("subject");
		//是否草稿状态
		String state = (String) param.get("param");
		String data = (String) param.get("data");
		bodyContent = data;
		Long[] attachments = list2LongArray((List<Long>) param.get("attachments"));
		boolean isForm = template.getModuleType() == 1 && Integer.valueOf(template.getBodyType()) == 20;

		try{
		if(isForm){
			if ("2.0".equals(Double.toString(FormUtils.getXmlVersion(data)))) {
                FormExport formExportData = FormUtils.xmlTransformFormExport(data);
				bodyContent = formExportData;
			}
		}
        long summaryId = flowFactory.sendCollaboration(senderLoginName, templateCode, subject, bodyContent, attachments, state, null);
		}catch(Exception e){
			returninfo = "|"+e.getMessage();
			//throw new BusinessException("biz:" + e.getMessage());
		}
        //记录日志
        FlowLog l = new FlowLog();
        l.setSubject(subject);
        l.setData(data);
        l.setSenderLoginName(senderLoginName);
        l.setTemplateCode(templateCode);
        flowLog.info(l);
		return keyFiledVal+returninfo;
	}
	protected Long[] list2LongArray(List list){
		
		if(list == null) {
			return new Long[0];
		}
		int size = list.size();
		Long[] array = new Long[size];
		for (int i = 0; i < size; i++) {
			Object o = list.get(i);
			array[i] = ((Number) o).longValue();
		}
		return array;
	}
	
	/**
	 * 取得流程的运转状态
	 * @param flowId 协同id，summary表主键
	 * @return
	 * @throws ServiceException
	 */
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("state/{flowId}")
	@RestInterfaceAnnotation
    public Response getFlowState (@PathParam("flowId")long flowId) 
    		throws Exception {
        
        if (flowFactory == null) {
            flowFactory = (FlowFactory) AppContext.getBean("flowFactory");
        }
        long state = flowFactory.getFlowState("", flowId);
        return  ok(state);
    }
	/**
	 * @param flowId
	 * @param exportType 0导出枚举值 1导出枚举ID
	 * @param exportFormat 导出格式：xml，导出XML格式;json，导出JSON格式
	 * @return
	 * @throws Exception
	 */
	@GET
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("data/{flowId}")
	@RestInterfaceAnnotation
	public Response getFlowData (@PathParam("flowId")long flowId,@QueryParam("exportType") String exportType,@QueryParam("exportFormat") String exportFormat) 
    		throws Exception {
        if(documentFactory==null){
            documentFactory=(DocumentFactory)AppContext.getBean("documentFactory");
        }
        String restFlag =exportType;
        FlowExport flowExport=documentFactory.exportFlow2(restFlag, flowId);   
        TextExport content = flowExport.getFlowContent();
        if("json".equals(exportFormat)){
        	Map param=new HashMap<String, Object>();
        	ColSummary summary;
            try {
                summary = getColManager().getColSummaryById(flowId);
                if(summary==null){
                    throw new ServiceException(ErrorServiceMessage.documentExportFlowExist.getErroCode(),ErrorServiceMessage.documentExportFlowExist.getValue());
                }
            } catch (BusinessException e) {
            	log.error(e.getLocalizedMessage(),e);
                throw new ServiceException(-1,"导出流程出错"+e.getLocalizedMessage());
            } catch (Exception e) {
            	log.error(e.getLocalizedMessage(),e);
                throw new ServiceException(-1,"导出流程出错"+e.getLocalizedMessage());
            }
            param.put("flowState", NumberUtils.toLong(Integer.toString(FlowUtil.getFlowState(getAffairManager(),summary))));
            param.put("projectId", FlowUtil.getProject(getProjectManager(),summary).getProjectId());
            param.put("projectName", FlowUtil.getProject(getProjectManager(),summary).getProjectName());

			return ok(FormUtils.formExportTransformJson(summary, flowExport, param));
        }else{
        	if(content instanceof FormExport){
    			FormExport fe = (FormExport)content;
    			fe.setDefinitions(Collections.EMPTY_LIST);
    			return ok( FormUtils.formExportTransformXml(fe));
    		}else if(content instanceof TextHtmlExport){
    			return ok(((TextHtmlExport)content).getContext());
    		}
        }
		
        return  ok(content.toString());
    }	
    private String toString(IDataPojo pl) throws ServiceException {
        StringWriter writer = new StringWriter();
        try {
            pl.saveToPropertyList().saveXMLToStream(writer);
        }catch (OAInterfaceException e) {
            throw new ServiceException(e);
       } catch (IOException e) {
    	   log.error(e.getMessage(),e);
		}
        return writer.toString();
    }	
	@POST
	@Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("notification/{flowToken}")
	@RestInterfaceAnnotation
	public Response sendNotification(@PathParam("flowToken") String flowToken,Map<String,Object> param) 
			throws Exception {
		String action = (String)param.get("action");
		if(action==null) {
			action = "approve"; // flow/notification/reject
		}
		WorkflowSuperNodeApi api = getWorkflowSuperNodeApi();
		String message = (String )param.get("message");
		Long memberId = -1L;
		if(param.containsKey("memberId")) {
			memberId = (Long)param.get("memberId");
		}
		String dataId = "";
		if(param.containsKey("dataId")) {
			dataId = (String)param.get("dataId");
		}
		int returnCode = (Integer)param.get("returnCode");
		String memberName = "";
		if(param.containsKey("memberName")) {
			memberName = (String)param.get("memberName");
		}
		api.operateWorkflowEngine(flowToken, returnCode, dataId, memberId, memberName, message);
		return ok(1);

	}
	private WorkflowSuperNodeApi api;
	private WorkflowSuperNodeApi getWorkflowSuperNodeApi() {
		if(api==null) {
			api = (WorkflowSuperNodeApi) AppContext.getBean("workflowSuperNodeApi");
		}
		return api;
	}
			/**
	 * 按时间段查询指定表单模板结束的流程
	 * 
	 * @param templateCode
	 *            模板编号
	 * @param beginDateTime
	 *            开始时间
	 * @param endDateTime
	 *            结束时间
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("FromFinish/{templateCode}/{startTime}/{endTime}")
	@RestInterfaceAnnotation
	public Response getFinishFrom(@PathParam("templateCode") String templateCode,
			@PathParam("startTime") String startTime,
			@PathParam("endTime") String endTime) throws Exception {
		if (flowService == null) {
			flowService = (FlowService) AppContext.getBean("flowService");
		}
		String[] templateCodeInfo = null;

		long[] idinfo = null;
		if (templateCode != null) {
			if (templateCode.indexOf(",") > 0) {
				templateCodeInfo = templateCode.split(",");
			} else {
				templateCodeInfo = new String[] { templateCode };
			}
		}
		if (templateCodeInfo != null && startTime != null
				&& endTime != null) {
			idinfo = flowService.getFormCollIdsByDateTimeRest(templateCodeInfo, startTime, endTime);
		}
		return ok(idinfo);
	}
		/**
	 * 根据summaryId 去查所有意见
	 * 
	 * @param summaryId 协同ID
	 *        
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getcollopinions/{summaryId}")
	@RestInterfaceAnnotation
	public Response getcollopinions(@PathParam("summaryId") Long summaryId) throws Exception {
		List<Comment> commentAllByModuleId = getCtpCommentManager().getCommentAllByModuleId(ModuleType.collaboration, summaryId);
		return ok(commentAllByModuleId);
	}
	
		/**
	 * 根据summaryId 去查所有意见
	 * 
	 * @param summaryId
	 * 		  公文Id
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("getedocopinions/{summaryId}")
	@RestInterfaceAnnotation
	public Response getedocopinions(@PathParam("summaryId") Long summaryId) throws Exception {
		List<EdocOpinion> findEdocOpinionBySummaryId = getEdocOpinionDao().findEdocOpinionBySummaryId(summaryId, false);
		for(EdocOpinion edocOpinion :findEdocOpinionBySummaryId){
			edocOpinion.setEdocSummary(null);
		}
		return ok(findEdocOpinionBySummaryId);
	}

	
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("FromTemplate/{templateCode}")
	@RestInterfaceAnnotation
	public Response getFromTemplate(@PathParam("templateCode") String templateCode) throws Exception {
        if (flowFactory == null) {

            flowFactory = (FlowFactory) AppContext.getBean("flowFactory");
        }
        return ok(flowFactory.getTemplateDefinition(null, templateCode)[1]);
	}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("dostaff/{summaryId}")
	@RestInterfaceAnnotation
	public Response getFlowDoStaff(@PathParam("summaryId") String summaryId) throws Exception {
		if(summaryId==null||summaryId.length()<=0){
			return ok(null);
		}
		List<Integer> stateInfo=new ArrayList<Integer>();
		stateInfo.add(3);//3标示待办
		List<CtpAffair> pendingList=getAffairManager().getAffairsByObjectIdAndStates(null, Long.valueOf(summaryId),stateInfo);

		ArrayList<Long> activityList = new ArrayList<Long>();
		ArrayList<Long> memberList = new ArrayList<Long>();
		ArrayList<V3xOrgMember> memberInfoList = new ArrayList<V3xOrgMember>();
		
		for(int i=0;i<pendingList.size();i++){
			activityList.add(pendingList.get(i).getActivityId());
		}
		for(int i=0;i<pendingList.size();i++){
			memberList.add(pendingList.get(i).getMemberId());
			
		}
		 //获取二维数组行
		 ArrayList<Long> rowInfo = new ArrayList<Long>();
		 ArrayList<Long> memberRemovedup = new ArrayList<Long>();
		 
		 HashSet r  =   new  HashSet(activityList); 
		 rowInfo.addAll(r); 
		 HashSet m  =   new  HashSet(memberList); 
		 memberRemovedup.addAll(m); 
		 
		 for(int i=0;i<memberRemovedup.size();i++){
			 memberInfoList.add(getOrgManager().getMemberById(pendingList.get(i).getMemberId()));
		 }

		 Map<Long, List<Long>> map = new HashMap<Long, List<Long>>();
		 for(CtpAffair s:pendingList){
			 if (map.containsKey(s.getActivityId())) {
	                List<Long> rList = map.get(s.getActivityId());
	                rList.add(s.getMemberId());
	                map.put(s.getActivityId(), rList);
	            } else {
	                List<Long> rList = new ArrayList<Long>();
	                rList.add(s.getMemberId());
	                map.put(s.getActivityId(), rList);
	            }
		 }
		 //定义二位数组每行列
		 V3xOrgMember[][] rows=new V3xOrgMember[rowInfo.size()][]; 
		 Object[] valuesObj = map.values().toArray(); 

		 for(int row=0;row<rows.length;row++){
			 ArrayList<Long> columnInfo=returnArrayLength(valuesObj[row]);
			 if(columnInfo==null){
				 continue;
			 }
			 rows[row]=new V3xOrgMember[columnInfo.size()]; 
			 for(int column=0;column<columnInfo.size();column++){
				 rows[row][column]=findV3xOrgMemberByList(columnInfo.get(column),memberInfoList);
			 }
		 }                           
        return ok(rows);
	}
	
	/** 
	* 获取表单模板V2.0XML信息
	* URL show/showbar
    * @since 6.1
    * @date 2017-3-31
	* @param  参数
	* <pre>
    *	类型	         名称			         必填	 备注
    *	String	templateCode	 Y	表单模板ID
    * </pre>
    * @return 返回表单XML信息
	* @throws 
	*/
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Path("fromtemplatexml/{templateCode}")
	@RestInterfaceAnnotation
	public Response getFromTemplateXml(@PathParam("templateCode") String templateCode) throws Exception {
		 return ok(getFlowFactory().getTemplateXml(templateCode));
	}
	
	private V3xOrgMember findV3xOrgMemberByList(Long memberId,ArrayList<V3xOrgMember> memberInfoList){
		if(memberInfoList==null||memberInfoList.size()<=0||memberId==null){
			return null;
		}
		V3xOrgMember res = null;
		for(V3xOrgMember m:memberInfoList){
			if(memberId.equals(m.getId())){
				res=m;
			}
		}
		return res;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("launchcollabrationbyCIP/{templateCode}")
	public Response launchCollabrationByCIP(@PathParam("templateCode") String templateCode,Map<String,Object> param) throws Exception{
		
		String transfertype="xml";//默认为xml(data为XML格式)
		transfertype=(String) param.get("transfertype");
		Map<String, Object> mapError = new HashMap<String, Object>();
		if("json".equals(transfertype)){
			//将param中data的JSON 转换为XML
			param=jsonValueToXml(param, templateCode);
			if(param==null){
				mapError.put("success", false);
				mapError.put("msg", "转换JSON失败！");
				return ok(mapError);
			}
		}
		//发起前校验
		if(cipFlowEventApi==null){
			cipFlowEventApi=(CipApi)AppContext.getBean("cipFlowEventManager");
		}
		if (templateManager == null) {
			templateManager = (TemplateManager) AppContext.getBean("templateManager");
        }
		CtpTemplate template = templateManager.getTempleteByTemplateNumber(templateCode);
		if (template == null||template.getWorkflowId()==null) {
	           throw new ServiceException(ErrorServiceMessage.flowTempleExist.getErroCode(),ErrorServiceMessage.flowTempleExist.getValue()+":"+templateCode);
	    }
		String senderLoginName = (String) param.get("senderLoginName");
		Map<String,String> o = new LinkedHashMap<String, String>();
		try {
			String sender = cipFlowEventApi.getBusinessSenderForRest(template.getId(), senderLoginName);
			param.put("senderLoginName", sender);
			long summaryId = sendCollaboration(templateCode, param);
			o.put("summaryId", String.valueOf(summaryId));
		} catch (Exception e) {
			return fail(e.getMessage());
		}
        return success(o);
	}
	
	private ArrayList<Long> returnArrayLength(Object value){
		if(value==null){
			return null;
		}
		if(value instanceof ArrayList){
			return (ArrayList<Long>) value;
         }
		return null;
	}
}
