package com.seeyon.v3x.edoc.webmodel;

import com.seeyon.v3x.edoc.domain.EdocStatCondition;

public class EdocStatConditionModel {

    private EdocStatCondition stat;
    private EdocStatCondition relationStat;
	public EdocStatCondition getStat() {
		return stat;
	}
	public void setStat(EdocStatCondition stat) {
		this.stat = stat;
	}
	public EdocStatCondition getRelationStat() {
		return relationStat;
	}
	public void setRelationStat(EdocStatCondition relationStat) {
		this.relationStat = relationStat;
	}
    
    
}
 