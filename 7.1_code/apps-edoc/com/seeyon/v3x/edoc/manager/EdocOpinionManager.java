package com.seeyon.v3x.edoc.manager;

import java.util.Hashtable;

import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.v3x.edoc.dao.EdocOpinionDao;

public class EdocOpinionManager {
	
	private AffairManager affairManager;
	private EdocOpinionDao edocOpinionDao;
	
	public void setEdocOpinionDao(EdocOpinionDao edocOpinionDao)
	{
		this.edocOpinionDao=edocOpinionDao;
	}
	
	public void setAffairManager(AffairManager affairManager)
	{
		this.affairManager=affairManager;
	}
	public AffairManager getAffairManager()
	{
		return this.affairManager;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}
	
	public Hashtable getOpinion(long summaryId,long curUser,long sender)
	{
		Hashtable hs=new Hashtable();
		
		return hs;
	}
}
