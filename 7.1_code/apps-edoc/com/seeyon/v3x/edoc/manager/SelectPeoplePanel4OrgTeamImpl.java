/**
 * 
 */
package com.seeyon.v3x.edoc.manager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONArray;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.selectpeople.manager.AbstractSelectPeoplePanel;
import com.seeyon.v3x.edoc.domain.EdocObjTeam;

/**
 * @author <a href="tanmf@seeyon.com">Tanmf</a>
 * @date 2012-11-15 
 */
public class SelectPeoplePanel4OrgTeamImpl extends AbstractSelectPeoplePanel{
	private static final Log log = LogFactory.getLog(SelectPeoplePanel4OrgTeamImpl.class);

    private EdocObjTeamManager edocObjTeamManager;
    
    public void setEdocObjTeamManager(EdocObjTeamManager edocObjTeamManager) {
        this.edocObjTeamManager = edocObjTeamManager;
    }

    @Override
    public String getJsonString(long memberId, long loginAccountId,String extParams) throws BusinessException {
        StringBuilder a = new StringBuilder();
        StringBuilder b = new StringBuilder();
        a.append("[");
        int i = 0;
        List<EdocObjTeam> eas = this.edocObjTeamManager.findAllSimpleNotPager(loginAccountId);
		Collections.sort(eas, new Comparator<EdocObjTeam>() {
			public int compare(EdocObjTeam key1, EdocObjTeam key2) {
		    	if(key1 == null || key2 == null){
		    		return -1;
		    	}
		    	
		    	int id1 = key1.getSortId() == null?1:key1.getSortId();
		    	int id2 = key2.getSortId() == null?1:key2.getSortId();
		    	
		        if(id1 == id2){
		            return key1.getId().compareTo(key2.getId());
		        }else{
		        	return id1 > id2 ? 1 : (id1 < id2 ? -1 : 0);
		        }
			}
		});
        for (EdocObjTeam t : eas) {
            if(i++ != 0){
                a.append(",");
                b.append(",");
            }
            t.toJsonString(a);
            b.append(t.getId());
        }
        a.append("]");
        Map<Long,String> map = this.edocObjTeamManager.getOrgTeamForDepartment(b.toString(),loginAccountId);
        if(map.size()!=0){
        	String result= addOrgTeamForDeptMent(a.toString(), map);
        	log.info("orgTeamjson: "+result);
        	return result;
        }else{
        	return a.toString();
        }
    }
    
    public Object[] getName(String id, Long accountId){
        EdocObjTeam et = edocObjTeamManager.getById(Long.parseLong(id));
        if(et == null){
            return null;
        }
        
        return new Object[]{et.getName(), et.getOrgAccountId()};
    }

    @Override
    public Date getLastModifyTimestamp(Long loginAccountId) throws BusinessException {
        return edocObjTeamManager.getLastModifyTimestamp();
    }

    @Override
    public String getType() {
        return "OrgTeam";
    }

    /**
    * @Title: addOrgTeamForDeptMent
    * @Description: TODO 添加机构组下的部门信息
    * @param @param orgTeamJson
    * @param @return    设定文件
    * @return String    返回类型
    * @return Map<Long,String>    返回类型
    * @throws
    */
	private  String addOrgTeamForDeptMent(String orgTeamJson,Map<Long,String> map) {
	  	StringBuffer resultJson=new StringBuffer();
	  	try{
	  		List<Map<String, String>> orgTeamList = (List<Map<String, String>>) JSONArray.parse(orgTeamJson);
	  		int i = 0;
	  		resultJson.append("[");
	  		for (Map<String, String> mapList : orgTeamList) {
	  			if(i++ !=0){
	  				resultJson.append(",");
	  			}
	  			resultJson.append("{");
	  			for (Map.Entry entry : mapList.entrySet()) {
	  				resultJson.append(entry.getKey()+":\""+entry.getValue()+"\"");
	  				if("K".equals(entry.getKey())){
	  					if(entry.getValue() != null){
	  						String orgTeamStr = map.get(Long.parseLong(entry.getValue().toString()));
	  						if(!StringUtils.isBlank(orgTeamStr)){
	  							resultJson.append(",M:"+orgTeamStr+",");
	  						}else{
	  							resultJson.append(",");
	  						}
	  					}
	  				}
	  			}
	  			resultJson.append("}");
	  		}
	  		resultJson.append("]");
	  		return resultJson.toString();
	  	}catch(Exception e){
	  		log.error("机构组转换选人界面数据异常.");
	  	}
	  	return "[]";
	}
	
}
