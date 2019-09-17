package com.seeyon.v3x.edoc.dao;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.web.util.WebUtil;
import com.seeyon.ctp.organization.po.OrgMember;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.dao.paginate.Pagination;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EdocListDao extends BaseHibernateDao<EdocSummary> {

	public EdocSummaryDao edocSummaryDao;

	private static final String selectSummary = "summary.id," +
			"summary.startUserId," +
			"summary.caseId," +
			"summary.completeTime,"+
			"summary.subject," +
			"summary.secretLevel," +
			"summary.identifier," +
			"summary.docMark," +
			"summary.serialNo," +
			"summary.createTime,"+
			"summary.sendTo," +
			"summary.issuer," +
			"summary.signingDate," +
			"summary.deadline," +
			"summary.deadlineDatetime," +
			"summary.startTime," +
			"summary.copies," +
			"summary.createPerson," +
			"summary.sendUnit," +
			"summary.sendDepartment,"+
			"summary.hasArchive," +
			"summary.processId," +
			"summary.caseId,"+
			"summary.urgentLevel, " +
			"summary.templeteId, "+
			"summary.state, " +
			"summary.copyTo, " +
			"summary.reportTo,"+
			"summary.archiveId,"+
			"summary.edocType, "+
			"summary.docMark2,"+
			"summary.sendTo2, "+
			"summary.docType,"+
			"summary.sendType,"+
			"summary.isQuickSend,"+
			"summary.currentNodesInfo";



	private static final String selectAffair = selectSummary +
			",affair.id," +
			"affair.state," +
			"affair.subState," +
			"affair.track," +
			"affair.hastenTimes," +
			"affair.coverTime," +
			"affair.remindDate," +
			"affair.deadlineDate," +
			"affair.receiveTime," +
			"affair.completeTime," +
			"affair.createDate," +
			"affair.memberId," +
			"affair.bodyType," +
			"affair.transactorId,"+
			"affair.updateDate,"+
			"affair.extProps,"+
			"affair.nodePolicy,"+
			"affair.subObjectId,"+
			"affair.expectedProcessTime,"+
			"affair.activityId," +
			"affair.fromId," + 
			"affair.backFromId";
	private static final String selectSummarySql = "summary.id summaryid," +
			"summary.start_user_id," +
			"summary.case_id," +
			"summary.complete_time summary_complete_time,"+
			"summary.subject," +
			"summary.secret_level," +
			"summary.identifier," +
			"summary.doc_mark," +
			"summary.serial_no," +
			"summary.create_time,"+
			"summary.send_to," +
			"summary.issuer," +
			"summary.signing_date," +
			"summary.deadline," +
			"summary.deadline_datetime," +
			"summary.start_time," +
			"summary.copies," +
			"summary.create_person," +
			"summary.send_unit," +
			"summary.send_department,"+
			"summary.has_archive," + 
			"summary.process_id," +
			"summary.case_id case_id2,"+
			"summary.urgent_level, " +
			"summary.templete_id, "+
			"summary.state summary_state, " +
			"summary.copy_to, " +
			"summary.report_to,"+
			"summary.archive_id,"+
			"summary.edoc_type, "+
			"summary.doc_mark2,"+
			"summary.send_to2, "+
			"summary.doc_type,"+
			"summary.send_type,"+
			"summary.is_quick_send,"+
			"summary.current_nodes_info";


	
	private static final String selectAffairSql = selectSummarySql +
			",affair.id," +
			"affair.state," +
			"affair.sub_state," +
			"affair.track," +
			"affair.hasten_times," +
			"affair.is_cover_time," +
			"affair.remind_date," +
			"affair.deadline_date," +
			"affair.receive_time," +
			"affair.complete_time," +
			"affair.create_date," +
			"affair.member_id," +
			"affair.body_type," +
			"affair.transactor_id,"+
			"affair.update_date,"+
			"affair.ext_props,"+
			"affair.node_policy,"+
			"affair.sub_object_id,"+
			"affair.expected_process_time,"+
			"affair.activity_id," +
			"affair.from_Id," + 
			"affair.back_from_id";
	/**
	 * G6分发待发列表查询
	 * @param type
	 * @param condition
	 * @return
	 */
	public List<Object[]> findWaitEdocList(int type, Map<String, Object> condition) {
		Long userId = (Long)condition.get("userId");
		int edocType = condition.get("edocType")==null?-1:(Integer)condition.get("edocType");
		String textfield = condition.get("textfield") == null ? null : (String)condition.get("textfield");
        String textfield1 = condition.get("textfield1") == null ? null : (String)condition.get("textfield1");
        String conditionKey = condition.get("conditionKey") == null ? null : (String)condition.get("conditionKey");

		StringBuilder buffer = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        buffer.append(" select s.id,s.subject,s.secretLevel,s.docMark,s.serialNo,s.caseId,s.processId,s.deadlineDatetime,r.registerUserName,r.registerDate, ");
        buffer.append(" r.recTime,r.recieveUserName,a.id,a.subState,r.registerType,r.id,r.autoRegister,r.recieveId,r.exchangeMode,s.isQuickSend ");
		buffer.append(" from EdocSummary s,EdocRegister r,CtpAffair a where s.id = r.distributeEdocId and a.objectId = s.id ");
		buffer.append(" and s.startUserId = :startUserId and s.edocType=:edocType and a.state = :state and a.delete=:delete and r.distributeState <>:notDistributeState");

		params.put("startUserId", userId);
		params.put("edocType", edocType);
		params.put("state", StateEnum.col_waitSend.key());
		params.put("delete", false);
		params.put("notDistributeState", 1);//登记表中distribute_state为1时表示未分发，当电子收文回退到待分发时就为1，分发待发列表不显示 被回退的电子收文

		//标题
        if("subject".equals(conditionKey)) {
        	if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and s.subject like :subject");
	        	params.put("subject", "%"+textfield+"%");
        	}
        }
        //公文文号
		else if("docMark".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and s.docMark like :docMark");
	        	params.put("docMark", "%"+textfield+"%");
			}
        }
        //文件秘级
		else if("secretLevel".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and s.secretLevel = :secretLevel");
	        	params.put("secretLevel", textfield);
			}
        }
		// 状态
		if ("subState".equals(conditionKey)) {
			if (Strings.isNotBlank(textfield)) {
				if(textfield.equals(SubStateEnum.col_waitSend_stepBack.getKey()+"")){//回退，要查询出，指定回退的
					List<Integer> substateList=new ArrayList<Integer>();
					substateList.add(SubStateEnum.col_waitSend_stepBack.getKey());
					substateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
					substateList.add(SubStateEnum.col_pending_specialBacked.getKey());
					buffer.append(" and a.subState in (:a_substate)");
					params.put("a_substate", substateList);
				}else{
					buffer.append(" and a.subState = :subState");
					params.put("subState", Integer.valueOf(textfield));
				}
			}
		}
        //发文单位
        if ("sendUnit".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
				buffer.append(" and s.sendUnit like :r_edocUnit");
	        	params.put("r_edocUnit", "%"+textfield+"%");
			}
        }
        //签收时间
        else if ("recieveDate".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
				if (Strings.isNotBlank(textfield)) {
					java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
					buffer.append(" and r.recTime >= :timestamp1");
					params.put("timestamp1", stamp);
				}
				if (Strings.isNotBlank(textfield1)) {
					java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
					//OA-24096 收文-待办列表，小查询，根据签收时间查询，什么都不选，可以查出所有的数据。但是，不论是开始还是结束时间，随便选个时间，就查不出一条数据。结果为空。
					buffer.append(" and r.recTime <= :timestamp2");
					params.put("timestamp2", stamp);
				}
			}
        }
		//登记时间
        else if ("registerDate".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
				if (Strings.isNotBlank(textfield)) {
					java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
					buffer.append(" and r.registerDate >= :timestamp1");
					params.put("timestamp1", stamp);
				}
				if (Strings.isNotBlank(textfield1)) {
					java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
					buffer.append(" and r.registerDate <= :timestamp2");
					params.put("timestamp2", stamp);
				}
			}
        }
        //交换方式
        else if("exchangeMode".equals(conditionKey)){
        	if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and r.exchangeMode = :exchangeMode");
	        	params.put("exchangeMode", Integer.valueOf(textfield));
			}
        }
        
        if(EdocHelper.isG6Version()) { //G6版本包含登记、分发
			if("2".equals(EdocSwitchHelper.isOpenRegister())) {//打开登记
				buffer.append(" order by r.registerDate desc, s.createTime desc");
			} else {
				buffer.append(" order by r.recTime desc, s.createTime desc");
			}
        } else {
        	buffer.append(" order by r.recTime desc, s.createTime desc");
        }
		List<Object[]> result = edocSummaryDao.find(buffer.toString(), params);
		return result;
	}


	public List<Object[]> findEdocList(int type, Map<String, Object> condition) {
		 if ("true".equals(condition.get("deduplication"))) {
			 return findEdocListDeduplication(type, condition);
		 }else{
			 return findEdocList0(type, condition);
		 }
	}
	
	
	/**
	 * 同一流程只显示一条
	 * @param type
	 * @param condition
	 * @return
	 */
	private List<Object[]> findEdocListDeduplication(int type, Map<String, Object> condition) {
		List<Integer> stateList = (List<Integer>)condition.get("stateList");
		List<Integer> substateList = (List<Integer>)condition.get("substateList");
		List<Integer> appList = (List<Integer>)condition.get("appList");
		String listType = condition.get("listType")==null ? "listSendAll" : (String)condition.get("listType");
		int edocType = condition.get("edocType")==null?-1:(Integer)condition.get("edocType");
		long processType = condition.get("processType")==null?-1:(Integer)condition.get("processType");
		long subEdocType = condition.get("subEdocType")==null?-1:(Long)condition.get("subEdocType");
		int track = condition.get("track")==null?-1:(Integer)condition.get("track");
       Long userId = (Long)condition.get("userId");
       
       String textfield = condition.get("textfield") == null ? null : (String)condition.get("textfield");
       String textfield1 = condition.get("textfield1") == null ? null : (String)condition.get("textfield1");
       String conditionKey = condition.get("conditionKey") == null ? null : (String)condition.get("conditionKey");
       
		List<AgentModel> edocAgent = condition.get("edocAgent") == null ? new ArrayList<AgentModel>() : (List<AgentModel>)condition.get("edocAgent");
       boolean agentToFlag = condition.get("agentToFlag") == null ? false : (Boolean)condition.get("agentToFlag");
       
       StringBuilder buffer = new StringBuilder();
       Map<String, Object> params = new HashMap<String, Object>();
       //在办表加入暂存代办提醒时间列
 		String zcdbTimeSelect = ""; 
 		String zcdbTable = "";
 		if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
 			zcdbTable = ", edoc_zcdb  edocZcdb";
 			zcdbTimeSelect = ", edocZcdb.zcdb_time ";
 		}
 		//发文/签报SQL
 		buffer.append("select "+selectAffairSql + zcdbTimeSelect+" from ctp_affair affair,edoc_summary summary" +zcdbTable);
 		
 		//发起人
 		if ("startMemberName".equals(conditionKey) && Strings.isNotBlank(textfield)) {
 			buffer.append(" ,org_member mem ");
       }
 		
 		//是否连查EdocRegister
		boolean isNoRegister = false;
 		//收文管理  -- 成文单位
 		if ("edocUnit".equals(conditionKey) && Strings.isNotBlank(textfield)) {
			isNoRegister = true;
       }
 		//收文管理  -- 签收时间/登记时间
 		if ("recieveDate".equals(conditionKey) || "registerDate".equals(conditionKey)) {
 			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
 				isNoRegister = true;
 			}
       }
 		if (isNoRegister) {
 			buffer.append(" ,edoc_register register ");
 		}
 		buffer.append(" ,(select max(affair2.id)  id from ctp_affair affair2 where affair2.is_delete=0 ");
 		if(CollectionUtils.isNotEmpty(stateList)) {
 			buffer.append(" and affair2.state in (:a_state) ");
			params.put("a_state", stateList);
		}
		if(CollectionUtils.isNotEmpty(appList)) {
			buffer.append(" and affair2.app in (:a_app) ");
			params.put("a_app", appList);
		}
		
		StringBuilder sbAgentAffair1 = getMemberOrAgentSql("affair2",type, userId, edocAgent, agentToFlag, params);
		buffer.append( sbAgentAffair1 );
		buffer.append(" AND affair2.complete_time=");
		StringBuilder completeTimesb = new StringBuilder();
		completeTimesb.append("(");
		completeTimesb.append(" SELECT max(ctpAffair3.complete_time) FROM ctp_affair ctpAffair3 WHERE ");
		completeTimesb.append(" ctpAffair3.object_id = affair2.object_id ");
		completeTimesb.append(" AND ctpAffair3.is_delete=0 ");
		if(CollectionUtils.isNotEmpty(stateList)) {
			completeTimesb.append(" and ctpAffair3.state in (:a_state) ");
			params.put("a_state", stateList);
		}
		if(CollectionUtils.isNotEmpty(appList)) {
			completeTimesb.append(" and ctpAffair3.app in (:a_app) ");
			params.put("a_app", appList);
		}
		
		StringBuilder sbAgentAffair2 = getMemberOrAgentSql("ctpAffair3",type, userId, edocAgent, agentToFlag, params);
		completeTimesb.append( sbAgentAffair2 );
		
		completeTimesb.append(" GROUP BY ctpAffair3.object_id  ");
		completeTimesb.append(" ) GROUP BY affair2.object_id");
		buffer.append(completeTimesb);
		buffer.append(" ) a");
		
		
		
		
 		buffer.append(" where affair.object_id=summary.id ");
 		buffer.append(" and affair.id = a.id ");
 		if (isNoRegister) {
 			buffer.append(" and summary.id = register.distribute_edoc_id");
 		}
		if(Strings.isNotBlank(zcdbTable)){
			buffer.append(" and edocZcdb.affairId =affair.id ");
		}
		//发起人
 		if ("startMemberName".equals(conditionKey) && Strings.isNotBlank(textfield)) {
	        	buffer.append(" and affair.sender_id=mem.id  ");
 		}
		
		
		StringBuilder sbAgentAffair = getMemberOrAgentSql("affair",type, userId, edocAgent, agentToFlag, params);
		
		buffer.append(sbAgentAffair);
		
      
 		
		if(CollectionUtils.isNotEmpty(stateList)) {
			buffer.append(" and affair.state in (:a_state)");
			params.put("a_state", stateList);
		}
		if(CollectionUtils.isNotEmpty(appList)) {
			buffer.append(" and affair.app in (:a_app)");
			params.put("a_app", appList);
		}
		
		buffer.append(" and affair.is_delete=:affair_delete");
		params.put("affair_delete",Boolean.FALSE);
		
		buffer.append(" and affair.object_id=summary.id");
		
		buffer.append(" and summary.state<>:delete_state");
		params.put("delete_state", EdocConstant.flowState.deleted.ordinal());

		if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
			buffer.append(" and edocZcdb.affair_id =affair.id");
		}
		if(CollectionUtils.isNotEmpty(substateList)) {
			buffer.append(" and affair.sub_state in (:a_substate)");
			params.put("a_substate", substateList);
		}
		//当开启区分阅文、办文开关，区分“阅文”和“办文”
		Long accountId=(Long)condition.get("accountId");
		boolean isYueBanFlag=false;
		if(EdocSwitchHelper.showBanwenYuewen(accountId)){
			isYueBanFlag=true;
		}
		if(isYueBanFlag && processType!=-1 && processType!=0) {
			buffer.append(" and summary.process_type=:s_processType");
			params.put("s_processType", processType);
		}
		if(subEdocType != -1) {
			buffer.append(" and summary.sub_edoc_type=:s_subEdocType");
			params.put("s_subEdocType", subEdocType);
       }

		//过滤条件：跟踪
		if(track != -1) {
			buffer.append(" and affair.track=:track ");
			params.put("track", track);
		}
		//跟踪
       if ("isTrack".equals(conditionKey)) {
       	buffer.append(" and affair.track = 1");
       } 
       //关于已经完成的filter
       if ("finishfilter".equals(conditionKey)) {
       	buffer.append(" and summary.finish_date is not null");
       }
       //关于未完成的filter
       if ("notfinishfilter".equals(conditionKey)) {
       	buffer.append(" and summary.finish_date is null");
       }
		//标题
       else if("subject".equals(conditionKey)) {
       	if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.subject like :subject");
	        	params.put("subject", "%"+textfield+"%");
       	}
       } 
		//关键字
		else if("keywords".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.keywords like :keywords");
	        	params.put("keywords", "%"+textfield+"%");
			}
       } 
		//公文文号
		else if("docMark".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.doc_mark like :docMark");
	        	params.put("docMark", "%"+textfield+"%");
			}
       } 
		//内部文号
		else if("docInMark".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.serial_no like :serialNo");
	        	params.put("serialNo", "%"+textfield+"%");
			}
       } 
   		//来文单位
		else if("sendUnit".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.send_unit like :sendUnit");
	        	params.put("sendUnit", "%"+textfield+"%");
			}
        }
		//退回人
		else if("backBoxPeople".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.ext_props like :backBoxPeople");
	        	params.put("backBoxPeople", "%"+textfield+"%");
			}
       } 
		//文件秘级 
		else if("secretLevel".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.secret_level = :secretLevel");
	        	params.put("secretLevel", textfield);
			}
       } 
		//紧急程度
		else if("urgentLevel".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.urgent_level = :urgentLevel");
	        	params.put("urgentLevel", textfield);
			}
       } 
		//行文类型
		else if("sendType".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.send_type = :sendType");
	        	params.put("sendType", textfield);
			}
       } 
		//流程节点
		else if("nodePolicy".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.node_policy = :nodePolicy");
	        	params.put("nodePolicy", textfield);
			}
       } 
		//
		else if ("cusSendType".equals(conditionKey)) {
	    	if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.sub_edoc_type=:subEdocType");
	        	params.put("subEdocType", Long.parseLong(textfield));        		
	        }
	    } 
		//成文单位 
       if ("edocUnit".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
				//buffer.append(" and register.edocUnit like :r_edocUnit");
				buffer.append(" and summary.send_unit like :r_edocUnit");
	        	params.put("r_edocUnit", "%"+textfield+"%");  
			}			
       }
		//发起时间
		else if("createDate".equals(conditionKey)) {
       	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and summary.create_time >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and summary.create_time <= :timestamp2");
				params.put("timestamp2", stamp);
			}
       } 
		//退回时间
		else if("backDate".equals(conditionKey)) {
	    	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and affair.update_date >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and affair.update_date <= :timestamp2");
				params.put("timestamp2", stamp);
			}
       } 
		//退回方式
		else if ("backBox".equals(conditionKey) || "retreat".equals(conditionKey)) {
       	if (Strings.isNotBlank(textfield)) {
				buffer.append(" and affair.sub_state >= :a_subState2");
				params.put("a_subState2", Integer.parseInt(textfield));
			}
       }
		//签收时间
       else if ("recieveDate".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
				if (Strings.isNotBlank(textfield)) {
					java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
					buffer.append(" and register.rec_time >= :timestamp1");
					params.put("timestamp1", stamp);
				}
				if (Strings.isNotBlank(textfield1)) {
					java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
					//OA-24096 收文-待办列表，小查询，根据签收时间查询，什么都不选，可以查出所有的数据。但是，不论是开始还是结束时间，随便选个时间，就查不出一条数据。结果为空。
					buffer.append(" and register.rec_time <= :timestamp2");
					params.put("timestamp2", stamp);
				}
			}
       }
		//登记时间
       else if ("registerDate".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
				buffer.append(" and summary.id = register.distribute_edoc_id");
				if (Strings.isNotBlank(textfield)) {
					java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
					buffer.append(" and register.register_date >= :timestamp1");
					params.put("timestamp1", stamp);
				}
				if (Strings.isNotBlank(textfield1)) {
					java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
					buffer.append(" and register.register_date <= :timestamp2");
					params.put("timestamp2", stamp);
				}
			}
       }
       //到达时间
       else if ("receiveTime".equals(conditionKey)) {
			if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and affair.receive_time >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and affair.receive_time <= :timestamp2");
				params.put("timestamp2", stamp);
			}
       }//处理期限
       else if("expectprocesstime".equals(conditionKey)){
       	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and affair.expected_process_time >= :timestamp1 and affair.expected_process_time is not null");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and affair.expected_process_time <= :timestamp2 and affair.expected_process_time is not null");
				params.put("timestamp2", stamp);
			}
       }else if("deadlineDatetime".equals(conditionKey)){
       	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and summary.deadline_datetime >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and summary.deadline_datetime <= :timestamp2");
				params.put("timestamp2", stamp);
			}
       }else if("workflowState".equals(conditionKey)){
           buffer.append(" and summary.state=:summaryState ");
           if ("0".equals(textfield)) {
           	params.put("summaryState",EdocConstant.flowState.run.ordinal());
           } else if ("1".equals(textfield)) {
           	params.put("summaryState",EdocConstant.flowState.finish.ordinal());
           } else if ("2".equals(textfield)) {
           	params.put("summaryState",EdocConstant.flowState.terminate.ordinal());
           }
       }else if("exchangeMode".equals(conditionKey)){//交换方式
       	if (Strings.isNotBlank(textfield)) {
				buffer.append(" and register.exchange_mode = :exchangeMode");
				params.put("exchangeMode", Integer.parseInt(textfield));
			}
       }
       
       //发起人
       if ("startMemberName".equals(conditionKey) && Strings.isNotBlank(textfield)) {
        	buffer.append(" and mem.name like :startMemberName");
        	params.put("startMemberName", "%"+textfield+"%");
       }
       
       //公文类型
       if(edocType != -1) {
       	buffer.append(" and summary.edoc_type=:edocType");
       	params.put("edocType", edocType);
       }
       if("secretLevel".equals(conditionKey) && Strings.isNotBlank(textfield)) {
	        buffer.append(" and summary.secret_level = :secretLevel");
           params.put("secretLevel", textfield);
       }
       //排序
       if(type == EdocNavigationEnum.LIST_TYPE_PENDING) {
       	buffer.append(" order by affair.receive_time desc");
		} else if(type == EdocNavigationEnum.LIST_TYPE_DONE) {
			buffer.append(" order by affair.complete_time desc");
		} else {
			buffer.append(" order by affair.create_date desc");
		}
       List<Object[]> result = new ArrayList<Object[]>();
       JDBCAgent jdbc = new JDBCAgent(true);
       FlipInfo flipInfo =new FlipInfo();
       flipInfo.setNeedTotal(true);
       HttpServletRequest request = WebUtil.getRequest();
		int page = 0;
		try {
			page = Integer.parseInt(request.getParameter("page"));
		}
		catch (Exception e) {
		}
       flipInfo.setPage(page);
       flipInfo.setSize(Pagination.getMaxResults());
       try {
			jdbc.findNameByPaging(buffer.toString(),params,flipInfo);
			result = flipInfo.getData();
		} catch(Exception e){
			logger.error("合并同一流程查询错误！",e);
		}finally {
			jdbc.close();
		}
       Pagination.setRowCount(flipInfo.getTotal());
       
       List<Map> l = flipInfo.getData();
       List<Object[]> querySet = new ArrayList<Object[]>();
       
   	List<String> booleanList = new ArrayList<String>();
   	booleanList.add("has_archive");
   	booleanList.add("is_quick_send");
   	booleanList.add("is_cover_time");
   	List<String> decimalList = new ArrayList<String>();
   	decimalList.add("summary_state");
   	List<String> timestampList = new ArrayList<String>();
   	timestampList.add("signing_date");
   	timestampList.add("deadline_datetime");
       for (Map<String,Object> map : l) {
       	Object[] obj = new Object[map.size()];
       	Iterator ite = map.keySet().iterator();
       	int j = 0;
       	while (ite.hasNext()) {
           	String key = (String) ite.next();
           	Object a =map.get(key);
           	if(a != null && booleanList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
           		a = Integer.valueOf(String.valueOf(a)) == 0 ? false : true;
           	}
           	if(a != null && decimalList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
           		a = Integer.valueOf(String.valueOf(a));
           	}
           	if(a != null && timestampList.contains(key) && Strings.isNotBlank(String.valueOf(a))) {
           		a = (Timestamp)a;
           	}
           	obj[j] = a;
           	j++;
           }
       	querySet.add(obj);
       }
		return querySet;
	}
	
	private StringBuilder getMemberOrAgentSql(String alias,int type, Long userId, List<AgentModel> edocAgent, boolean agentToFlag, Map<String, Object> params) {
		StringBuilder sbAgent = new StringBuilder();
		//代理人
		if(edocAgent != null && !edocAgent.isEmpty()){
			if (!agentToFlag) {////////////////////////////代理人
				sbAgent.append(" and (").append(alias).append(".member_id=:userId");
				//待办、在办，已办，已办结列表需要体现公文代理数据
				if (type==EdocNavigationEnum.LIST_TYPE_PENDING || type==EdocNavigationEnum.LIST_TYPE_DONE
					|| type == EdocNavigationEnum.LIST_TYPE_SENT ) {
					sbAgent.append(" or (");
					for(int i=0; i<edocAgent.size(); i++) {
						if(i != 0) {sbAgent.append(" or ");}
						AgentModel agent = edocAgent.get(i);
						sbAgent.append(" (").append(alias).append(".member_id=:edocAgentToId"+i+" and ").append(alias).append(".receive_time>=:proxyCreateDate"+i+")");
						params.put("edocAgentToId"+i, agent.getAgentToId());
						params.put("proxyCreateDate"+i, agent.getStartDate());
					}
					sbAgent.append(")");
				}
				sbAgent.append(")");
				params.put("userId", userId);
			} else {////////////////////////////被代理人
				//被代理人待办，在待，已办，已办结列表不体现公文代理数据
				sbAgent.append(" and ").append(alias).append(".member_id=:userId");
				params.put("userId", userId);
			}		
       } else {//非代理人正常显示公文数据
       	sbAgent.append(" and ").append(alias).append(".member_id=:userId");
			params.put("userId", userId);
       }
		return sbAgent;
	}
	
	private List<Object[]> findEdocList0(int type, Map<String, Object> condition) {
		List<Integer> stateList = (List<Integer>)condition.get("stateList");
		List<Integer> substateList = (List<Integer>)condition.get("substateList");
		List<Integer> appList = (List<Integer>)condition.get("appList");
		String listType = condition.get("listType")==null ? "listSendAll" : (String)condition.get("listType");
		int edocType = condition.get("edocType")==null?-1:(Integer)condition.get("edocType");
		long processType = condition.get("processType")==null?-1:(Integer)condition.get("processType");
		long subEdocType = condition.get("subEdocType")==null?-1:(Long)condition.get("subEdocType");
		int track = condition.get("track")==null?-1:(Integer)condition.get("track");
        Long userId = (Long)condition.get("userId");

        String textfield = condition.get("textfield") == null ? null : (String)condition.get("textfield");
        String textfield1 = condition.get("textfield1") == null ? null : (String)condition.get("textfield1");
        String conditionKey = condition.get("conditionKey") == null ? null : (String)condition.get("conditionKey");

		List<AgentModel> edocAgent = condition.get("edocAgent") == null ? new ArrayList<AgentModel>() : (List<AgentModel>)condition.get("edocAgent");
        boolean agentToFlag = condition.get("agentToFlag") == null ? false : (Boolean)condition.get("agentToFlag");

        List<Integer> subAppList = (List<Integer>)condition.get("subAppList");
        
        StringBuilder buffer = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        //在办表加入暂存代办提醒时间列
  		String zcdbTimeSelect = "";
  		String zcdbTable = "";
  		/*新公文不维护EdocZcdb对象，直接通过sub_state进行获取
  		 * if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
  			zcdbTable = ", EdocZcdb as edocZcdb";
  			zcdbTimeSelect = ", edocZcdb.zcdbTime ";
  		}*/
  		
  		boolean queryRegisterFlag = false;//用于A8 待发列表查询登记数据标识
  		String a8RegisterFields = "";
  		
  		String exchangeMode="";
  		
  		if("exchangeMode".equals(conditionKey)||"true".equals(condition.get("a8WaitSendList"))){
    		exchangeMode=",register.exchangeMode";
    		queryRegisterFlag = true;
    	}
  		
  	    if("true".equals(condition.get("a8WaitSendList"))){
  	        a8RegisterFields = ",register.registerType,register.id,register.autoRegister,register.recieveId";
  	        queryRegisterFlag = true;
  	    }
  	    
  		//发文/签报SQL
  		buffer.append("select "+selectAffair + exchangeMode + a8RegisterFields + zcdbTimeSelect + " from CtpAffair as affair,EdocSummary as summary" +zcdbTable);

  		//是否连查EdocRegister
		boolean isNoRegister = false;
  		//收文管理  -- 成文单位
  		if ("edocUnit".equals(conditionKey) && Strings.isNotBlank(textfield)) {
			isNoRegister = true;
        }
  		//收文管理  -- 签收时间/登记时间
  		if ("recieveDate".equals(conditionKey) || "registerDate".equals(conditionKey)) {
  			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
  				isNoRegister = true;
  			}
        }
  		if (isNoRegister || queryRegisterFlag) {
  			buffer.append(" ,EdocRegister register ");
  		}
  		buffer.append(" where affair.objectId=summary.id ");
  		if (isNoRegister || queryRegisterFlag) {
  			buffer.append(" and summary.id = register.distributeEdocId");
  		}
		if(Strings.isNotBlank(zcdbTable)){
			buffer.append(" and edocZcdb.affairId =affair.id ");
		}
		
		StringBuilder sbAgentAffair = getMemberOrAgentHql("affair",type, userId, edocAgent, agentToFlag, params);
		
		buffer.append(sbAgentAffair);

		if ("true".equals(condition.get("deduplication"))) {

    		
    		StringBuilder completeTimesb = new StringBuilder();
    		completeTimesb.append(" SELECT max(ctpAffair2.id) FROM CtpAffair as ctpAffair2 WHERE 1=1 ");
    		completeTimesb.append(" AND ctpAffair2.delete=0 ");
    		if(CollectionUtils.isNotEmpty(stateList)) {
    			completeTimesb.append(" and ctpAffair2.state in (:a_state) ");
    			params.put("a_state", stateList);
    		}
    		if(CollectionUtils.isNotEmpty(appList)) {
    			completeTimesb.append(" and ctpAffair2.app in (:a_app) ");
    			params.put("a_app", appList);
    		}
    		//兼容新老公文
    		if(CollectionUtils.isNotEmpty(subAppList)) {
    			completeTimesb.append(" and ctpAffair2.subApp in (:a_subApp) ");
    			params.put("a_subApp", subAppList);
    		}
    		
    		StringBuilder sbAgentAffair2 = getMemberOrAgentHql("ctpAffair2",type, userId, edocAgent, agentToFlag, params);
    		completeTimesb.append( sbAgentAffair2 );
    		
    		completeTimesb.append(" GROUP BY ctpAffair2.objectId  ");
    		
        	buffer.append(" and ");
       
        	buffer.append(" affair.id in ( ").append(completeTimesb).append(" ) ");
           
	       
			
		}

		if(CollectionUtils.isNotEmpty(stateList)) {
			buffer.append(" and affair.state in (:a_state)");
			params.put("a_state", stateList);
			List sub_state = new ArrayList();
			sub_state.add(SubStateEnum.col_pending_specialBack.getKey());
			sub_state.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
			sub_state.add(SubStateEnum.col_pending_specialBackCenter.getKey());
			if(stateList.contains(StateEnum.col_pending.getKey())){
				buffer.append(" and affair.subState not in (:affairsubState) ");
				params.put("affairsubState", sub_state);
			}
		}
		if(CollectionUtils.isNotEmpty(appList)) {
			buffer.append(" and affair.app in (:a_app)");
			params.put("a_app", appList);
		}

		buffer.append(" and affair.delete=:affair_delete");
		params.put("affair_delete",Boolean.FALSE);

		buffer.append(" and affair.objectId=summary.id");

		buffer.append(" and summary.state<>:delete_state");
		params.put("delete_state", EdocConstant.flowState.deleted.ordinal());

		/*新公文不维护EdocZcdb对象，直接通过sub_state进行获取
		 * if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
			buffer.append(" and edocZcdb.affairId =affair.id");
		}*/
		if(CollectionUtils.isNotEmpty(substateList)) {
			buffer.append(" and affair.subState in (:a_substate)");
			params.put("a_substate", substateList);
		}
		//当开启区分阅文、办文开关，区分“阅文”和“办文”
		Long accountId=(Long)condition.get("accountId");
		boolean isYueBanFlag=false;
		if(EdocSwitchHelper.showBanwenYuewen(accountId)){
			isYueBanFlag=true;
		}
		if(isYueBanFlag && processType!=-1 && processType!=0) {
			buffer.append(" and summary.processType=:s_processType");
			params.put("s_processType", processType);
		}
		if(subEdocType != -1) {
			buffer.append(" and summary.subEdocType=:s_subEdocType");
			params.put("s_subEdocType", subEdocType);
        }
		
		//过滤条件：跟踪
		if(track != -1) {
			buffer.append(" and affair.track=:track ");
			params.put("track", track);
		}
		//跟踪
        if ("isTrack".equals(conditionKey)) {
        	buffer.append(" and affair.track = 1");
        }
        //关于已经完成的filter
        if ("finishfilter".equals(conditionKey)) {
        	buffer.append(" and summary.finishDate is not null");
        }
        //关于未完成的filter
        if ("notfinishfilter".equals(conditionKey)) {
        	buffer.append(" and summary.finishDate is null");
        }
		//发起人
		if ("startMemberName".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	int index = buffer.toString().indexOf("where");
	        	String start = buffer.toString().substring(0, index);
	        	String end = buffer.toString().substring(index);
	        	String hql = start + "," + OrgMember.class.getName()+" as mem " + end;
	        	buffer = new StringBuilder();
	        	buffer.append(hql);
	        	buffer.append(" and affair.senderId=mem.id and mem.name like :startMemberName");
	        	params.put("startMemberName", "%"+textfield+"%");
			}
        }
		//标题
        else if("subject".equals(conditionKey)) {
        	if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.subject like :subject");
	        	params.put("subject", "%"+textfield+"%");
        	}
        }
		//关键字
		else if("keywords".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.keywords like :keywords");
	        	params.put("keywords", "%"+textfield+"%");
			}
        }
		//公文文号
		else if("docMark".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.docMark like :docMark");
	        	params.put("docMark", "%"+textfield+"%");
			}
        }
		//内部文号
		else if("docInMark".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.serialNo like :serialNo");
	        	params.put("serialNo", "%"+textfield+"%");
			}
        }
		//来文单位
		else if("sendUnit".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.sendUnit like :sendUnit");
	        	params.put("sendUnit", "%"+textfield+"%");
			}
        }
		//退回人
		else if("backBoxPeople".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.extProps like :backBoxPeople");
	        	params.put("backBoxPeople", "%"+textfield+"%");
			}
        }
		//文件秘级
		else if("secretLevel".equals(conditionKey) && Strings.isNotBlank(textfield)) {
	        buffer.append(" and summary.secretLevel = :secretLevel");
            params.put("secretLevel", textfield);
        }
		//紧急程度
		else if("urgentLevel".equals(conditionKey) && Strings.isNotBlank(textfield)) {
            buffer.append(" and summary.urgentLevel = :urgentLevel");
            params.put("urgentLevel", textfield);
        }
		//行文类型
		else if("sendType".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.sendType = :sendType");
	        	params.put("sendType", textfield);
			}
        }
		//流程节点
		else if("nodePolicy".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.nodePolicy = :nodePolicy");
	        	params.put("nodePolicy", textfield);
			}
        }
		//
		else if ("cusSendType".equals(conditionKey)) {
	    	if(Strings.isNotBlank(textfield)) {
	        	buffer.append(" and summary.subEdocType=:subEdocType");
	        	params.put("subEdocType", Long.parseLong(textfield));
	        }
	    }
		//成文单位
        if ("edocUnit".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield)) {
				//buffer.append(" and register.edocUnit like :r_edocUnit");
				buffer.append(" and summary.sendUnit like :r_edocUnit");
	        	params.put("r_edocUnit", "%"+textfield+"%");
			}
        }
		//发起时间
		else if("createDate".equals(conditionKey)) {
        	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.parseDatetime(textfield + " 00:00:00");
				buffer.append(" and summary.createTime >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.parseDatetime(textfield1 + " 23:59:59");
				buffer.append(" and summary.createTime <= :timestamp2");
				params.put("timestamp2", stamp);
			}
        }
		//退回时间
		else if("backDate".equals(conditionKey)) {
	    	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and affair.updateDate >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and affair.updateDate <= :timestamp2");
				params.put("timestamp2", stamp);
			}
        }
		//退回方式
		else if ("backBox".equals(conditionKey) || "retreat".equals(conditionKey)) {
        	if (Strings.isNotBlank(textfield)) {
				buffer.append(" and affair.subeState >= :a_subState2");
				params.put("a_subState2", Integer.parseInt(textfield));
			}
        }
		//签收时间
        else if ("recieveDate".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
				if (Strings.isNotBlank(textfield)) {
					java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
					buffer.append(" and register.recTime >= :timestamp1");
					params.put("timestamp1", stamp);
				}
				if (Strings.isNotBlank(textfield1)) {
					java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
					//OA-24096 收文-待办列表，小查询，根据签收时间查询，什么都不选，可以查出所有的数据。但是，不论是开始还是结束时间，随便选个时间，就查不出一条数据。结果为空。
					buffer.append(" and register.recTime <= :timestamp2");
					params.put("timestamp2", stamp);
				}
			}
        }
		//登记时间
        else if ("registerDate".equals(conditionKey)) {
			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
				buffer.append(" and summary.id = register.distributeEdocId");
				if (Strings.isNotBlank(textfield)) {
					java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
					buffer.append(" and register.registerDate >= :timestamp1");
					params.put("timestamp1", stamp);
				}
				if (Strings.isNotBlank(textfield1)) {
					java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
					buffer.append(" and register.registerDate <= :timestamp2");
					params.put("timestamp2", stamp);
				}
			}
        }
        //到达时间
        else if ("receiveTime".equals(conditionKey)) {
			if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and affair.receiveTime >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and affair.receiveTime <= :timestamp2");
				params.put("timestamp2", stamp);
			}
        }//处理期限
        else if("expectprocesstime".equals(conditionKey)){
        	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and affair.expectedProcessTime >= :timestamp1 and affair.expectedProcessTime is not null");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and affair.expectedProcessTime <= :timestamp2 and affair.expectedProcessTime is not null");
				params.put("timestamp2", stamp);
			}
        }else if("deadlineDatetime".equals(conditionKey)){
        	if (Strings.isNotBlank(textfield)) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(textfield);
				buffer.append(" and summary.deadlineDatetime >= :timestamp1");
				params.put("timestamp1", stamp);
			}
			if (Strings.isNotBlank(textfield1)) {
				java.util.Date stamp = Datetimes.getTodayLastTime(textfield1);
				buffer.append(" and summary.deadlineDatetime <= :timestamp2");
				params.put("timestamp2", stamp);
			}
        }else if("workflowState".equals(conditionKey)){
            buffer.append(" and summary.state=:summaryState ");
            if ("0".equals(textfield)) {
            	params.put("summaryState",EdocConstant.flowState.run.ordinal());
            } else if ("1".equals(textfield)) {
            	params.put("summaryState",EdocConstant.flowState.finish.ordinal());
            } else if ("2".equals(textfield)) {
            	params.put("summaryState",EdocConstant.flowState.terminate.ordinal());
            }
        }else if("exchangeMode".equals(conditionKey)){//交换方式
        	if (Strings.isNotBlank(textfield)) {
				buffer.append(" and register.exchangeMode = :exchangeMode");
				params.put("exchangeMode", Integer.parseInt(textfield));
			}
        }
        //公文类型
        if(edocType != -1) {
        	buffer.append(" and summary.edocType=:edocType");
        	params.put("edocType", edocType);
        }
        if(CollectionUtils.isNotEmpty(stateList)) {
			buffer.append(" and affair.state in (:a_state) ");
			params.put("a_state", stateList);
		}
        if("secretLevel".equals(conditionKey) && Strings.isNotBlank(textfield)) {
	        buffer.append(" and summary.secretLevel = :secretLevel");
            params.put("secretLevel", textfield);
        }
        //过滤删除
        buffer.append(" and affair.delete=:affair_delete ");
		params.put("affair_delete", Boolean.FALSE);
        
        //排序
        if(type == EdocNavigationEnum.LIST_TYPE_PENDING) {
        	buffer.append(" order by affair.receiveTime desc");
		} else if(type == EdocNavigationEnum.LIST_TYPE_DONE) {
			buffer.append(" order by affair.completeTime desc");
		} else {
			buffer.append(" order by affair.createDate desc");
		}
        
        List<Object[]> result = null;
        if(condition.containsKey("isPagination") && Boolean.FALSE.equals((Boolean)condition.get("isPagination"))) {//不需分页
        	result = edocSummaryDao.find(buffer.toString(), -1, -1, params);
        } else {
        	result = edocSummaryDao.find(buffer.toString(), params);
        }
		return result;
	}


	private StringBuilder getMemberOrAgentHql(String alias,int type, Long userId, List<AgentModel> edocAgent, boolean agentToFlag, Map<String, Object> params) {
		StringBuilder sbAgent = new StringBuilder();
		//代理人
		if(edocAgent != null && !edocAgent.isEmpty()){
			if (!agentToFlag) {////////////////////////////代理人
				sbAgent.append(" and (").append(alias).append(".memberId=:userId");
				//待办、在办，已办，已办结列表需要体现公文代理数据
				if (type==EdocNavigationEnum.LIST_TYPE_PENDING || type==EdocNavigationEnum.LIST_TYPE_DONE
					|| type == EdocNavigationEnum.LIST_TYPE_SENT ) {
					sbAgent.append(" or (");
					for(int i=0; i<edocAgent.size(); i++) {
						if(i != 0) {sbAgent.append(" or ");}
						AgentModel agent = edocAgent.get(i);
						sbAgent.append(" (").append(alias).append(".memberId=:edocAgentToId"+i+" and ").append(alias).append(".receiveTime>=:proxyCreateDate"+i+")");
						params.put("edocAgentToId"+i, agent.getAgentToId());
						params.put("proxyCreateDate"+i, agent.getStartDate());
					}
					sbAgent.append(")");
				}
				sbAgent.append(")");
				params.put("userId", userId);
			} else {////////////////////////////被代理人
				//被代理人待办，在待，已办，已办结列表不体现公文代理数据
				sbAgent.append(" and ").append(alias).append(".memberId=:userId");
				params.put("userId", userId);
			}		
        } else {//非代理人正常显示公文数据
        	sbAgent.append(" and ").append(alias).append(".memberId=:userId");
			params.put("userId", userId);
        }
		return sbAgent;
	}

	/**
	 *
	 * @param type
	 * @param condition
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public List<Object[]> combQueryByCondition(int type, Map<String, Object> condition, EdocSearchModel em) {
		List<Integer> stateList = (List<Integer>)condition.get("stateList");
		List<Integer> substateList = (List<Integer>)condition.get("substateList");
		List<Integer> appList = (List<Integer>)condition.get("appList");
		String listType = condition.get("listType")==null ? "listSendAll" : (String)condition.get("listType");
		long processType = condition.get("processType")==null?-1:(Integer)condition.get("processType");
		long subEdocType = condition.get("subEdocType")==null?-1:(Long)condition.get("subEdocType");
		int track = condition.get("track")==null?-1:(Integer)condition.get("track");
        Long userId = (Long)condition.get("userId");

		List<AgentModel> edocAgent = condition.get("edocAgent") == null ? new ArrayList<AgentModel>() : (List<AgentModel>)condition.get("edocAgent");
        boolean agentToFlag = condition.get("agentToFlag") == null ? false : (Boolean)condition.get("agentToFlag");

        StringBuilder buffer = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        //在办表加入暂存代办提醒时间列
  		String zcdbTimeSelect = "";
  		String zcdbTable = "";
  		if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
  			zcdbTable = ", EdocZcdb as edocZcdb";
  			zcdbTimeSelect = ", edocZcdb.zcdbTime ";
  		}
  		//发文/签报SQL
  		buffer.append("select "+selectAffair + zcdbTimeSelect+" from CtpAffair as affair,EdocSummary as summary" +zcdbTable);
  		//是否连查EdocRegister
		boolean isNoRegister = false;
  		//收文管理  -- 成文单位/登记时间
  		if (Strings.isNotBlank(em.getSendUnit()) || em.getRegisterDateB()!=null || em.getRegisterDateE()!=null) {
  			isNoRegister = true;
  		}
  		//收文管理  -- 签收时间
  		if (em.getRecieveDateB()!=null || em.getRecieveDateE()!=null) {
  			isNoRegister = true;
  		}
  		if (isNoRegister) {
  			buffer.append(",EdocRegister register where summary.id = register.distributeEdocId ");
  		} else {
  			buffer.append(" where 1=1 ");
  		}
  		buffer.append(" and affair.objectId=summary.id ");
		if(Strings.isNotBlank(zcdbTable)){
			buffer.append(" and edocZcdb.affairId =affair.id ");
		}
  		if ("true".equals(condition.get("deduplication"))) {
  			buffer.append(" and affair.id in ( select max(affair.id) from CtpAffair as affair,EdocSummary as summary" + zcdbTable);
  			if (isNoRegister) {
  	  			buffer.append(",EdocRegister register where summary.id = register.distributeEdocId ");
  	  		} else {
  	  			buffer.append(" where 1=1 ");
  	  		}
		}

        buffer.append(" and affair.objectId=summary.id");
		buffer.append(" and summary.state<>:delete_state ");
		params.put("delete_state", EdocConstant.flowState.deleted.ordinal());

		buffer.append(" and affair.delete=:affair_delete ");
		params.put("affair_delete", Boolean.FALSE);

		if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
			buffer.append(" and edocZcdb.affairId =affair.id");
		}
		if(CollectionUtils.isNotEmpty(stateList)) {
			buffer.append(" and affair.state in (:a_state)");
			params.put("a_state", stateList);
			List sub_state = new ArrayList();
			sub_state.add(SubStateEnum.col_pending_specialBack.getKey());
			sub_state.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
			sub_state.add(SubStateEnum.col_pending_specialBackCenter.getKey());
			if(stateList.contains(StateEnum.col_pending.getKey())){
				buffer.append(" and affair.subState not in (:affairsubState) ");
				params.put("affairsubState", sub_state);
			}
		}
		if(CollectionUtils.isNotEmpty(substateList)) {
			buffer.append(" and affair.subState in (:a_substate)");
			params.put("a_substate", substateList);
		}
		//区分是收文办文(1)、收文阅文(2)、分发草稿箱(3)魏俊标
		String isG6 = SystemProperties.getInstance().getProperty("edoc.isG6");
		if("true".equals(isG6) && processType!=-1 && processType!=0) {
			buffer.append(" and summary.processType=:s_processType");
			params.put("s_processType", processType);
		}
		//公文单类型
		if(subEdocType != -1) {
			buffer.append(" and summary.subEdocType=:s_subEdocType");
			params.put("s_subEdocType", subEdocType);
        }
		//代理人
		StringBuffer sbAgent = new StringBuffer();
		if(edocAgent != null && !edocAgent.isEmpty()){
			if (!agentToFlag) {////////////////////////////代理人
				sbAgent.append(" and (affair.memberId=:userId");
				//待办、在办，已办，已办结列表需要体现公文代理数据
				if (type==EdocNavigationEnum.LIST_TYPE_PENDING || type==EdocNavigationEnum.LIST_TYPE_DONE) {
					sbAgent.append(" or (");
					for(int i=0; i<edocAgent.size(); i++) {
						if(i != 0) {sbAgent.append(" or ");}
						AgentModel agent = edocAgent.get(i);
						sbAgent.append(" (affair.memberId=:edocAgentToId"+i+" and affair.receiveTime>=:proxyCreateDate"+i+")");
						params.put("edocAgentToId"+i, agent.getAgentToId());
						params.put("proxyCreateDate"+i, agent.getStartDate());
					}
					sbAgent.append(")");
				}
				sbAgent.append(")");
				params.put("userId", userId);
			} else {////////////////////////////被代理人
				//被代理人待办，在待，已办，已办结列表不体现公文代理数据
				sbAgent.append(" and affair.memberId=:userId");
				params.put("userId", userId);
			}
        } else {//非代理人正常显示公文数据
        	sbAgent.append(" and affair.memberId=:userId");
			params.put("userId", userId);
        }
		buffer.append(sbAgent);
		//过滤条件：跟踪
		if(track != -1) {
			buffer.append(" and affair.track=:track ");
			params.put("track", track);
		}
		if(CollectionUtils.isNotEmpty(appList)) {
			buffer.append(" and affair.app in (:a_app)");
			params.put("a_app", appList);
		}
		
        //发起人
        if (Strings.isNotBlank(em.getCreatePerson())) {
        	int index = buffer.toString().indexOf("where");
        	String start = buffer.toString().substring(0, index);
        	String end = buffer.toString().substring(index);
        	String hql = start + "," + OrgMember.class.getName()+" as mem " + end;
        	buffer = new StringBuilder();
        	buffer.append(hql);
        	buffer.append(" and affair.senderId=mem.id and mem.name like :startMemberName");
        	params.put("startMemberName", "%"+em.getCreatePerson()+"%");
        }
        //标题
        if (Strings.isNotBlank(em.getSubject())) {
        	buffer.append(" and summary.subject like :subject");
        	params.put("subject", "%"+specialString(em.getSubject())+"%");
        }
        //主题词
        if (!Strings.isBlank(em.getKeywords())) {
        	buffer.append(" and summary.keywords like :keywords");
        	params.put("keywords", "%"+specialString(em.getKeywords())+"%");
        }
        //公文文号
        if (Strings.isNotBlank(em.getDocMark())) {
        	buffer.append(" and summary.docMark like :docMark");
        	params.put("docMark", "%"+specialString(em.getDocMark())+"%");
        }
        //内部文号
	    if (Strings.isNotBlank(em.getSerialNo())) {
	    	buffer.append(" and summary.serialNo like :serialNo");
	    	params.put("serialNo", "%"+specialString(em.getSerialNo())+"%");
	    }
        //文件秘级
        if (Strings.isNotBlank(em.getSecretLevel())) {
            buffer.append(" and summary.secretLevel = :secretLevel");
            params.put("secretLevel", em.getSecretLevel());
        }
        //紧急程度
        if (Strings.isNotBlank(em.getUrgentLevel())) {
            buffer.append(" and summary.urgentLevel = :urgentLevel");
            params.put("urgentLevel", em.getUrgentLevel());
        }
        //行文类型
        if (Strings.isNotBlank(em.getSendType())) {
        	buffer.append(" and summary.sendType = :sendType");
        	params.put("sendType", em.getSendType());
        }
        //流程节点
        if (Strings.isNotBlank(em.getNodePolicy())) {
        	buffer.append(" and affair.nodePolicy = :nodePolicy");
        	params.put("nodePolicy", em.getNodePolicy());
        }
        //成文单位
        if (Strings.isNotBlank(em.getSendUnit())) {
        	//buffer.append(" and register.edocUnit like :r_edocUnit");
        	buffer.append(" and summary.sendUnit like :r_edocUnit");
        	params.put("r_edocUnit", "%" + specialString(em.getSendUnit()) + "%");
        }
      //流程状态
        if (em.getSummaryState() != null) {
        	buffer.append(" and summary.state =:summaryState");
        	params.put("summaryState", em.getSummaryState());
        }
        
        //流程期限
        if(em.getWorkflowDateB() != null || em.getWorkflowDateE() != null) {
        	if (em.getWorkflowDateB()!=null) {
        		java.util.Date stamp = Datetimes.getTodayFirstTime(em.getWorkflowDateB());
				buffer.append(" and summary.deadlineDatetime >= :deadlineDatetimeB");
				params.put("deadlineDatetimeB", stamp);
			}
			if (em.getWorkflowDateE()!=null) {
				java.util.Date stamp = Datetimes.getTodayLastTime(em.getWorkflowDateE());
				buffer.append(" and summary.deadlineDatetime <= :deadlineDatetimeE");
				params.put("deadlineDatetimeE", stamp);
			}
        }
        //发起时间
        if (em.getCreateTimeB()!=null || em.getCreateTimeE()!=null) {
        	if (em.getCreateTimeB()!=null) {
        		java.util.Date stamp = Datetimes.getTodayFirstTime(em.getCreateTimeB());
				buffer.append(" and summary.createTime >= :a_createDate1");
				params.put("a_createDate1", stamp);
			}
			if (em.getCreateTimeE()!=null) {
				java.util.Date stamp = Datetimes.getTodayLastTime(em.getCreateTimeE());
				buffer.append(" and summary.createTime <= :a_createDate2");
				params.put("a_createDate2", stamp);
			}
        }
        //签收时间
        if (em.getRecieveDateB()!=null || em.getRecieveDateE()!=null) {
  			if (em.getRecieveDateB()!=null) {
  				java.util.Date stamp = Datetimes.getTodayFirstTime(em.getRecieveDateB());
				buffer.append(" and register.recTime >= :r_recTime1");
				params.put("r_recTime1", stamp);
  			}
  			if (em.getRecieveDateE()!=null) {
  				java.util.Date stamp = Datetimes.getTodayLastTime(em.getRecieveDateE());
  				buffer.append(" and register.recTime <= :r_recTime2");
				params.put("r_recTime2", stamp);
  			}
        }
        //登记时间
        if (em.getRegisterDateB()!=null || em.getRegisterDateE()!=null) {
			if (em.getRegisterDateB()!=null) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(em.getRegisterDateB());
				buffer.append(" and register.registerDate >= :r_registerDate1");
				params.put("r_registerDate1", stamp);
			}
			if (em.getRegisterDateE()!=null) {
				java.util.Date stamp = Datetimes.getTodayLastTime(em.getRegisterDateE());
				buffer.append(" and register.registerDate <= :r_registerDate2");
				params.put("r_registerDate2", stamp);
			}
        }
        //到达时间
        if (em.getReceiveTimeB()!=null || em.getReceiveTimeE()!=null) {
        	if (em.getReceiveTimeB()!=null) {
				java.util.Date stamp = Datetimes.getTodayFirstTime(em.getReceiveTimeB());
				buffer.append(" and affair.receiveTime >= :a_receiveTime1");
				params.put("a_receiveTime1", stamp);
			}
			if (em.getReceiveTimeE()!=null) {
				java.util.Date stamp = Datetimes.getTodayLastTime(em.getReceiveTimeE());
				buffer.append(" and affair.receiveTime <= :a_receiveTime2");
				params.put("a_receiveTime2", stamp);
			}
        }
        
       //处理期限
        if (em.getExpectprocesstimeB() != null || em.getExpectprocesstimeE() != null) {
            if (em.getExpectprocesstimeB() != null) {
                java.util.Date stamp = Datetimes.getTodayFirstTime(em.getExpectprocesstimeB());
                buffer.append(" and affair.expectedProcessTime >= :a_expectedProcessTime1");
                params.put("a_expectedProcessTime1", stamp);
            }
            if (em.getExpectprocesstimeE() != null) {
                java.util.Date stamp = Datetimes.getTodayLastTime(em.getExpectprocesstimeE());
                buffer.append(" and affair.expectedProcessTime <= :a_expectedProcessTime2");
                params.put("a_expectedProcessTime2", stamp);
            }
        }
        
        //同一流程只显示最后一条
        if ("true".equals(condition.get("deduplication"))) {
        	buffer.append(" group by summary.id ) ");
        	buffer.append(sbAgent);
        }
        //排序
        if(type == EdocNavigationEnum.LIST_TYPE_PENDING) {
        	buffer.append(" order by affair.receiveTime desc");
		} else if(type == EdocNavigationEnum.LIST_TYPE_DONE) {
			buffer.append(" order by summary.createTime desc, affair.completeTime");
		} else {
			buffer.append(" order by affair.createDate desc");
		}
        List<Object[]> result = edocSummaryDao.find(buffer.toString(), params);
		return result;
	}

	/**
	 * 特殊字符转化
	 * @param str
	 * @return
	 */
	public String specialString(String str) {
		if(Strings.isNotBlank(str)) {
        	StringBuilder buffer=new StringBuilder();
        	for(int i=0;i<str.length();i++) {
        		if(str.charAt(i)=='\'') {
        			buffer.append("\\'");
        		} else {
        			buffer.append(str.charAt(i));
        		}
        	}
        	str = SQLWildcardUtil.escape(buffer.toString());
        }
		return str;
	}

	public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
		this.edocSummaryDao = edocSummaryDao;
	}
	/**
	 * 查询待办总数
	 * @param type
	 * @param condition
	 * @return
	 */
	public int findEdocPendingCount(int type, Map<String, Object> condition) {
		List<Integer> stateList = (List<Integer>)condition.get("stateList");
		List<Integer> substateList = (List<Integer>)condition.get("substateList");
		List<Integer> appList = (List<Integer>)condition.get("appList");
		String listType = condition.get("listType")==null ? "listSendAll" : (String)condition.get("listType");
		int edocType = condition.get("edocType")==null?-1:(Integer)condition.get("edocType");
		long processType = condition.get("processType")==null?-1:(Integer)condition.get("processType");
		long subEdocType = condition.get("subEdocType")==null?-1:(Long)condition.get("subEdocType");
		int track = condition.get("track")==null?-1:(Integer)condition.get("track");
        Long userId = (Long)condition.get("userId");

        String textfield = condition.get("textfield") == null ? null : (String)condition.get("textfield");
        String textfield1 = condition.get("textfield1") == null ? null : (String)condition.get("textfield1");
        String conditionKey = condition.get("conditionKey") == null ? null : (String)condition.get("conditionKey");

		List<AgentModel> edocAgent = condition.get("edocAgent") == null ? new ArrayList<AgentModel>() : (List<AgentModel>)condition.get("edocAgent");
        boolean agentToFlag = condition.get("agentToFlag") == null ? false : (Boolean)condition.get("agentToFlag");

        StringBuilder buffer = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();
        //在办表加入暂存代办提醒时间列
  		String zcdbTimeSelect = "";
  		String zcdbTable = "";
  		if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
  			zcdbTable = ", EdocZcdb as edocZcdb";
  			zcdbTimeSelect = ", edocZcdb.zcdbTime ";
  		}
  		//发文/签报SQL
  		buffer.append("select "+selectAffair + zcdbTimeSelect+" from CtpAffair as affair,EdocSummary as summary" +zcdbTable);

  		//是否连查EdocRegister
		boolean isNoRegister = false;
  		//收文管理  -- 成文单位
  		if ("edocUnit".equals(conditionKey) && Strings.isNotBlank(textfield)) {
			isNoRegister = true;
        }
  		//收文管理  -- 签收时间/登记时间
  		if ("recieveDate".equals(conditionKey) || "registerDate".equals(conditionKey)) {
  			if(Strings.isNotBlank(textfield) || Strings.isNotBlank(textfield1)) {
  				isNoRegister = true;
  			}
        }
  		if (isNoRegister) {
  			buffer.append(" ,EdocRegister register ");
  		}
  		buffer.append(" where affair.objectId=summary.id ");
  		if (isNoRegister) {
  			buffer.append(" and summary.id = register.distributeEdocId");
  		}
		if(Strings.isNotBlank(zcdbTable)){
			buffer.append(" and edocZcdb.affairId =affair.id ");
		}

		if ("true".equals(condition.get("deduplication"))) {
			buffer.append(" and affair.id in ( select max(affair.id) from CtpAffair as affair,EdocSummary as summary" + zcdbTable);
			if (isNoRegister) {
	  			buffer.append(" ,EdocRegister register ");
	  			buffer.append(" where summary.id = register.distributeEdocId ");
	  		} else {
	  			buffer.append(" where 1=1 ");
	  		}
		}

		if(CollectionUtils.isNotEmpty(stateList)) {
			buffer.append(" and affair.state in (:a_state)");
			params.put("a_state", stateList);
		}
		if(CollectionUtils.isNotEmpty(appList)) {
			buffer.append(" and affair.app in (:a_app)");
			params.put("a_app", appList);
		}

		buffer.append(" and affair.delete=:affair_delete");
		params.put("affair_delete",Boolean.FALSE);

		buffer.append(" and affair.objectId=summary.id");

		buffer.append(" and summary.state<>:delete_state");
		params.put("delete_state", EdocConstant.flowState.deleted.ordinal());

		if(listType.equals(EdocNavigationEnum.EdocV5ListTypeEnum.listZcdb.getKey())) {
			buffer.append(" and edocZcdb.affairId =affair.id");
		}
		if(CollectionUtils.isNotEmpty(substateList)) {
			buffer.append(" and affair.subState in (:a_substate)");
			params.put("a_substate", substateList);
		}
		//当开启区分阅文、办文开关，区分“阅文”和“办文”
		Long accountId=(Long)condition.get("accountId");
		boolean isYueBanFlag=false;
		if(EdocSwitchHelper.showBanwenYuewen(accountId)){
			isYueBanFlag=true;
		}
		if(isYueBanFlag && processType!=-1 && processType!=0) {
			buffer.append(" and summary.processType=:s_processType");
			params.put("s_processType", processType);
		}
		if(subEdocType != -1) {
			buffer.append(" and summary.subEdocType=:s_subEdocType");
			params.put("s_subEdocType", subEdocType);
        }
		StringBuffer sbAgent = new StringBuffer();
		//代理人
		if(edocAgent != null && !edocAgent.isEmpty()){
			if (!agentToFlag) {////////////////////////////代理人
				sbAgent.append(" and (affair.memberId=:userId");
				//待办、在办，已办，已办结列表需要体现公文代理数据
				if (type==EdocNavigationEnum.LIST_TYPE_PENDING || type==EdocNavigationEnum.LIST_TYPE_DONE
					||(EdocHelper.isG6Version()&&type == EdocNavigationEnum.LIST_TYPE_SENT)) {
					sbAgent.append(" or (");
					for(int i=0; i<edocAgent.size(); i++) {
						if(i != 0) {sbAgent.append(" or ");}
						AgentModel agent = edocAgent.get(i);
						sbAgent.append(" (affair.memberId=:edocAgentToId"+i+" and affair.receiveTime>=:proxyCreateDate"+i+")");
						params.put("edocAgentToId"+i, agent.getAgentToId());
						params.put("proxyCreateDate"+i, agent.getStartDate());
					}
					sbAgent.append(")");
				}
				sbAgent.append(")");
				params.put("userId", userId);
			} else {////////////////////////////被代理人
				//被代理人待办，在待，已办，已办结列表不体现公文代理数据
				sbAgent.append(" and affair.memberId=:userId");
				params.put("userId", userId);
			}
        } else {//非代理人正常显示公文数据
        	sbAgent.append(" and affair.memberId=:userId");
			params.put("userId", userId);
        }
		buffer.append(sbAgent);
		//过滤条件：跟踪
		if(track != -1) {
			buffer.append(" and affair.track=:track ");
			params.put("track", track);
		}

        //公文类型
        if(edocType != -1) {
        	buffer.append(" and summary.edocType=:edocType");
        	params.put("edocType", edocType);
        }
        //同一流程只显示最后一条
        if ("true".equals(condition.get("deduplication"))) {
        	buffer.append(" group by summary.id ) ");
        	buffer.append(sbAgent);
        }
        //排序
        if(type == EdocNavigationEnum.LIST_TYPE_PENDING) {
        	buffer.append(" order by affair.receiveTime desc");
		} else if(type == EdocNavigationEnum.LIST_TYPE_DONE) {
			buffer.append(" order by affair.completeTime desc");
		} else {
			buffer.append(" order by affair.createDate desc");
		}
       int selectCount=edocSummaryDao.count(buffer.toString(), params);
	   return selectCount;
	}
	
    private List<List<Long>>  createList(List<Long> targe,int size) {  
        List<List<Long>> listArr = new ArrayList<List<Long>>();  
        //获取被拆分的数组个数  
        int arrSize = targe.size()%size==0?targe.size()/size:targe.size()/size+1;  
        for(int i=0;i<arrSize;i++) {  
            List<Long>  sub = new ArrayList<Long>();  
            //把指定索引数据放入到list中  
            for(int j=i*size;j<=size*(i+1)-1;j++) {  
                if(j<=targe.size()-1) {  
                    sub.add(targe.get(j));  
                }  
            }  
            listArr.add(sub);  
        }  
        return listArr;  
    }  
    
    /**
     * 为M3公文首页查询各类型各状态数据准备
     * @return
     */
    public List<Object[]> getCountGroupByEdocType(Map<String, Object> condition){
    	Map<String, Object> params = new HashMap<String, Object>();
    	Long userId = (Long)condition.get("userId");
    	
    	StringBuffer sql = new StringBuffer();
    	sql.append(" select count(affair.id), affair.app, affair.state, affair.subState, affair.memberId, summary.govdocType ");
    	sql.append("   from CtpAffair affair, EdocSummary summary ");
    	sql.append("  where affair.objectId = summary.id ");
    	sql.append("    and affair.delete = 0 ");
    	sql.append("    and summary.state <> 4 ");
    	sql.append("    and summary.govdocType in (1,2,3) ");

    	List<AgentModel> edocAgent = condition.get("edocAgent") == null ? new ArrayList<AgentModel>() : (List<AgentModel>)condition.get("edocAgent");
        boolean agentToFlag = condition.get("agentToFlag") == null ? false : (Boolean)condition.get("agentToFlag");

		StringBuffer sbAgent = new StringBuffer();
		//代理人
		if(edocAgent != null && !edocAgent.isEmpty()){
			if (!agentToFlag) { //代理人
				sbAgent.append(" and (affair.memberId=:userId");
				sbAgent.append(" or (");
				for(int i=0; i<edocAgent.size(); i++) {
					if(i != 0) {sbAgent.append(" or ");}
					AgentModel agent = edocAgent.get(i);
					sbAgent.append(" (affair.memberId=:edocAgentToId"+i+" and affair.receiveTime>=:proxyCreateDate"+i+")");
					params.put("edocAgentToId"+i, agent.getAgentToId());
					params.put("proxyCreateDate"+i, agent.getStartDate());
				}
				sbAgent.append(")");
				sbAgent.append(")");
				params.put("userId", userId);
			} else {//被代理人
				//被代理人待办，在待，已办，已办结列表不体现公文代理数据
				sbAgent.append(" and affair.memberId=:userId");
				params.put("userId", userId);
			}
        } else {//非代理人正常显示公文数据
        	sbAgent.append(" and affair.memberId=:userId");
			params.put("userId", userId);
        }
		sql.append(sbAgent);
		
    	sql.append("  group by affair.app, affair.state, affair.subState, affair.memberId, summary.govdocType ");
    	return DBAgent.find(sql.toString(), params);
    }
}
