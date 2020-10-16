package com.seeyon.ctp.rest.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.supervise.manager.SuperviseManager;
import com.seeyon.ctp.organization.bo.V3xOrgEntity;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OrgManagerDirect;
import com.seeyon.ctp.rest.util.BeanSerializerFactory;
import com.seeyon.ctp.rest.util.MapperFactory;
import com.seeyon.ctp.rest.util.OrganizationUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;



/**
 * 
 * 不要在这个里面添加rest接口了， 后续接口请统一放在EdocResource.java
 * 如果是待办相关的接口，请放到公共接口里面去
 * 
 */
@Path("affairs")
@Deprecated
public class AffairDataResource extends BaseResource{

	private static Log LOGGER = LogFactory.getLog(AffairDataResource.class);
	AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
	private  OrgManager orgManager=null;
	private OrgManagerDirect orgManagerDirect;
	public OrgManagerDirect getOrgManagerDirect(){
	    if(orgManagerDirect==null){
	    		orgManagerDirect=(OrgManagerDirect)AppContext.getBean("orgManagerDirect");
	    	}
	    	return orgManagerDirect;
	    }
	    public void setOrgManagerDirect(OrgManagerDirect orgManagerDirect){
	    	this.orgManagerDirect=orgManagerDirect;
	    }
		public OrgManager getOrgManager(){
			if(orgManager==null){
				orgManager=(OrgManager)AppContext.getBean("orgManager");
			}
			return orgManager;
		}
		
    /**
	 * 描述：通用查询方法（根据关键字模糊查询，如：查询code为“futao”会查询出futao1，futao2）
	 * @param clazz
	 * @param fieldName
	 * @param fieldValue
	 * @return
	 * @author futao
	 */
	protected List<V3xOrgEntity> getEntityNoRelationDirect(Class<? extends V3xOrgEntity> clazz,String fieldName, String fieldValue){
		List<V3xOrgEntity> listEntity = getOrgManagerDirect().getEntityNoRelationDirect(clazz.getSimpleName(), fieldName,
				fieldValue, null, null);
        if (listEntity.size() > 0) {
        	return listEntity;
        }
        return new ArrayList<V3xOrgEntity>();
	}

