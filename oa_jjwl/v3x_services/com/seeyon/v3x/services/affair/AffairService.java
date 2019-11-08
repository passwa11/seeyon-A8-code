package com.seeyon.v3x.services.affair;

import com.seeyon.v3x.services.ServiceException;

public interface AffairService {
	
	public abstract String exportPendingList(String paramString1, String paramString2, int paramInt1, int paramInt2,String paramString3,String paramString4,String paramString5,String paramString6) throws ServiceException;

	public abstract String exportAgentPendingList(String paramString1, String paramString2, int paramInt1, int paramInt2) throws ServiceException;

	public abstract String exportTrackList(String paramString1, String paramString2, int paramInt1, int paramInt2) throws ServiceException;
	
	// 标题，发起人，发起时间，处理时间
	public abstract String exportDoneList(String paramString1, String paramString2, int paramInt1, int paramInt2,String paramString3,String paramString4,String paramString5,String paramString6) throws ServiceException;
	
	// 标题，接收人，发起时间
	public abstract String exportSentList(String paramString1, String paramString2, int paramInt1, int paramInt2,String paramString3,String paramString4,String paramString5) throws ServiceException;
  
}
