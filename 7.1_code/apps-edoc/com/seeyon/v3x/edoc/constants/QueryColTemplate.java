package com.seeyon.v3x.edoc.constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.util.Strings;
public class QueryColTemplate {

	public List<QueryCol> getQueryCol(String colId,PackageColValueInter inter){
		List<QueryCol> queryColList = new ArrayList<QueryCol>();
		String[] ids = colId.split(",");
		for(int i=0;i<ids.length;i++){
			QueryCol qc = new QueryCol();
			String label = EdocQueryColConstants.queryColMap.get(Integer.parseInt(ids[i]));
			qc.setLabel(label);
			List<Object> values = new ArrayList<Object>();
			inter.packageValue(label,values);
			qc.setValues(values);
			queryColList.add(qc);
		}
		return queryColList;
	}
	
	public List<QueryCol> getQueryCol(String colId,Map<String,String> leftMap,PackageColValueInter inter){
		List<QueryCol> queryColList = new ArrayList<QueryCol>();
		String[] ids = colId.split(",");
		for(int i=0;i<ids.length;i++){
			QueryCol qc = new QueryCol();
			String key = ids[i];
			String label = leftMap.get(key);
			if(Strings.isNotBlank(label)){
				qc.setKey(key);
				qc.setLabel(leftMap.get(key));
				List<Object> values = new ArrayList<Object>();
				inter.packageValue(key,values);
				qc.setValues(values);
				queryColList.add(qc);
			}
		}
		return queryColList;
	}
}
