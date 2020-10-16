/*
 * Created on 2005-2-1
 *
 */
package net.joinwork.bpm.engine.execute;

import java.util.Date;
import java.util.List;

import net.joinwork.bpm.engine.wapi.CaseInfo;

/**
 * @author dinghong
 *
 */
public class BPMCaseInfo extends CaseInfo {
	public void setIsSubCase(boolean isSubCase) {
		this.isSubCase = isSubCase;
	}
	/**
		 * @param b
		 */
	public void setCanCancel(boolean b) {
		canCancel = b;
	}

	/**
	 * @param b
	 */
	public void setCanQueryRunlog(boolean b) {
		canQueryRunlog = b;
	}

	/**
	 * @param b
	 */
	public void setCanQueryState(boolean b) {
		canQueryState = b;
	}

	/**
	 * @param b
	 */
	public void setCanReplaceProcess(boolean b) {
		canReplaceProcess = b;
	}

	/**
	 * @param b
	 */
	public void setCanSuspend(boolean b) {
		canSuspend = b;
	}

	/**
	 * @param i
	 */
	public void setCaseId(long i) {
		caseId = i;
	}

	/**
	 * @param string
	 */
	public void setCaseName(String string) {
		caseName = string;
	}

	/**
	 * @param date
	 */
	public void setFinishDate(Date date) {
		finishDate = date;
	}

	/**
	 * @param string
	 */
	public void setProcessId(String string) {
		processId = string;
	}

	/**
	 * @param string
	 */
	public void setProcessName(String string) {
		processName = string;
	}

	/**
	 * @param date
	 */
	public void setStartDate(Date date) {
		startDate = date;
	}

	/**
	 * @param string
	 */
	public void setStartUser(String string) {
		startUser = string;
	}

	/**
	 * @param i
	 */
	public void setState(int i) {
		state = i;
	}

	/**
	 * @param string
	 */
	public void setStateName(String string) {
		stateName = string;
	}

	/**
	 * @param list
	 */
	public void setStatusList(List list) {
		statusList = list;
	}
	/**
		 * @param string
		 */
	public void setProcessIndex(String string) {
		processIndex = string;
	}
	/**
		 * @param string
		 */
	public void setLastPerformer(String string) {
		lastPerformer = string;
	}

}
