package com.seeyon.apps.govdoc.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * 表单公文数据访问管理
 * @author tanggl
 *
 */
public class GovdocDao {
	
	/**
	 * 更新summary部分字段
	 * @param columns
	 * @param where
	 * @throws BusinessException
	 */
	public void update(Map<String, Object> columns, Object[][]  where)
			throws BusinessException {
		Object[][] w = where;
		if (columns == null || columns.size() == 0) {
			return;
		}

		if (w == null) {
			w = new Object[0][2];
		}

		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("update " + EdocSummary.class.getName()).append(" a set ");
		
		Set<String> keys = columns.keySet();
		int i = 0;
		for (String key : keys) {
			if (i > 0) {
				sb.append(", ");
			}
			if(columns.get(key) == null) {
				sb.append("a." + key + " = null");	
			} else {
				sb.append("a." + key + " = :"+key);
				paramMap.put(key, columns.get(key));
			}
			i++;
		}

		if (w != null && w.length > 0) {
			sb.append(" where ");
			
			for (i=0; i<w.length; i++) {
				Object[] key = w[i];
				
				if (i > 0) {
					sb.append(" and ");
				}
				sb.append("a." + key[0] + " = :" + key[0]);
				paramMap.put((String)key[0], key[1]);
			}
		}
		DBAgent.bulkUpdate(sb.toString(), paramMap);
	}
	
}
