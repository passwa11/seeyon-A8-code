/**
 * 
 */
package com.seeyon.ctp.rest.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.xkjt.dao.XkjtDao;
import com.seeyon.apps.xkjt.po.XkjtOpenMode;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPost;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.services.ServiceException;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;
import com.seeyon.ctp.workflow.engine.enums.ChangeType;
import com.seeyon.ctp.workflow.exception.BPMException;
import com.seeyon.ctp.workflow.exception.NoSuchWorkitemException;
import com.seeyon.ctp.workflow.manager.CaseManager;
import com.seeyon.ctp.workflow.manager.WorkflowAjaxManager;
import com.seeyon.ctp.workflow.util.WorkflowUtil;
import com.seeyon.ctp.workflow.vo.CPMatchResultVO;
import com.seeyon.ctp.workflow.vo.WFMoreSignSelectPerson;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.oainterface.common.OAInterfaceException;

import net.joinwork.bpm.definition.BPMActor;
import net.joinwork.bpm.definition.BPMHumenActivity;
import net.joinwork.bpm.engine.execute.BPMCase;
import net.joinwork.bpm.engine.wapi.ProcessEngine;
import net.joinwork.bpm.engine.wapi.WAPIFactory;
import net.joinwork.bpm.engine.wapi.WorkflowBpmContext;
import sun.util.logging.resources.logging;


/**
 * <p>工作流Rest接口</p>
 * <p>path ： /workflow
 * 
 * @author wangchw
 * @since V5-6.0
 */
