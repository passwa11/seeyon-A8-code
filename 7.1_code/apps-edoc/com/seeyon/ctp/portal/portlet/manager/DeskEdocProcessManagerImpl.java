package com.seeyon.ctp.portal.portlet.manager;

import java.util.Map;

import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.portal.portlet.bo.CollaborationInfo;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.manager.EdocManager;

public class DeskEdocProcessManagerImpl implements
		DeskCollaborationProcessManager {
	private String app;
	private EdocManager edocManager;
	
	
	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	@Override
	public void setApp(String app) {
		this.app = app;
	}

	@Override
	public String getApp() {
		return this.app;
	}

	@Override
	public void finishWorkitemQuick(Map<String, String> param)
			throws BusinessException {
		//公文不做快速处理
	}

	@Override
	public CollaborationInfo getCollaboration(Map<String, String> param) throws BusinessException {
		int app = Integer.parseInt(param.get("app"));
		//String subApp = param.get("subApp");
		String objectId = param.get("objectId");
		//String sender = param.get("sender");
		String affairId = param.get("affairId");
		String subObjectId = param.get("subObjectId");
		
		CollaborationInfo info = new CollaborationInfo();
		
		String clickUrl = "";
		String contentUrl = "";
		/**
		 * TODO：这个地方最好用系统枚举
		 */
		
		if(app == ApplicationCategoryEnum.edoc.key()){//公文
			contentUrl = "/edocController.do?method=detailIFrame&from=Pending&affairId="+affairId;
            clickUrl = contentUrl;
        }else if((app == ApplicationCategoryEnum.edocSend.key())
                ||(app == ApplicationCategoryEnum.edocRec.key())){//发文
        	contentUrl = "/edocController.do?method=getContent&summaryId="+objectId+"&affairId="+affairId+"&from=Pending&openFrom=";
            clickUrl = "/edocController.do?method=detailIFrame&from=Pending&affairId="+affairId;    
        }else if(app == ApplicationCategoryEnum.edocSign.key()){//签报
            clickUrl = "/edocController.do?method=detailIFrame&from=Pending&affairId="+affairId;
            contentUrl = "/edocController.do?method=getContent&summaryId="+objectId+"&affairId="+affairId+"&from=Pending&openFrom=";
        }else if(app == ApplicationCategoryEnum.exSend.key()){//待发送公文
        	contentUrl = "/exchangeEdoc.do?method=edit&upAndDown=&id="+subObjectId+"&modelType=toSend&reSend=&affairId="+affairId+"&fromlist=";
            clickUrl = "/exchangeEdoc.do?method=sendDetail&modelType=toSend&id="+subObjectId;
            
        }else if(app == ApplicationCategoryEnum.exSign.key()){//待签收公文
            clickUrl = "/exchangeEdoc.do?method=receiveDetail&modelType=toReceive&id="+subObjectId+"&affairId="+affairId;
            contentUrl = "/exchangeEdoc.do?method=edit&upAndDown=&id="+subObjectId+"&modelType=toReceive&reSend=&affairId=&fromlist=";
        }
		else if(app == ApplicationCategoryEnum.edocRegister.key()){//待登记公文
			
			if(GovdocHelper.isG6Version()){
				contentUrl = "/edocController.do?method=newEdocRegister&comm=create&from=desktop&edocType=1&recieveId="+subObjectId+"&edocId="+objectId+"&registerType=1&sendUnitId=&registerId=-1";
				clickUrl = "/edocController.do?method=entryManager&entry=recManager&listType=newEdocRegister&comm=create&edocType=1&recieveId="+subObjectId+"&edocId="+objectId+"&registerType=1&sendUnitId=&registerId=-1&recListType=registerPending";
			
			}else{
				contentUrl = "/edocController.do?method=newEdoc&edocType=1&comm=register&quickView=true&edocId="+objectId+"&recieveId="+subObjectId;
	        	clickUrl = "/edocController.do?method=entryManager&entry=recManager&listType=newEdoc&comm=register&regeiterCompetence=register&recieveId="+subObjectId+"&edocId="+objectId+"&affairId=" + affairId;
			}
        }
		else if(app == ApplicationCategoryEnum.edocRecDistribute.key()){//G6待分发公文
			
        	contentUrl = "/edocController.do?method=newEdoc&edocType=1&comm=distribute&quickView=true&registerId="+objectId;
        	clickUrl = "/edocController.do?method=entryManager&entry=recManager&listType=newEdoc&comm=distribute&recListType=listDistribute&registerId="+objectId;
        }
		
		//设置点击弹出的处理页面
		info.setContentUrl(contentUrl);
		//设置在快速处理区显示内容的url
		info.setClickUrl(clickUrl);
		info.setShowSubmitBtn(false);
		info.setShowComment(false);
		
		String str = GovdocHelper.getEdocAttSizeAndAttDocSize(Long.parseLong(objectId));
    	String[] strs = str.split("[,]");
    	
        info.setAttSize(Integer.parseInt(strs[0]));
        info.setAttDocSize(Integer.parseInt(strs[1]));
		return info;
	}

}