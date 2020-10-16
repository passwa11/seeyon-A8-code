package com.seeyon.apps.govdoc.manager.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.permission.manager.EdocHandlerInterface;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.v3x.edoc.domain.EdocElementFlowPermAcl;
import com.seeyon.v3x.edoc.manager.EdocElementFlowPermAclManager;

/**
 * 集群调用的Handler(暂不配到spring中，老公文节点权限预置才需要)
 * @author 唐桂林
 *
 */
public class GovdocHandlerElement implements EdocHandlerInterface {

	@Override
	public void saveEdocElementPermByAccountId(Map<Long, ConfigItem> arg0)
			throws BusinessException {
		    //boolean isEdoc = false;//权限的类型－－是否为公文T
	        EdocElementFlowPermAclManager edocElementFlowPermAclManager = 
	                (EdocElementFlowPermAclManager)AppContext.getBean("edocElementFlowPermAclManager");
	        Set<Long>  longs= arg0.keySet();
	     //   long configId=0L;
	        for(Long l : longs){
	        	ConfigItem item = (ConfigItem) arg0.get(l);
	 	        List<EdocElementFlowPermAcl> elementList = null;
	 	        if(item==null)
	 	        	continue;
	 	        elementList = edocElementFlowPermAclManager.getEdocElementFlowPermAcls(item.getId());
	 	        
	 	        List<EdocElementFlowPermAcl> newElementList = new ArrayList<EdocElementFlowPermAcl>();
	 	        if(null!=elementList && elementList.size()>0){//如果是公文权限，重新赋值
	 	            for(EdocElementFlowPermAcl acl:elementList){
	 	                EdocElementFlowPermAcl newAcl = new EdocElementFlowPermAcl();
	 	                newAcl.setNewId();
	 	                newAcl.setAccess(acl.getAccess());
	 	                 newAcl.setFlowPermId(l);
	 	                newAcl.setEdocElement(acl.getEdocElement());
	 	                newElementList.add(newAcl);
	 	            }   
	 	        }
	 	        
	 	        edocElementFlowPermAclManager.saveEdocElementFlowPermAcls(newElementList);    
	        }
	                 
	        

	}

}