	/**
	 * 功能描述：通过code取单位信息
	 * @param clazz
	 * @param value
	 * @return
	 * @author futao
	 */
	protected V3xOrgEntity getAllEntityByCode(Class<? extends V3xOrgEntity> clazz,String value){
		List<V3xOrgEntity> listEntity=getEntityNoRelationDirect(clazz,"code",value);
		if (listEntity.size() > 0) {
            for (V3xOrgEntity ent : listEntity) {
                if (value.equals(ent.getCode())) {
                    return ent;
                }
            }
        }
		return null;
	}
		
	
	/**
	 * 获取待办事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @param [memberId] 人员ID
	 * @param [apps] 模块代码, 1 -  协同， 10 - 调查，   组合使用则传 1,10  还需要其他模块以此类推，中间用逗号隔开
	 * @return
	 * @throws BusinessException
	 */
	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("pending")
	@RestInterfaceAnnotation
    public FlipInfo getPendingList(@QueryParam("ticket") String ticket,
            @QueryParam("memberId") Long memberId,@QueryParam("apps") String apps) throws BusinessException {
		
		FlipInfo flipInfo = super.getFlipInfo();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 if(Strings.isNotBlank(apps)){
				 List<Integer> l = new ArrayList<Integer>();
				 String[] a  = apps.split(",");
				 if(a != null){
					 for(String s : a){
						 l.add(Integer.valueOf(s));
					 }
					 params.put("app", l);
				 }
			 }
			 try {
					affairManager.getByConditions(flipInfo, params);
				} catch (BusinessException e) {
					LOGGER.error("获得待办affair数据报错!",e);
				}	
		}
		return flipInfo;
	}
	
	public List<CtpAffair> addFields(List<CtpAffair> xx){
//		MapperFactory.getInstance().register(CtpAffair.class,new BeanSerializerFactory.Builder(){
//		    public Map addFields(Object bean) {
//		        Map<String,String> data = new HashMap<String,String>();
//		        CtpAffair ctpAffair = (CtpAffair) bean;
//		        long memberId = ctpAffair.getMemberId();
//		        long SenderId = ctpAffair.getSenderId();
//		        try {
//					data.put("memberName",getOrgManager().getMemberById(memberId).getName());
//					data.put("SenderName",getOrgManager().getMemberById(SenderId).getName());
//				} catch (BusinessException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		        return data;
//		    }
//		});
		return null;
	}
	
	/**
	 * 按着人员编码获取待办事项
	 * @param tikcket
	 * @param memberCode
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("pending/code/{memberCode}")
	@RestInterfaceAnnotation
	public FlipInfo getPendingList(@QueryParam("ticket")String ticket,
			@PathParam("memberCode") String memberCode){
		FlipInfo flipInfo = super.getFlipInfo();
		V3xOrgEntity v3xOrgEntity=getAllEntityByCode(V3xOrgMember.class, memberCode);
		V3xOrgMember member=(V3xOrgMember)v3xOrgEntity;
		Long memberId=member.getId();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得待办affair数据报错!",e);
			}
		}
		return flipInfo;
	}

	/**
	 * 获取某个模块的待办事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @param app   模块app         
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("pendingbyapp")
	public FlipInfo getPendingListByApp(@QueryParam("ticket") String ticket,
			@QueryParam("memberId") Long memberId,@QueryParam("app") Integer app) throws BusinessException {
	    
	    return getPendingByApp(ticket, memberId, app, false);
	}
	
	
	/**
     * 获取某个模块的待办事项数量
     * 
     * @param ticket
     *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
     * @param app   模块app         
     * @return
     * @throws BusinessException
     */
	@GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("pendingCountbyapp")
    public FlipInfo getPendingListCountByApp(@QueryParam("ticket") String ticket,
            @QueryParam("memberId") Long memberId,@QueryParam("app") Integer app) throws BusinessException {
        
	    return getPendingByApp(ticket, memberId, app, true);
    }
	
	/**
	 * 获取待办数据
	 * 
	 * @param ticket
	 * @param memberId
	 * @param app
	 * @param onlyCount 是否只获取数量
	 * @return
	 *
	 * @Author      : xuqw
	 * @Date        : 2016年6月24日上午11:05:01
	 *
	 */
	private FlipInfo getPendingByApp(String ticket, Long memberId, Integer app, boolean onlyCount){
	    
	    FlipInfo flipInfo = super.getFlipInfo();
        if((flipInfo != null || onlyCount) && memberId != null){
             Map<String,Object> params = new HashMap<String,Object>();
             
             params.put("memberId", memberId);
             params.put("state", Integer.valueOf(StateEnum.col_pending.getKey()));
             params.put("delete", Boolean.valueOf(false));
             params.put("app", app);
             
             //会议待办，只显示周期性会议中最近一条的
             if(Integer.valueOf(ApplicationCategoryEnum.meeting.key()).equals(app)){
                 //params.put("subStates", SubStateEnum.col_pending_unRead.key());
                 params.put("subStates", SubStateEnum.meeting_pending_join.key());
             }
             
             try {
                 if(onlyCount){
                     int count = affairManager.getCountByConditions(params);
                     if(flipInfo == null){
                         flipInfo = new FlipInfo();
                     }
                     flipInfo.setTotal(count);
                 }else {
                     affairManager.getByConditions(flipInfo, params);
                 }
            } catch (BusinessException e) {
                LOGGER.error("获得待办affair数据报错!",e);
            }
        }
        return flipInfo;
	}
	
	/**
	 * 获取某个模块的已办事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("donebyapp")
	public FlipInfo getDoneListByApp(@QueryParam("ticket") String ticket,
									  @QueryParam("memberId") Long memberId,@QueryParam("app") Integer app) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_done.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 params.put("app", app);
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得已办affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	
	
	/**
	 * 获取已办事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("done")
	@RestInterfaceAnnotation
	public FlipInfo getDoneList(@QueryParam("ticket") String ticket,
									  @QueryParam("memberId") Long memberId) throws BusinessException {
		
		FlipInfo flipInfo = super.getFlipInfo();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_done.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得已办affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	/**
	 * 按着人员编码获取已办事项
	 * @param ticket
	 * @param memberCode
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("done/code/{memberCode}")
	@RestInterfaceAnnotation
	public FlipInfo getDoneListByMemberCode(@QueryParam("ticket") String ticket,
									  @PathParam("memberCode") String memberCode) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		V3xOrgEntity v3xOrgEntity=getAllEntityByCode(V3xOrgMember.class, memberCode);
		V3xOrgMember member=(V3xOrgMember)v3xOrgEntity;
		Long  memberId=member.getId();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_done.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得已办affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	
	/**
	 * 获取已发事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("sent")
	@RestInterfaceAnnotation
	public FlipInfo getSentList(@QueryParam("ticket") String ticket,
										@QueryParam("memberId") Long memberId) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_sent.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得已发affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	/**
	 * 按人员编码获取已发事项
	 * @param ticket
	 * @param meberCode
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("sent/code/{meberCode}")
	@RestInterfaceAnnotation
	public FlipInfo getSentListByMemberCode(@QueryParam("ticket") String ticket,
										@PathParam("meberCode") String meberCode) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		V3xOrgEntity v3xOrgEntity=getAllEntityByCode(V3xOrgMember.class, meberCode);
		V3xOrgMember member=(V3xOrgMember)v3xOrgEntity;
		Long memberId=member.getId();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_sent.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得已发affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	
	/**
	 * 获取待发事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("draft")
	@RestInterfaceAnnotation
	public FlipInfo getDraftList(@QueryParam("ticket") String ticket,
										@QueryParam("memberId") Long memberId) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 params.put("state", Integer.valueOf(StateEnum.col_waitSend.getKey()));
			 params.put("delete", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得待发affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	
	
	/**
	 * 获取跟踪事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("track")
	@RestInterfaceAnnotation
	public FlipInfo getTrackList(@QueryParam("ticket") String ticket,
										@QueryParam("memberId") Long memberId) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 List<Integer> states = new ArrayList<Integer>();
			 states.add(StateEnum.col_sent.getKey());
			 states.add(StateEnum.col_pending.getKey());
			 states.add(StateEnum.col_done.getKey());
			 params.put("state", states);
			 params.put("delete", Boolean.valueOf(false));
			 params.put("track", 1);
			 params.put("finish", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得跟踪affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	/**
	 * 按着人员编码获取跟踪事项
	 * @param ticket
	 * @param memberCode
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("track/code/{memberCode}")
	@RestInterfaceAnnotation
	public FlipInfo getTrackListByMemberCode(@QueryParam("ticket") String ticket,
										@PathParam("memberCode") String memberCode) throws BusinessException {
		FlipInfo flipInfo = super.getFlipInfo();
		V3xOrgEntity v3xOrgEntity=getAllEntityByCode(V3xOrgMember.class, memberCode);
		V3xOrgMember member=(V3xOrgMember)v3xOrgEntity;
		Long memberId=member.getId();
		if(flipInfo != null && memberId != null){
			 Map<String,Object> params = new HashMap<String,Object>();
			 
			 params.put("memberId", memberId);
			 List<Integer> states = new ArrayList<Integer>();
			 states.add(StateEnum.col_sent.getKey());
			 states.add(StateEnum.col_pending.getKey());
			 states.add(StateEnum.col_done.getKey());
			 params.put("state", states);
			 params.put("delete", Boolean.valueOf(false));
			 params.put("track", 1);
			 params.put("finish", Boolean.valueOf(false));
			 try {
				affairManager.getByConditions(flipInfo, params);
			} catch (BusinessException e) {
				LOGGER.error("获得跟踪affair数据报错!",e);
			}
		}
		return flipInfo;
	}
	/**
	 * 获取督办事项
	 * 
	 * @param ticket
	 *            sso单点登录获得的身份令牌，由外部系统提供，外部系统每次请求服务需要带上此参数。
	 * @return
	 * @throws BusinessException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("supervise")
	@RestInterfaceAnnotation
	public FlipInfo getSuperviseList(@QueryParam("ticket") String ticket,
										@QueryParam("memberId") Long memberId) throws BusinessException {
		
		FlipInfo flipInfo = super.getFlipInfo();
		if(flipInfo != null && memberId != null){
			Map<String,List<Object>> conditon = new HashMap<String,List<Object>>();
			 
			List<Object> list = new ArrayList<Object>();
	        list.add(String.valueOf(memberId));
	        conditon.put("userId", list);
	        
	        SuperviseManager superviseManager = (SuperviseManager)AppContext.getBean("superviseManager");
			try {
				 superviseManager.getSuperviseList4Portal(flipInfo, conditon);
			} catch (BusinessException e) {
				LOGGER.error("获得督办数据报错!",e);
			}
		}
		return flipInfo;
	}
}
