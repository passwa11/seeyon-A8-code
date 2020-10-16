package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.v3x.isearch.manager.ISearchManager;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

public class EdocManager4ISearchManager extends ISearchManager {
	/**  **/
    private static final long serialVersionUID = -3203706106690275684L;
    private EdocManager edocManager;	

	@Override
	public Integer getAppEnumKey() {
		return ApplicationCategoryEnum.edoc.getKey();
	}

	@Override
	public String getAppShowName() {		
		return null;
	}

	@Override
	public int getSortId() {
		// TODO Auto-generated method stub
		return this.getAppEnumKey();
	}

	@Override
	public List<ResultModel> iSearch(ConditionModel cModel) {		
		return edocManager.iSearch(cModel);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public EdocManager getEdocManager() {
		return edocManager;
	}

	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

}
