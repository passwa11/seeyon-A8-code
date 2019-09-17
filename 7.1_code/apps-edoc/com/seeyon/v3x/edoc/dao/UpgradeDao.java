package com.seeyon.v3x.edoc.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.permission.vo.PermissionVO;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocCategory;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public interface UpgradeDao {

	List<EdocCategory> getAllEdocCategory();
	
	List<EdocForm> getAllEdocForm();

	List<PermissionVO> getPermission(String category, Long unitId);

	void upgradeAIP(Long id, Long id2);

	List<String> getTempList() throws BusinessException;

	void templateUpgrade(Map<Long, Long> formIds, Map<Long, CtpTemplateCategory> categories) throws BusinessException;

	void doBasejdqx(List<ConfigItem> sourList, List<Long> accounts);

	void insterEdocElements(List<String> sqls) throws BusinessException, SQLException;

	void saveDataRelation(Map<Long, Long> tempMap, String string) throws BusinessException, SQLException;

	void saveDataRelation2(List<Long> tempList, String string) throws BusinessException, SQLException;

	String getDbType();

	List<String> getTableName(String tables) throws Exception;

	void excuteSql(String string) throws BusinessException, SQLException;

	void deleteRoleMember(V3xOrgRole role) throws BusinessException, SQLException;

	Object getDataByHql(String hql, Map<String, Object> map, FlipInfo fileinfo);

	List<CtpAffair> getAffairBySubObjectIds(List ids);

	void updateAll(List listObj);

	void saveOrUpdateAll(List listObj);

	void saveAll(List listObj);
	
	public EdocSummary getSummarySerialNo(Long summaryI) throws BusinessException;
	
}