@Path("workflow")
@Produces({ MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
public class WorkflowResource extends BaseResource {
    
    private static Log LOGGER = CtpLogFactory.getLog(WorkflowResource.class);
    private static final String ERROR_KEY          = "error_msg";
    private static final String SUCCESS_KEY        = "success"; 
    private static final String SUCCESS_VALUE_TRUE        = "true";
    private static final String SUCCESS_VALUE_FALSE        = "false";
    
    private WorkflowApiManager workflowApiManager = (WorkflowApiManager)AppContext.getBean("wapi");
    private WorkflowAjaxManager WFAjax             = (WorkflowAjaxManager) AppContext.getBean("WFAjax");
    private CaseManager            caseManager            = (CaseManager) AppContext.getBean("caseManager");
    /**
     * <p>获取流程数据</p>
     * 
     * @since V5-6.0
     * 
     * @param isRunning boolean    | 必填 | 
     *                 是否运行中的流程, 取值 <code>true</code> 或者 <code>false</code>
     * @param processId String     | 必填 | 
     *                 流程ID
     * @param caseId    String     | 必填 | 
     *                 流程实例CASEID
     * @return 
     *                 流程节点信息的JSON数据
     * @throws ServiceException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("diagramdata")
    public Response getWorkflowDiagramData(
            @QueryParam("isRunning") boolean isRunning,
            @QueryParam("processId") String processId,
            @QueryParam("caseId") String caseId) throws ServiceException {
        
        String diagramdata;
        try {
            User user = AppContext.getCurrentUser();
            diagramdata = workflowApiManager.getWorkflowJsonForMobileNoMembers(isRunning, processId, caseId, user.getName());
            
            return ok(diagramdata);
            
        } catch (BPMException e) {
            LOGGER.error("获取流程图数据异常,isRunning:="+isRunning+";processId:="+processId+";caseId:="+caseId, e);
            throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
        }
    }
    
    /**
     * 获取流程xml数据
     * 
     * @since V5-6.0
     * 
     * @param isRunning boolean    | 必填 | 
     *                 是否运行中的流程, 取值 <code>true</code> 或者 <code>false</code>
     * @param processId String     | 必填 | 
     *                 流程ID
     * @param caseId    String     | 必填 | 
     *                 流程实例CASEID
     * @return 工作流最原始的XML数据
     * @throws ServiceException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("processXml")
    public Response getWorkflowXMLData(
            @QueryParam("isRunning") boolean isRunning,
            @QueryParam("processId") String processId,
            @QueryParam("caseId") String caseId) throws ServiceException {
        
        try {
            Map<String, String> ret = new HashMap<String, String>();
            
            String diagramdata = workflowApiManager.getWorkflowXMLForMobile(isRunning, processId, caseId);
            
            ret.put("process_xml", diagramdata);
            
            return ok(ret);
            
        } catch (BPMException e) {
            LOGGER.error("获取流程图数据异常,isRunning:="+isRunning+";processId:="+processId+";caseId:="+caseId, e);
            throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
        }
    }
    
    /**
     * 
     * 新建自由协同，对流程进行<strong> 新增 </strong>节点操作
     * 
     * @param params
     * <blockquote><pre>
     * params.workflowXml String |必填| 
     *            工作流XML或者XML缓存ID(第一次调用这个方法后会生成缓存ID), 如果没有值传""
     * params.orgJson String |必填| 
     *            JSON数组, 新增节点信息, 
     *            示例：[{
     *                   "id":"-8492409909120645741",
     *                   "name":"xuqw1+",
     *                   "excludeChildDepartment":false,
     *                   "includeChild":true,
     *                   "accountId":"-327612323971118692",
     *                   "accountName":"创世纪",
     *                   "entityType":"Member"
     *                }]
     * params.currentNodeId String |必填|
     *            开始节点传 ""
     * params.type String |必填| 
     *            新增节点方式, 取值 0 - 串发， 1 - 并发, 2 - 会签
     * params.currentUserId String |必填| 
     *            数字类型的字符串， 当前登录人员ID
     * params.currentUserName String |必填|
     *            当前登录人员名称
     * params.currentAccountId String |必填|
     *            数字类型的字符串， 当前登录单位ID
     * params.currentAccountName String |必填|
     *            当前登录单位名称
     * params.defaultPolicyId String |必填|
     *            默认节点权限， 示例:collaboration
     * params.defaultPolicyName String |必填|
     *            默认节点权限名称，defaultPolicyId的名字， 示例： 协同
     * params.caseId String |必填| 
     *            数字类型的字符串， 工作流实例ID， 没有时传 -1
     * params.showNodes String |非必填|
     *            节点ID拼装的字符串，只显示的节点信息， 如果传了这个值， 流程的json数据将不全部返回， 
     *            只返回 showNodes指定的节点， 流程图展现时， 由前置节点 + showNodes节点 + 后置节点组成
     * </pre></blockquote>
     * 
     * @return
     * 结果集为一个数组， ret[0]: 工作流原生XML信息的缓存ID， 
     * ret[1] : xml转换后的JSON字符串
     * @throws ServiceException
     * @since V5-6.0
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("processXmlandJson")
    public Response getProcessXmlandJson(Map<String, String> params) throws ServiceException {
        
      //工作流xml串，值为null,就表示全新选人，有值，就是在现有的流程上编辑
        String workflowXml = ParamUtil.getString(params, "workflowXml", "");
        
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        workflowXml = WorkflowUtil.getTempProcessXml(workflowXml);
        
      //[{id:xx,name:xxx,entityType:xxx,accountId:xxx,accountName:xxx}]
        String orgJson = ParamUtil.getString(params, "orgJson", "");
        
      //当前节点Id
        String currentNodeId = ParamUtil.getString(params, "currentNodeId", "");
        
      //0：串发，1：并发，2：会签
        String type = ParamUtil.getString(params, "type", "");
        
      //当前登录人ID
        String currentUserId = ParamUtil.getString(params, "currentUserId", "");
        
      //当前登录人名称
        String currentUserName = ParamUtil.getString(params, "currentUserName", "");
        
      //当前登录人所在单位ID
        String currentAccountId = ParamUtil.getString(params, "currentAccountId", "");
        
      //当前登录人所在单位名称
        String currentAccountName = ParamUtil.getString(params, "currentAccountName", "");
        
      //默认节点权限ID
        String defaultPolicyId = ParamUtil.getString(params, "defaultPolicyId", "");
        
      //默认节点权限名称
        String defaultPolicyName = ParamUtil.getString(params, "defaultPolicyName", "");
        String caseId = ParamUtil.getString(params, "caseId", "");
        try {
            
            List<BPMHumenActivity> addHumanNodes = new ArrayList<BPMHumenActivity>();
            String processXml = workflowApiManager.freeAddNode(workflowXml,orgJson,currentNodeId,type,currentUserId,currentUserName,currentAccountId,
                    currentAccountName,defaultPolicyId,defaultPolicyName, addHumanNodes);
            
            //流程处理过程中，加签 ，当前会签 只显示的局部节点
              String showNodes =  ParamUtil.getString(params, "showNodes", "");
              List<String> showList = new ArrayList<String>();
              if(Strings.isNotBlank(showNodes)){
                  String[] ns = showNodes.split(",");
                  for(String i : ns){
                      showList.add(i);
                  }
                  for(BPMHumenActivity b : addHumanNodes){
                      showList.add(b.getId());
                  }
              }
            
            String processJson= workflowApiManager.getWorkflowJsonForMobile(processXml, showList,caseId);
            
            String[] result= new String[2];
            //重置processXml返回结果
            result[0]= WorkflowUtil.savedProcessXmlTempAndReturnId(null, processXml, currentNodeId, currentUserId, "-1");
            result[1]= processJson;
            return ok(result);
            
        } catch (BPMException e) {
            LOGGER.error("获取流程图数据异常,workflowXml:="+workflowXml+";orgJson:="+orgJson+";currentNodeId:="+currentNodeId+";type:="+type, e);
            throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
        }
    }
    
    
    /**
     * 
     * 自由流程新建时：删除流程节点
     * 
     * @param params
     * <blockquote><pre>
     * params.workflowXml String |必填|
     *          工作流XML或者XML缓存ID(第一次调用这个方法后会生成缓存ID), 如果没有值传""
     * params.currentNodeId String |必填| 
     *          数字类型的字符串， 需要删除的节点ID
     * params.caseId String |必填|
     *          数字类型的字符串， 流程的实例ID， 没有时传-1
     * params.showNodes String |非必传|
     *          节点ID拼装的字符串，只显示的节点信息， 如果传了这个值， 流程的json数据将不全部返回， 
     *          只返回 showNodes指定的节点， 流程图展现时， 由前置节点 + showNodes节点 + 后置节点组成
     * 
     * </pre></blockquote>
     * @return
     * 结果集为一个数组， ret[0]: 工作流原生XML信息的缓存ID， 
     * ret[1] : xml转换后的JSON字符串
     * @throws ServiceException
     *
     * @Since V5-6.0
     *
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("freeDeleteNode")
    public Response freeDeleteNode(Map<String, String> params) throws ServiceException {
        
        String workflowXml = ParamUtil.getString(params, "workflowXml", "");//工作流xml串，值为null,就表示全新选人，有值，就是在现有的流程上编辑
        String currentNodeId= ParamUtil.getString(params, "currentNodeId", "");//要删除的节点Id
        String caseId = ParamUtil.getString(params, "caseId", "");
         
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        workflowXml = WorkflowUtil.getTempProcessXml(workflowXml);
        
        //流程处理过程中，加签 ，当前会签 只显示的局部节点
        String showNodes =  ParamUtil.getString(params, "showNodes", "");
        List<String> showList = new ArrayList<String>();
        if(Strings.isNotBlank(showNodes)){
            String[] ns = showNodes.split(",");
            for(String i : ns){
                showList.add(i);
            }
        }
        
        try {
            
            
            String processXml= workflowApiManager.freeDeleteNode(workflowXml,currentNodeId);
            
            String processJson= workflowApiManager.getWorkflowJsonForMobile(processXml, showList,caseId);
            
            String[] result= new String[2];
            //重置processXml返回结果
            result[0]= WorkflowUtil.savedProcessXmlTempAndReturnId(null, processXml, currentNodeId, String.valueOf(AppContext.currentUserId()), "15");
            result[1]= processJson;
            return ok(result);
        } catch (BPMException e) {
            LOGGER.error("删除流程节点异常,workflowXml:="+workflowXml+";currentNodeId:="+currentNodeId, e);
            throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
        }
    }
    
    
    /**
     * 
     * 替换流程节点， 新建自由协同和处理流程时的流程编辑都调用这个方法
     * 
     * @param params
     * <blockquote><pre>
     * params.workflowXml String |必填|
     *          工作流XML或者XML缓存ID(第一次调用这个方法后会生成缓存ID), 如果没有值传""
     * params.currentNodeId String |必填|
     *          数字类型的字符串， 需要替换的节点ID
     * params.oneOrgJson String |必填|
     *          将节点替换成这个信息， 示例：{
     *                            "id":"-8387780859843164961",
     *                            "name":"xuqw5+",
     *                            "excludeChildDepartment":false,
     *                            "includeChild":true,
     *                            "accountId":"-327612323971118692",
     *                            "accountName":"创世纪",
     *                            "entityType":"Member"
     *                            }
     * params.defaultPolicyId String |必填|
     *            默认节点权限， 示例:collaboration
     * params.defaultPolicyName String |必填|
     *            默认节点权限名称，defaultPolicyId的名字， 示例： 协同
     * params.caseId String |必填|
     *          数字类型的字符串， 流程的实例ID， 没有时传-1
     * params.showNodes String |非必传|
     *          节点ID拼装的字符串，只显示的节点信息， 如果传了这个值， 流程的json数据将不全部返回， 
     *          只返回 showNodes指定的节点， 流程图展现时， 由前置节点 + showNodes节点 + 后置节点组成                   
     * </pre></blockquote>
     * @return
     *  结果集为一个数组， ret[0]: 工作流原生XML信息的缓存ID， 
     * ret[1] : xml转换后的JSON字符串,
     * ret[3] : 替换节点后， 节点ID会变更， 这个数据是替换后的新的节点ID
     * @throws ServiceException
     *
     * @Since V5-6.0
     *
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("freeReplaceNode")
    public Response freeReplaceNode(Map<String, String> params) throws ServiceException {
        String workflowXml = ParamUtil.getString(params, "workflowXml", "");//工作流xml串，值为null,就表示全新选人，有值，就是在现有的流程上编辑
        String currentNodeId= ParamUtil.getString(params, "currentNodeId", "");//要替换的节点Id
        String oneOrgJson= ParamUtil.getString(params, "oneOrgJson", "");//{id:xx,name:xxx,entityType:xxx,accountId:xxx,accountName:xxx,includeChild:true/false}
        String defaultPolicyId= ParamUtil.getString(params, "defaultPolicyId", "");
        String defaultPolicyName= ParamUtil.getString(params, "defaultPolicyName", "");
         String caseId = ParamUtil.getString(params, "caseId", "");
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        workflowXml = WorkflowUtil.getTempProcessXml(workflowXml);
        
        //流程处理过程中，加签 ，当前会签 只显示的局部节点
        String showNodes =  ParamUtil.getString(params, "showNodes", "");
        
        try {
            BPMCase theCase = caseManager.getCase(Long.valueOf(caseId));
            String[] processXml = workflowApiManager.freeReplaceNode(workflowXml,currentNodeId,oneOrgJson,defaultPolicyId,defaultPolicyName,theCase);
            
            List<String> showList = new ArrayList<String>();
            if(Strings.isNotBlank(showNodes)){
                showNodes = showNodes.replace(currentNodeId, processXml[1]);
                String[] ns = showNodes.split(",");
                for(String i : ns){
                    showList.add(i);
                }
            }
            String processJson= workflowApiManager.getWorkflowJsonForMobile(processXml[0], showList,caseId);
            
            String[] result= new String[3];
            //重置processXml返回结果
            result[0]= WorkflowUtil.savedProcessXmlTempAndReturnId(null, processXml[0], currentNodeId, String.valueOf(AppContext.currentUserId()), "16");
            result[1]= processJson;
            result[2]= processXml[1];
            return ok(result);
        } catch (BPMException e) {
            LOGGER.error("替换流程节点异常,workflowXml:="+workflowXml+";currentNodeId:="+currentNodeId+";oneOrgJson:="+oneOrgJson, e);
            throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
        }
    }
    
    /**
     * 
     * 修改节点权限
     * 
     * 
     * @param params
     * <blockquote><pre>
     * params.workflowXml String |必填|
     *          工作流XML或者XML缓存ID(第一次调用这个方法后会生成缓存ID), 如果没有值传""
     * params.currentNodeId String |必填|
     *          数字类型的字符串， 需要替换的节点ID
     * params.updateAll String |非必填|
     *          是否应用到全部节点， 取值 <code>true</code>/<code>false</code>
     * params.updateNodes String |非必填|
     *          指定更新节点ID拼接的字符串， ID之间使用<code>,</code>分隔，如果updateAll为<code>true</code>才生效 
     * params.caseId String |必填|
     *          数字类型的字符串， 流程的实例ID， 没有时传-1
     * params.showNodes String |非必传|
     *          节点ID拼装的字符串，只显示的节点信息， 如果传了这个值， 流程的json数据将不全部返回， 
     *          只返回 showNodes指定的节点， 流程图展现时， 由前置节点 + showNodes节点 + 后置节点组成          
     * params.nodePropertyJson String |必填|
     *          JSON格式字符串， 示例： <strong>示例中没有值的都为非必传，有值的都为必传</strong>
     *               {
     *                 "policyId":"approve",    //节点权限ID
     *                 "policyName":"审批",       //节点权限名称
     *                 "dealTerm":"",           //超期时间
     *                 "remindTime":"",         //提醒时间
     *                 "processMode":"",        //执行模式  
     *                                          //     single - 单人执行 
     *                                          //     multiple - 多人执行  
     *                                          //     all - 全体执行 
     *                                          //     competition - 竞争执行  
     *                                          //     1 - 超级节点阻塞模式 
     *                                          //     0 - 超级节点非阻塞模式
     *                 "matchScope":"",         //匹配范围
     *                 "desc":"",               //节点描述
     *                 "dealTermType":"0",      //超期处理类型(自由流程默认传0：消息提醒)
     *                 "dealTermUserId":"-1",   //超期转指定人ID(自由流程默认传-1)
     *                 "dealTermUserName":"-1", //超期转指定人名称(自由流程默认传-1)
     *                 "includeChild":"true",   //是否包含子部门(主要给部门节点用，其它节点默认为true), 取值 true/false
     *                 "rup":"1",               //角色是否自动向上查找(自由流程默认传1)
     *                 "pup":"1",               //岗位是否自动向上查找(自由流程默认传1)
     *                 "na":"-1",               //无人是否自动跳过(自由流程默认传-1)
     *                 "formApp":"",            //表单ID(自由流程默认传空字符串)
     *                 "formViewOperation":"",  //表单多视图操作权限ID(自由流程默认传空字符串)
     *                 "formField":"",          //表单字段信息(自由流程默认传空字符串)
     *                 "tolerantModel":""       //超级节点容错处理模式(自由流程默认传空字符串)
     *                 }
     * </pre></blockquote>
     * @return 
     * 结果集为一个数组<br/>
     * ret[0]: 工作流原生XML信息的缓存ID<br/>
     * ret[1] : xml转换后的JSON字符串
     * @throws ServiceException
     *
     * @Since V5-6.0
     *
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("freeChangeNodeProperty")
    public Response freeChangeNodeProperty(Map<String, String> params) throws ServiceException {
        
        String workflowXml = ParamUtil.getString(params, "workflowXml", "");//工作流xml串，值为null,就表示全新选人，有值，就是在现有的流程上编辑
        String currentNodeId= ParamUtil.getString(params, "currentNodeId", "");//要修改的节点Id
        String updateAll = ParamUtil.getString(params, "updateAll", "");//是否更新全部节点
        String updateNodes = ParamUtil.getString(params, "updateNodes", "");//更新全部的时候，只更新这些节点， 没有值则更新流程全部节点
        String caseId = ParamUtil.getString(params, "caseId", "");
         
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        workflowXml = WorkflowUtil.getTempProcessXml(workflowXml);
        
        //流程处理过程中，加签 ，当前会签 只显示的局部节点
        String showNodes =  ParamUtil.getString(params, "showNodes", "");
        List<String> showList = new ArrayList<String>();
        if(Strings.isNotBlank(showNodes)){
            String[] ns = showNodes.split(",");
            for(String i : ns){
                showList.add(i);
            }
        }
        
        String nodePropertyJson= ParamUtil.getString(params, "nodePropertyJson", "");
        try {
            
            List<String> updateNodesList = null;
            if(Strings.isNotBlank(updateNodes)){
                updateNodesList =  Arrays.asList(updateNodes.split(","));
            }else{
                updateNodesList = Collections.emptyList();
            }
            BPMCase theCase = caseManager.getCase(Long.valueOf(caseId));
            String processXml= workflowApiManager.freeChangeNodeProperty(workflowXml,currentNodeId,nodePropertyJson, "true".equals(updateAll), updateNodesList,theCase);
            
            String processJson= workflowApiManager.getWorkflowJsonForMobile(processXml, showList,caseId);
            
            String[] result= new String[2];
            //重置processXml返回结果
            result[0]= WorkflowUtil.savedProcessXmlTempAndReturnId(null, processXml, currentNodeId, String.valueOf(AppContext.currentUserId()), "16");
            result[1]= processJson;
            return ok(result);
        } catch (BPMException e) {
            LOGGER.error("替换流程节点异常,workflowXml:="+workflowXml+";currentNodeId:="+currentNodeId+";nodePropertyJson:="+nodePropertyJson, e);
            throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
        }
    }
    
    /**
     * 
     * 校验流程是否可以取回
     * 
     * @param appName String |必填| 
     *             模块名称， 示例：collaboration  
     * @param processId String |必填|
     *             数字类型的字符串， 流程ID
     * @param activityId String |必填|
     *             数字类型的字符串， 取回节点ID
     * @param workitemId String |必填|
     *             数字类型的字符串， 工作流的事项ID，对应ctp_affair的subObject字段
     * @return
     *  示例：
     * <blockquote><pre>
     *  {
     *    "canTakeBack":true, //是否可以取回， 值为 true/false
     *    "state":"0"         //节点状态,
     *                        //     -1 - 程序或数据发生异常,不可以取回
     *                        //      0 - 可以取回
     *                        //      1 - 当前流程已经结束,不可以取回
     *                        //      2 - 后面节点任务事项已处理完成,不可以取回
     *                        //      3 - 当前节点触发的子流程已经结束,不可以取回
     *                        //      4 - 当前节点触发的子流程中已核定通过,不可以取回
     *                        //      5 - 当前节点是知会节点,不可以取回
     *                        //      6 - 当前节点为核定节点,不可以取回
     *                        //      8 - 当前流程处于指定回退状态，你不能进行此操作！
     *                        //      9 - 该操作不能恢复，是否进行删除操作
     *                        //      10 - 删除失败，请联系管理员解决！
     *                        //      11 - 取消收藏失败，请联系管理员解决！
     *                        //      12 - 收藏失败，请联系管理员解决！
     *                        //      13 - 修改原意见
     *                        //      14 - 不修改原意见
     *  }
     * </pre></blockquote>
     * @throws ServiceException
     *
     * @Since V5-6.0
     *
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("canTakeBack")
    public Response canTakeBack(
            @QueryParam("appName") String appName,
            @QueryParam("processId") String processId,
            @QueryParam("activityId") String activityId,
            @QueryParam("workitemId") String workitemId) throws ServiceException {
        
        try {
            String message = workflowApiManager.canTakeBack(appName, processId, activityId, workitemId);
            return ok(message);
            
        } catch (BPMException e) {
            LOGGER.error("是否可以取回ajax判断,appName:="+appName+";processId:="+processId+";activityId:="+activityId+";workitemId:="+workitemId, e);
            throw new ServiceException(new OAInterfaceException(0, e.getMessage()));
        }
    }
    
    
    /**
     * 流程预提交接口
     * 
     * @param param
     * <blockquote><pre>
     * param._json_params Map |必填|
     *            JSON对象
     *            param._json_params.context String |必填|
     *                 JSON格式字符串
     *            param._json_params.cpMatchResult String |必填|
     *                 JSON格式字符串
     *                 
     * 完整示例：以下例子中，有值的为必填参数， 没有值的为非必填
     * {
     *      "_json_params" : {
     *          "context" : {
     *              "appName" : "collaboration",//应用CODE
     *              "processXml" : "",          //工作流XML或者XML缓存ID
     *              "processId" : "-4975991029776562006",//流程ID, 如果这个字段有值， processTemplateId字段需要设置为""
     *              "caseId" : "7095770068904264376",    //流程实例ID
     *              "currentActivityId" : "14913792340221",//当前节点ID
     *              "currentWorkitemId" : "102513515865853721",//流程事项ID， 对应ctp_affair的sub_object_id
     *              "currentUserId" : "-8492409909120645741", //当前登录用户ID
     *              "currentAccountId" : "-327612323971118692",//当前登录单位ID
     *              "formData" : "9003220480554971505",//表单数据ID， 非表单流程传""
     *              "mastrid" : "9003220480554971505",//表单数据ID，  非表单流程传""
     *              "processTemplateId" : "", //流程模板ID， 发起/修改模版流程时需要设置这个字段
     *              "currentWorkItemIsInSpecial" : false,//流程当前是否处于指定回退状态， 取值true/false
     *              "isValidate" : "true", //流程是否已经校验过了, 取值"true"/"false"
     *              "debugMode" : false, //是否为调试， 取值false
     *              "bussinessId" : "5019625158063722803", //业务ID
     *              "matchRequestToken" : "H5--8492409909120645741-1491566375810"//流程缓存CODE，  需要唯一， 预提交后需要提交流程，需要将数据放入工作流值域中
     *          },
     *          "cpMatchResult" : {
     *              "allNotSelectNodes" : [],//为选择分支节点集合
     *              "allSelectNodes" : [],//已选分支节点集合
     *              "allSelectInformNodes" : [],//已选知会节点集合
     *              "pop" : false,//是否弹出选人分支， 取值false
     *              "token" : "",//非必传
     *              "last" : "false", 是否为最后节点最后一个人处理
     *              "alreadyChecked" : "false"//是否已经校验过， 取值 "true"/"false"
     *          }
     *      }
     *  }
     * </pre></blockquote>
     * @return
     * @throws ServiceException
     *
     * @Author      : xuqw
     * @Date        : 2016年6月6日下午4:14:13
     * @Since V5-6.0
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transBeforeInvokeWorkFlow")
    public Response transBeforeInvokeWorkFlow(Map<String, Object> param) throws ServiceException {
        Map jsonStr = (Map) param.get("_json_params");
        WorkflowBpmContext context = JSONUtil.parseJSONString(jsonStr.get("context").toString(), WorkflowBpmContext.class);
        CPMatchResultVO cpMatchResult = JSONUtil.parseJSONString((String) jsonStr.get("cpMatchResult"),
                CPMatchResultVO.class);

        CPMatchResultVO vo = null;
        try {
            
            
            //有流程ID，就不在传templateId
            /*String processId = context.getProcessId();
            if(Strings.isNotBlank(processId)){
                context.setProcessTemplateId("");
            }*/
            
            //H5参数处理：从流程临时表中取出流程变更之后的processXml
            context.setProcessXml(WorkflowUtil.getTempProcessXml(context.getProcessXml()));
            
            vo = workflowApiManager.transBeforeInvokeWorkFlow(context, cpMatchResult);
        } catch (NoSuchWorkitemException e) {
            LOGGER.error("流程预提交失败", e);
            vo = new CPMatchResultVO();
            vo.setCanSubmit("true");
            vo.setCannotSubmitMsg("");
            vo.setAlreadyChecked("true");
        } catch (BPMException e) {
            LOGGER.error("流程预提交失败", e);
            vo = new CPMatchResultVO();
            vo.setCanSubmit("false");
            vo.setCannotSubmitMsg("预提交异常");
        }
        vo.setToken("WORKFLOW");
        
        return ok(toJSON(vo));
    }
    
    /**
     * 
     * 自动跳过节点分支被选中时的校验：后面是否会有分支或选人的节点
     * 
     * @param param
     * <blockquote><pre>
     * param._json_params Map |必填|
     *            JSON对象
     *            param._json_params.context String |必填|
     *                 {@link #transBeforeInvokeWorkFlow(Map)}参数中的context属性
     *            param._json_params.checkedNodeId String |必填|
     *                 校验节点ID
     *            param._json_params.preAllSelectNodesForBranchCheck String |必填|
     *                 JSON格式字符串, 
     *            param._json_params.preAllNotSelectNodesForBranchCheck String |必填|
     *                 JSON格式字符串, 
     *            param._json_params.preAllSelectInformNodesForBranchCheck String |必填|
     *                 JSON格式字符串, 
     *            param._json_params.currentSelectInformNodesForBranchCheck String |必填|
     *                 JSON格式字符串, 
     *                 
     * </pre></blockquote>
     * @return
     * <blockquote><pre>
     * {
     *    "result" : "true"// 如果需要选人返回"true", 如果不需要返回"false"
     * }
     * <pre></blockquote>
     * @throws ServiceException
     *
     * @Author      : xuqw
     * @Date        : 2016年6月11日下午9:58:36
     * @Since V5-6.0
     */
    /*@POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("transCheckBrachSelectedWorkFlow")
    public Response transCheckBrachSelectedWorkFlow(Map<String, Object> param) throws ServiceException {
        
        
        Map jsonStr = (Map) param.get("_json_params");
        WorkflowBpmContext context = JSONUtil.parseJSONString(jsonStr.get("context").toString(), WorkflowBpmContext.class);

        String checkedNodeId = String.valueOf(jsonStr.get("checkedNodeId"));
        
        Set<String> allSelectNodes = JSONUtil.parseJSONString(jsonStr.get("preAllSelectNodesForBranchCheck").toString(), Set.class);
        Set<String> allNotSelectNodes = JSONUtil.parseJSONString(jsonStr.get("preAllNotSelectNodesForBranchCheck").toString(), Set.class);
        Set<String> allSelectInformNodes = JSONUtil.parseJSONString(jsonStr.get("preAllSelectInformNodesForBranchCheck").toString(), Set.class);
        Set<String> currentSelectInformNodes = JSONUtil.parseJSONString(jsonStr.get("currentSelectInformNodesForBranchCheck").toString(), Set.class);
        
        boolean check = false;
        try {
            //H5参数处理：从流程临时表中取出流程变更之后的processXml
            context.setProcessXml(WorkflowUtil.getTempProcessXml(context.getProcessXml()));
            
            check = workflowApiManager.transCheckBrachSelectedWorkFlow(context, checkedNodeId, allSelectNodes, 
                                                 allNotSelectNodes, allSelectInformNodes, currentSelectInformNodes);
        } catch (BPMException e) {
            LOGGER.error("预提交，校验自动跳过节点是否选人失败", e);
        }
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("result", String.valueOf(check));
        
        return ok(ret);
    }*/
    
    
    /**
     * 增加节点， 包括 加签/当前会签/知会/多级会签
     * 
     * @param param
     * <pre>
     *   param.processId String | 必填| 
     *                   数字类型的字符串, 流程ID
     *   param.caseId String | 必填| 
     *                   数字类型的字符串， 当前待办节点的ID
     *   param.currentActivityId String | 必填|
     *                   数字类型的字符串， 当前操作节点ID
     *   param.targetActivityId  String | 必填| 
     *                   数字类型的字符串， 目标活动节点Id（将要执行加签或知会、会签动作的节点Id）
     *   param.userId  String | 必填| 
     *                   数字类型的字符串，执行加签操作的用户Id
     *   param.changeType String | 必填| 
     *                   流程修改类型（1加签、2知会、3当前会签、5多级会签、6传阅）
     *   param.message String | 必填| 
     *                   流程修改信息
     *   param.baseProcessXML String | 非必填| 
     *                   工作流XML或者XML缓存ID， 该参数可以为null，此时针对id=processId的工作流执行流程修改操作
     *   param.baseReadyObjectJSON String | 非必填| 
     *                   当前会签信息， 由后生成， 加签/知会不传值,
     *                   该参数可以为null，此时表示没有针对当前流程的Ready状态的节点 如果不为空，并且格式符合BPMProcess的话，在该xml的基础上执行流程修改操作。
     *   param.messageDataList String | 必填|
     *                   发送消息用的json格式字符串， 由后台生成
     *   param.changeMessageJSON String | 必填|
     *                   加签/减签等操作数据， 后台生成
     *  param.showNodes String |非必填|
     *            节点ID拼装的字符串，只显示的节点信息， 如果传了这个值， 流程的json数据将不全部返回， 
     *            只返回 showNodes指定的节点， 流程图展现时， 由前置节点 + showNodes节点 + 后置节点组成
     * <pre>
     * 
     * @return
     * 结果集为数组<br/>
     * ret[0]: 工作流原生XML信息的缓存ID<br/>
     * ret[1]: 当前会签信息， 对应readyObjectJSON<br/>
     * ret[2]: 是否增加节点成功, 取值 "true"/"false"<br/>
     * ret[3]: 增加节点不成功的错误信息<br/>
     * ret[4]: 节点变动消息相关信息，对应process_message_data/messageDataList<br/>
     * ret[5]: 节点变动相关信息， 对应processChangeMessage/changeMessageJSON<br/>
     * ret[6]: 流程节点信息的JSON数据<br/>
     * ret[7]: 如果是多级会签， 这个值返回的是会签合并节点的ID<br/>
     * ret[8]: 节点是否只读， 取值"true"/"false"<br/>
     * 
     * @throws ServiceException
     *
     * @Author      : xuqw
     * @Date        : 2016年6月22日下午4:56:02
     * @Since V5-6.0
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("addNode")
    public Response addNode(Map<String, Object> param) throws ServiceException {
        
        String processId = ParamUtil.getString(param, "processId", "");
        String caseId = ParamUtil.getString(param, "caseId", "");
        String currentActivityId = ParamUtil.getString(param, "currentActivityId", "");
        String targetActivityId = ParamUtil.getString(param, "targetActivityId", "");
        String userId = ParamUtil.getString(param, "userId", "");
        //int changeType = ParamUtil.getString(param, "changeType", "");
        String _changeType = ParamUtil.getString(param, "changeType", "0");
        int changeType = Integer.valueOf(_changeType);//串发
        int action = Integer.valueOf(ParamUtil.getString(param, "action", "9"));
        String _message = ParamUtil.getString(param, "message", "{}");
        Map<Object, Object> message = JSONUtil.parseJSONString(_message, Map.class);
        
        String baseProcessXML = ParamUtil.getString(param, "baseProcessXML", "");
        String baseReadyObjectJSON = ParamUtil.getString(param, "baseReadyObjectJSON", "");
        String messageDataList = ParamUtil.getString(param, "messageDataList", "");
        String changeMessageJSON = ParamUtil.getString(param, "changeMessageJSON", "");
        
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        baseProcessXML = WorkflowUtil.getTempProcessXml(baseProcessXML);
        
        String[] ret = new String[9];
        try {
            
            //用于保存新增的节点ID
            List<BPMHumenActivity> addHumanNodes = new ArrayList<BPMHumenActivity>();
            
            String[] addRet = workflowApiManager.addNode(processId, currentActivityId, targetActivityId
                    , userId, changeType, message, baseProcessXML, baseReadyObjectJSON
                    , messageDataList, changeMessageJSON, addHumanNodes);
            
            for(int i = 0; i < addRet.length; i++){
                ret[i] = addRet[i];
            }
            
            if("true".equals(addRet[2])){
                
              //流程处理过程中，加签 ，当前会签 只显示的局部节点
                String showNodes =  ParamUtil.getString(param, "showNodes", "");
                List<String> showList = new ArrayList<String>();
                if(Strings.isNotBlank(showNodes)){
                    String[] ns = showNodes.split(",");
                    for(String i : ns){
                        showList.add(i);
                    }
                    
                    for(BPMHumenActivity b : addHumanNodes){
                        showList.add(b.getId());
                    }
                }
                
                
                String processJson = workflowApiManager.getWorkflowJsonForMobile(addRet[0], showList,caseId);
                ret[6] = processJson;
                
                //多级会签记录后面的节点
                if(changeType == ChangeType.MultistageAsign.getKey() 
                        && !showList.isEmpty()){
                    ret[7] = showList.get(showList.size() - 1);
                }
            }
            
            //重置processXml返回结果
            ret[0] = WorkflowUtil.savedProcessXmlTempAndReturnId(processId, ret[0], currentActivityId, userId, String.valueOf(action));
            
            boolean isFormReadOnly = workflowApiManager.isNodeFormReadonly(currentActivityId, processId);
            ret[8] = String.valueOf(isFormReadOnly);
            
            //项目：徐矿 移动端增加指定公开角色 zelda 2019年12月11日19:47:20 start
           
            try {
            	 String openRoleStr = AppContext.getSystemProperty("xkjt.openRole");
                 OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
                 User currentUser = AppContext.getCurrentUser();
                 V3xOrgRole openRole = orgManager.getRoleByName(openRoleStr, currentUser.getLoginAccount());
                 if(openRole == null) {
                	 LOGGER.error("zelda-为获取当前人员登陆单位的指定公开角色:LoginAccount " + currentUser.getLoginAccount() + ",roleName:" + openRoleStr);
                 }
            	 if(addRet.length >= 6) {
                 	String nodeJsonStr = addRet[5];
                 	if(Strings.isNotBlank(nodeJsonStr)) {
                 		JSONObject nodeJson = (JSONObject) JSONUtil.parseJSONString(nodeJsonStr);
                 		if(nodeJson != null) {
                 			JSONArray affertNdoes = (JSONArray) nodeJson.get("nodes");
                 			if(affertNdoes != null && affertNdoes.size() > 0) {
                 				XkjtDao xkjtDao = (XkjtDao) AppContext.getBean("xkjtDao");
                 				List<XkjtOpenMode> xkjtOpenModes = new ArrayList<XkjtOpenMode>();
                 				for(int i = 0; i < affertNdoes.size(); i++) {
                 					JSONObject affertNdoe = (JSONObject) affertNdoes.get(i);
                 					Boolean added = (Boolean) affertNdoe.get("added");
                 					if(added == true) {
                 						XkjtOpenMode xkjtOpenMode = new XkjtOpenMode();
                 						xkjtOpenMode.setIdIfNew();
                 						xkjtOpenMode.setNodeId(affertNdoe.getLong("id"));
                 						xkjtOpenMode.setRoleIds(String.valueOf(openRole.getId()));
                 						xkjtOpenMode.setIsDeleted(0);
                 						xkjtOpenModes.add(xkjtOpenMode);
                 					}
                 					
                 				}
                 				xkjtDao.saveOpenModes(xkjtOpenModes);
                 			}
                 		}
                 		
                 	}
                 }
			} catch (Exception e) {
				 LOGGER.error("zelda-添加指定公开角色错误", e);
			}
           
            //项目：徐矿 移动端增加指定公开角色 zelda 2019年12月11日19:47:20 start
            
        } catch (BPMException e) {
            LOGGER.error("H5加签异常", e);
        }
        
        return ok(ret);
    }
    
    /**
     * 
     * 获取可以减签的节点列表
     * 
     * @param processId String |必填| 
     *           数字类型的字符串， 工作流ID
     * @param nodeId String |必填| 
     *           数字类型的字符串， 当前节点ID
     * @param processXML String | 非必填| 
     *            工作流XML或者XML缓存ID， 该参数可以为null，此时针对id=processId的工作流执行流程修改操作
     * @return
     * <p>基于nodeId为参照可以减签的节点列表。</p>
     * <blockquote><pre>
     * [
     *   {
     *     "id" : "14913792412524",//节点ID
     *     "name" : "单人",          //节点名称
     *     "type" : "user",         //节点类型
     *     "policyName" : "协同",    //节点权限名称
     *     "isAdd" : "true/false",    //是否是加签节点
     *     "postName" : "研发", //如果节点是人员， 返回岗位信息
     *     "memberId" : "-8387780859843164961" //如果节点是人员，返回人员ID
     *   }
     * ]
     * </pre></blockquote>
     * 
     * @throws BPMException
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     *
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("preDeleteNodeFromDiagram")
    public Response preDeleteNodeFromDiagram(@QueryParam("processId") String processId, 
                                             @QueryParam("nodeId") String nodeId,
                                             @QueryParam("processXML") String processXML) throws BPMException {
        
        //从缓存中获取xml
        processXML = WorkflowUtil.getTempProcessXml(processXML);
        
        ProcessEngine engine = WAPIFactory.getProcessEngine("Engine_1");
        List<BPMHumenActivity> activityList = engine.preDeleteNode(processId, nodeId, processXML);
        
        List<Map<String, String>> ret = new ArrayList<Map<String,String>>();
        
        //workflow.deletePeople.noChildren
        
        if(Strings.isNotEmpty(activityList)) {
            OrgManager orgManager = (OrgManager)AppContext.getBean("orgManager");
            for(BPMHumenActivity node : activityList){
                Map<String, String> m = new HashMap<String, String>();
                m.put("id", node.getId());
                String nodeName = node.getBPMAbstractNodeName();
                if(Strings.isNotBlank(node.getCustomName())){
                	nodeName = node.getCustomName();
                }
                m.put("name", nodeName);
                m.put("policyName", node.getSeeyonPolicy().getName());
                m.put("isAdded", String.valueOf(node.isAdded()));
                if(Strings.isNotEmpty(node.getActorList())) {
                    BPMActor actor = (BPMActor)node.getActorList().get(0);
                    String type = actor.getParty().getType().id;
                    if("Department".equals(type)) {
                        
                    } else if("user".equals(type)) {
                        try {
                            V3xOrgMember member = orgManager.getMemberById(Long.parseLong(actor.getParty().getId()));
                            if(member != null) {
                                V3xOrgPost post = orgManager.getPostById(member.getOrgPostId());
                                if(post != null) {
                                    m.put("postName", post.getName());
                                }
                                m.put("memberId", String.valueOf(member.getId()));
                            }
                        } catch(Exception e) {}
                    }
                    m.put("type", type);
                } else {
                    m.put("type", "user");
                    m.put("name", node.getBPMAbstractNodeName());
                }
                ret.add(m);
            }
        }
        return ok(ret);
    }
    
    /**
     * 待办修改流程时删除节点：减签/删除
     * 
     * @param param
     * <blockquote><pre>
     * param.processId String | 必填| 
     *            数字类型的字符串, 流程ID
     * param.caseId String | 必填| 
     *            数字类型的字符串， 当前待办节点的ID
     * param.currentActivityId String | 必填|
     *            数字类型的字符串，当前操作节点ID
     * param.userId  String | 必填| 
     *            数字类型的字符串，执行加签操作的用户Id
     * param.activityIdList String |必填|
     *            id数组转换后的字符串 
     * param.baseProcessXML String | 非必填| 
     *            工作流XML或者XML缓存ID， 该参数可以为null，此时针对id=processId的工作流执行流程修改操作
     * param.messageDataList String | 必填|
     *            发送消息用的json格式字符串， 由后台生成
     * param.changeMessageJSON String | 必填|
     *            加签/减签等操作数据， 后台生成
     *  param.showNodes String |非必填|
     *            节点ID拼装的字符串，只显示的节点信息， 如果传了这个值， 流程的json数据将不全部返回， 
     *            只返回 showNodes指定的节点， 流程图展现时， 由前置节点 + showNodes节点 + 后置节点组成
     * param.summaryId String |必填|
     *            业务ID， 如协同ID/公文ID
     * param.affairId
     *            待办事项ID
     * </pre></blockquote>
     * 
     * @return 
     * 返回结果为数组<br/>
     * ret[0] : 工作流原生XML信息的缓存ID<br/>
     * ret[1] : 节点变动消息相关信息，对应process_message_data/messageDataList<br/>
     * ret[2] : 节点变动相关信息， 对应processChangeMessage/changeMessageJSON<br/>
     * ret[3] : 流程节点信息的JSON数据<br/>
     * 
     * @throws BPMException 抛出异常
     * @Since V5-6.1
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("deleteNode")
    public Response deleteNode(Map<String, Object> param) throws ServiceException {
        
        String processId = ParamUtil.getString(param, "processId", "");
        String caseId = ParamUtil.getString(param, "caseId", "");
        String currentActivityId = ParamUtil.getString(param, "currentActivityId", "");
        String userId = ParamUtil.getString(param, "userId", "");
        
        String _activityIdList = ParamUtil.getString(param, "activityIdList", "");
        List<String> activityIdList = Collections.emptyList();
        if(_activityIdList != null){
            activityIdList = JSONUtil.parseJSONString(_activityIdList, List.class);
        }
        String baseProcessXML = ParamUtil.getString(param, "baseProcessXML", "");
        String messageDataList = ParamUtil.getString(param, "messageDataList", "");
        String changeMessageJSON = ParamUtil.getString(param, "changeMessageJSON", "");
        
        String summaryId = ParamUtil.getString(param, "summaryId", "");
        String affairId = ParamUtil.getString(param, "affairId", "");
        
        //H5参数处理：从流程临时表中取出流程变更之后的processXml
        baseProcessXML = WorkflowUtil.getTempProcessXml(baseProcessXML);
        
        String[] ret = new String[4];
        try {
            
            String[] delRet = workflowApiManager.deleteNode(processId, currentActivityId, userId, activityIdList
                    , baseProcessXML, messageDataList, changeMessageJSON, summaryId, affairId);
            
            for(int i = 0; i < delRet.length; i++){
                ret[i] = delRet[i];
            }
            
                
          //流程处理过程中，加签 ，当前会签 只显示的局部节点
            String showNodes =  ParamUtil.getString(param, "showNodes", "");
            List<String> showList = new ArrayList<String>();
            if(Strings.isNotBlank(showNodes)){
                String[] ns = showNodes.split(",");
                for(String i : ns){
                    showList.add(i);
                }
            }
            
            
            String processJson = workflowApiManager.getWorkflowJsonForMobile(delRet[0], showList,caseId);
            ret[3] = processJson;
            
            //重置processXml返回结果
            ret[0] = WorkflowUtil.savedProcessXmlTempAndReturnId(processId, ret[0], currentActivityId, userId, "15");
            
        } catch (BPMException e) {
            LOGGER.error("H5加签异常", e);
        }
        
        return ok(ret);
    }
        
    /**
     * 增加流程锁
     * 
     * @param processId String |必填| 
     *           数字类型的字符串， 工作流ID
     * @param action String |必填|
     *           数字类型的字符串， 流程锁类型
     *<blockquote><pre>
     *       3, //加签
     *       4, //减签
     *       5,//当前会签
     *       6, //知会
     *       7, //传阅
     *       8, //多级会签
     *       9, //回退
     *       10,//指定回退
     *       11, //终止
     *       12, //撤销
     *       13, //取回
     *       14, //提交
     *       15,//修改正文
     *       20 //移交
     *</pre></blockquote>
     * @return
     * 结果集为数组</br>
     * ret[0] : 是否加锁成功， 取值"true"/"false"<br/>
     * ret[1] : 没有获取到锁时的提示信息<br/>
     * ret[2] : 如果流程被其他人锁住， 该字段返回锁的所属人名称<br/>
     * @Since V5-6.0
     * @Author      : xuqw
     * @Date        : 2017年4月7日下午11:59:01
     *
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("lockH5Workflow")
    public Response lockH5Workflow(@QueryParam("processId") String processId, @QueryParam("action") Integer action) {
        String[] result = new String[3];
        try {
            String from = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
            result = workflowApiManager.lockWorkflowProcess(processId, String.valueOf(AppContext.currentUserId()), action, from);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(),e);
        }
        return ok(result);
    }
    
    /**
     * 
     * 释放流程锁
     * 
     * @param processId String |必填| 
     *           数字类型的字符串， 工作流ID
     * @param currentUserId String |必填|
     *           数字类型的字符串， 流程锁拥有者ID
     * @param action String |必填|
     *           数字类型的字符串， 流程锁类型 {@link #lockH5Workflow(String, Integer)}的action参数
     *
     * @return
     * 结果集为数组,<br/> 
     * ret[0] : 值为"true"/"false", "true" - 解锁成功， "false" - 解锁失败,<br/> 
     * ret[1] : 解锁失败原因
     * 
     * @throws ServiceException
     *
     * @Since V5-6.0
     *
     */
  /*  @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("releaseWorkflow")
    public Response releaseWorkflow(
            @QueryParam("processId") String processId,
            @QueryParam("currentUserId") String currentUserId,
            @QueryParam("action") String action) throws ServiceException {
        
        String[] ret = null;
        
        try {
            ret = workflowApiManager.releaseWorkFlowProcessLock(processId, currentUserId, Integer.parseInt(action));
        } catch (NumberFormatException e) {
            LOGGER.error("解锁失败, processId|userId|action=" + processId + "|" + currentUserId + "|" + action, e);
        } catch (BPMException e) {
            LOGGER.error("解锁失败, processId|userId|action=" + processId + "|" + currentUserId + "|" + action, e);
        }
        
        if(ret == null){
            ret = new String[]{"false", "解锁异常"};
        }
        
        return ok(ret);
    }*/
    
    /**
     * 
     * 释放流程锁
     * 
     * @param processId String |必填| 
     *           数字类型的字符串， 工作流ID
     * @param action String |必填|
     *           数字类型的字符串， 流程锁类型 {@link #lockH5Workflow(String, Integer)}的action参数
     *
     * @return
     * 结果集为数组,<br/> 
     * ret[0] : 值为"true"/"false", "true" - 解锁成功， "false" - 解锁失败,<br/> 
     * ret[1] : 解锁失败原因
     * 
     * @throws ServiceException
     *
     * @Since V5-6.0
     *
     */
    @GET
    @Path("unLockH5Workflow")
    public Response unLockH5Workflow(@QueryParam("processId") String processId, @QueryParam("action") Integer action) {
        
        String[] ret = null;
        
        try {
            if(action!= null && action.intValue()!=-1) {
                ret = workflowApiManager.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()), action);
            } else {
                ret = workflowApiManager.releaseWorkFlowProcessLock(processId, String.valueOf(AppContext.currentUserId()));
            }
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(),e);
        }
        
        if(ret == null){
            ret = new String[]{"false", "Unlock workflow error!"};
        }
        
        return ok(ret);
    }
    
    /**
     * 校验流程是否可以加签/减签/当前会签等修改流程的操作
     * 
     * @param workitemId String |必填| 流程事项ID
     * 
     * @return
     * 结果集为对象<br/>
     * canChange : 是否可以修改流程，取值"true"/"false", "true" - 可以修改流程， "false" - 不可以修改流程<br/>
     * msg : 不能修改流程的文字说明<br/>
     * 
     * @Since V5-6.0
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("canChangeNode")
    public Response canChangeNode(@QueryParam("workitemId") String workitemId) {
        String[] result = new String[2];
        Map<String,Object> ret = new HashMap<String,Object>();
        try {
            result = WFAjax.canChangeNode(workitemId);
            ret.put("canChange", result[0]);
            ret.put("msg", result[1]);
        } catch (BPMException e) {
            LOGGER.error(e.getMessage(),e);
        }
        return ok(ret);
    }
    
    /**
     * 多级会签选择部门时，只获取部门下面的 部门收发员/部门管理员/
     * 
     * @param deptIds 部门ID， 多个ID使用 <code>,</code>分隔
     * @return
     * 以部门节点为key的JSON对象
     * {
     *     "1159351569891870542" : [ {
     *       "accountId" : "-327612323971118692",
     *       "accountName" : "",
     *       "entityType" : "Member",
     *       "excludeChildDepartment" : "false",
     *       "name" : "部门公文收发员　xuqw1+",
     *       "id" : "-8492409909120645741"
     *     }]
     *   }
     * @throws ServiceException
     *
     * @Author      : xuqw
     * @Date        : 2016年7月11日下午10:00:19
     * @Since A8-V5 6.1
     *
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("multiSignDeptMembers")
    public Response multiSignDeptMembers(@QueryParam("deptIds") String deptIds) throws ServiceException {
        
        Map<String, List<Map<String, String>>> ret = new HashMap<String, List<Map<String, String>>>();
        
        if(Strings.isNotBlank(deptIds)){
            StringBuilder ds = new StringBuilder();
            String[] ids = deptIds.split(",");
            for(String id : ids){
                ds.append(V3xOrgEntity.ORG_ID_DELIMITER).append(V3xOrgEntity.ORGENT_TYPE_DEPARTMENT).append("|").append(id);
            }
            List<WFMoreSignSelectPerson> msps = workflowApiManager.findMoreSignPersons(ds.toString().substring(1));
            
            for(WFMoreSignSelectPerson msp : msps){
                
                List<V3xOrgMember> selPersons = msp.getSelPersons();
                V3xOrgEntity       selObj = msp.getSelObj();
                
                List<Map<String, String>> ms = new ArrayList<Map<String,String>>();
                
                for(V3xOrgMember m : selPersons){
                    
                    Map<String, String> memberMap = new HashMap<String, String>();
                    memberMap.put("accountId", m.getOrgAccountId().toString());
                    memberMap.put("accountName", "");
                    memberMap.put("entityType", V3xOrgEntity.ORGENT_TYPE_MEMBER);
                    memberMap.put("excludeChildDepartment", "false");
                    memberMap.put("id", m.getId().toString());
                    memberMap.put("name", m.getName());
                    ms.add(memberMap);
                }
                ret.put(selObj.getId().toString(), ms);
            }
        }
        
        
        return ok(ret);
    }
    
    
    /**
     * 
     * 校验是否可以指定回退到某个节点
     * 
     * @param params
     * <blockquote><pre>
     * params.processId String | 必填| 
     *            数字类型的字符串, 流程ID
     * params.caseId String | 必填| 
     *            数字类型的字符串， 当前待办节点的ID
     * params.currentSelectedNodeId String | 必填| 
     *            数字类型的字符串，被指定回退的节点ID
     * params.currentSelectedNodeName String | 非必填| 
     *            被指定回退的节点名称
     * params.currentStepbackNodeId String | 必填| 
     *            当前指定回退的节点ID
     * params.initialize_processXml String | 非必填| 
     *            工作流XML或者XML缓存ID， 该参数可以为null，此时针对id=processId的工作流执行流程修改操作
     * params.permissionAccountId String | 必填| 
     *            基于这个单位ID进行权限校验/获取配置
     * params.configCategory String |必填|
     *            应用分类， 示例：collaboration
     * </pre></blockquote>
     * 
     * @return
     * 返回结果集为数组<br/>
     * ret[0] : 校验是否通过, "true"/"false"<br/>
     * ret[1] : 校验不通过时的提示信息<br/>
     * ret[2] : 值为 "true"/"false" "true" - 当前节点与被选择节点之间存在分支条件<br/>
     * ret[3] : 值为 "true"/"false" "true" - 被回退的节点与当前处理节点之间有已办的交换类型节点，不能选择！<br/>
     * ret[4] : 值为 "true"/"false" "true" - 被回退的节点与当前处理节点之间有已办的核定节点，不能回退<br/>
     * ret[5] : 值为 "true"/"false" "true" - 被选择的节点与当前处理节点之间有已办的表单审核节点，不能回退<br/>
     * ret[6] : 值为 "true"/"false" "true" - 有子流程结束节点<br/>
     * ret[7] : 值为 "true"/"false"  "true" - 当前流程为新流程，不允许选择开始节点进行指定回退操作！<br/>
     * ret[8] : 值为 "true"/"false" "true" - 当前节点与被选择节点之间存在子流程<br/>
     * ret[9] : 值为 "true"/"false" "true" - 被回退节点含子流程且已结束不允许被回退,是否进行提交操作, 需要先判断ret[6] == "true"<br/>
     *                            "false" - 被回退的节点与当前处理节点之间有子流程触发节点，且触发的子流程已结束,不能回退, 需要先判断ret[6] == "true"<br/>
     * ret[10] : 值为 "true"/"false" "true" - 是否为该孩子节点<br/>
     * ret[11] : 值为 "true"/"false" "true" - 节点匹配不到人，不能回退<br/>
     * ret[12] : 值为 "true"/"false" "true" - 该流程的子流程已核定通过，不能回退！<br/>
     * ret[13] : 值为 "true"/"false" "true" - currentSelectedNodeId节点无效<br/>
     * ret[14] : 值为 null 				  -	暂时没用 <br/>
     * ret[15] : 值为 "true"/"false" "false" - 是否设置了不允许回退<br/>
     * ret[16] : 值为 "true"/"false" "false" - 超级节点阻塞模式不能被跳过<br/>
     * @throws ServiceException
     * @throws BPMException
     *
     * @Since A8-V5 6.1
     *
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("validateCurrentSelectedNode")
    public Response validateCurrentSelectedNode(Map<String, String> params) throws ServiceException, BPMException {
        
        String caseId = params.get("caseId");
        String currentSelectedNodeId = params.get("currentSelectedNodeId");
        String currentSelectedNodeName = params.get("currentSelectedNodeName");
        String currentStepbackNodeId = params.get("currentStepbackNodeId");
        String initialize_processXml = params.get("initialize_processXml");
        String permissionAccountId = params.get("permissionAccountId");
        String configCategory = params.get("configCategory");
        String processId = params.get("processId");
        String[] ret = new String[1];
        try {
            ret = workflowApiManager.validateCurrentSelectedNode(caseId, currentSelectedNodeId, currentSelectedNodeName, currentStepbackNodeId, initialize_processXml, permissionAccountId, configCategory, processId);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ok(ret);
    }
    
    
    /**
     * 
     * 校验节点是否可以提交
     * 
     * @param workitemId String |必填|
     *             数字类型的字符串， 工作流的事项ID，对应ctp_affair的subObject字段
     * @return
     * 结果集为数组<br/>
     * ret[0] : 值为"true"/"false", "true" - 可以处理， "false" - 不可以处理 <br/>
     * ret[0] : 不能处理是的文字说明<br/>
     * 
     * @throws ServiceException
     * @throws BPMException
     *
     * @Since A8-V5 6.1
     *
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("canWorkflowCurrentNodeSubmit")
    public Response canWorkflowCurrentNodeSubmit( @QueryParam("workitemId") String workitemId)throws ServiceException, BPMException {
        String[] result= new String[]{"true",""};
        try {
            result = workflowApiManager.canWorkflowCurrentNodeSubmit(workitemId);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return ok(result);
    }
    

    /**
     * <p>是否能否批量删除待办事项</p>
     * <p>主要校验锁、指定回退状态、子流程等流程相关的校验</p>
     * 
     * @param params
     * <blockquote></pre>
     * params._DEL_KEYS_ List |必填|
     *            待删除节点列表
     * 
     * 示例：
     * {
     *  "_DEL_KEYS_" : [
     *                   {
     *                     "workitemId" : "123",//工作流事项ID
     *                     "processId"  : "123",//工作流ID
     *                   }
     *                 ]
     * }
     * </pre></blockquote>
     * 
     * @return
     * 结果集为列表<br/>
     * [
     *    ["true", ""],//单条待办结果，第一个参数为，"true"， 表明可以删除 
     *    ["false", "错误信息"],//单条待办结果，第一个参数为，"false"， 表明不可以删除， 第二个参数返回错误信息 
     *    ["false", "流程锁信息", "锁名称"]//如果流程被其他人锁定， 单条待办校验结果同 {@link #lockH5Workflow(String, Integer)}的结果集
     * ]
     * @throws ServiceException
     * @Since A8-V5 6.1
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("canBatchDelete")
    public Response canBatchDelete(Map<String, List<Map<String,String>>> params)throws ServiceException, BPMException {
        List<String[]> list = new ArrayList<String[]>();
        List<Map<String,String>> pl = params.get("_DEL_KEYS_");
        if(Strings.isNotEmpty(pl)){
            for(Map<String,String> param : pl){
                String workitemId = param.get("workitemId");
                String processId = param.get("processId");
                
                String[] result = new String[3];
                try {
                    String from = Constants.login_sign.stringValueOf(AppContext.getCurrentUser().getLoginSign());
                    result = workflowApiManager.lockWorkflowProcess(processId, String.valueOf(AppContext.currentUserId()), 14, from);
                } catch (BPMException e) {
                    LOGGER.error(e.getMessage(),e);
                }
                if("true".equals(result[0])){
                    result= new String[]{"true",""};
                    try {
                        result = workflowApiManager.canWorkflowCurrentNodeSubmit(workitemId);
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                }
                list.add(result);
            }
        }
        return ok(list);
    }
    
    /**
     * 触发流程事件
     * 
     * @param param
     * <pre>
     * param.event  String | 必填 |  
     *                事件名称， 实例:"BeforeStart"
     * param.formData String | 非必填 | 
     *                表单数据ID/公文数据json串
     * param.mastrid String | 非必填 | 
     *                表单主表ID
     * param.processTemplateId String | 和processId有一个必填 | 
     *                表单模板ID
     * param.processId String |  和processTemplateId有一个必填 | 
     *                流程ID
     * param.currentActivityId String | 必填 | 
     *                当前节点ID
     * param.bussinessId  String | 必填 | 
     *                业务ID， 示例： summaryId
     * param.affairId  String | 必填 | 
     *                个人事项ID
     * param.appName  String | 必填 | 
     *                模块ID, 示例： form/collaboration/edoc
     * param.formAppId String |  表单必填 | 
     *                表单ID
     * param.formViewOperation String |  非必填 | 
     *                表单权限ID
     * </pre>
     * @return
     *    {
     *       success : "true"/"false",
     *       err_msg : "错误信息"
     *    }
     * @throws ServiceException
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年2月23日下午2:25:31
     *
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("executeWorkflowBeforeEvent")
    public Response executeWorkflowBeforeEvent(Map<String, Object> param) throws ServiceException {
        
        LOGGER.info(AppContext.currentUserName()+",executeWorkflowBeforeEvent");
        
        
        Map<String, String> ret = new HashMap<String, String>();
        
        String event = ParamUtil.getString(param, "event", "");
        WorkflowBpmContext context = new WorkflowBpmContext();
        context.setFormData(ParamUtil.getString(param, "formData", ""));
        context.setMastrid(ParamUtil.getString(param, "mastrid", ""));
        context.setProcessTemplateId(ParamUtil.getString(param, "processTemplateId", ""));
        context.setProcessId(ParamUtil.getString(param, "processId", ""));
        context.setCurrentActivityId(ParamUtil.getString(param, "currentActivityId", "")); 
        context.setBussinessId(ParamUtil.getString(param, "bussinessId", ""));
        context.setAffairId(ParamUtil.getString(param, "affairId", ""));
        context.setAppName(ParamUtil.getString(param, "appName", ""));
        context.setFormAppId(ParamUtil.getString(param, "formAppId", ""));
        context.setFormViewOperation(ParamUtil.getString(param, "formViewOperation", ""));
        context.setMatchRequestToken(ParamUtil.getString(param, "matchRequestToken", ""));
        context.setProcessXml(ParamUtil.getString(param, "processXml", ""));
        context.setCurrentNodeLast(ParamUtil.getString(param, "currentNodeLast", "false"));
        
      //H5参数处理：从流程临时表中取出流程变更之后的processXml
        context.setProcessXml(WorkflowUtil.getTempProcessXml(context.getProcessXml()));
        
        String exeRet = WFAjax.executeWorkflowBeforeEvent(event, context);
        
        if(Strings.isNotBlank(exeRet)){
            ret.put("err_msg", exeRet);
            ret.put("success", "false");
        }else{
            ret.put("success", "true");
        }
        
        return ok(ret);
    }
    
    /**
     * 获取选人界面不能选取单位的数据，目前超过1500的单位不能直接选取整个单位 
     * @return
     *  <pre>
     *       [{id:xxx,name:"",type:"account"},{id:xxx1,name:"",type:"account"}],目前type固定account,name固定为"";
     *  </pre>
     * @throws ServiceException
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("accountExcludeElements")
	public Response getAcountExcludeElements() throws ServiceException {
		String[] selectType = { "", "" };
		try {
			selectType = WFAjax.getAcountExcludeElements();
		} catch (BPMException e) {
			LOGGER.error("", e);
		}
		/* {id:181818,name:"杨海",type:"member"}] */
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		String[] s = selectType[0].split(",");
		if (s != null && s.length > 0) {
			for (String info : s) {
				/* String excludeStr= "Account|"+accountId+"|wf|"+accountId; */
				if(Strings.isNotBlank(info)){
					String[] s1 = info.split("[|]");
					String accountId = s1[1];
					Map<String, String> m = new HashMap<String, String>();
					m.put("id", accountId);
					m.put("type", "account");
					m.put("name", "");
					list.add(m);
				}
			}
		}

		return ok(list);
	}
    
}
