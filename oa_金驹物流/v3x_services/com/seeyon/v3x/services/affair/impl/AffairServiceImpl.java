package com.seeyon.v3x.services.affair.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.oainterface.common.OAInterfaceException;
import com.seeyon.oainterface.impl.exportdata.DoneSentListExporter;
import com.seeyon.oainterface.impl.exportdata.PendingListExporter;
import com.seeyon.oainterface.impl.exportdata.TrackListExporter;
import com.seeyon.v3x.services.ErrorServiceMessage;
import com.seeyon.v3x.services.ServiceException;
import com.seeyon.v3x.services.ServiceTokenCheck;
import com.seeyon.v3x.services.affair.AffairService;

public class AffairServiceImpl implements AffairService{
	
	private static Log log = LogFactory.getLog(AffairServiceImpl.class);

	@Override
	public String exportPendingList(String tokenId, String ticketId, int firstNum, int pageSize,String subject,String sender,String sendtime,String revietime) throws ServiceException {
		
		try{
			ServiceTokenCheck.active(tokenId);
		}catch(OAInterfaceException localOAInterfaceException){
			throw new ServiceException((int)ErrorServiceMessage.authorityservice.getErroCode(),ErrorServiceMessage.authorityservice.getValue());
		}
		
//		PendingListExporter exporter = new PendingListExporter();
		
		DoneSentListExporter exporter = new DoneSentListExporter();
		
		try{
			return exporter.getPendingList(ticketId, firstNum, pageSize,subject,sender,sendtime,revietime);
		}catch(ServiceException e){
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(), e.getMessage());
		}
		
	}

	@Override
	public String exportAgentPendingList(String tokenId, String ticketId, int firstNum, int pageSize) throws ServiceException {
		
		try{
			ServiceTokenCheck.active(tokenId);
		}catch(OAInterfaceException localOAInterfaceException){
			throw new ServiceException((int)ErrorServiceMessage.authorityservice.getErroCode(),ErrorServiceMessage.authorityservice.getValue());
		}
		
		PendingListExporter exporter = new PendingListExporter();
		
		try{
			return exporter.getAgentPendingList(ticketId, firstNum, pageSize);
		}catch(ServiceException e){
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(), e.getMessage());
		}
	}

	@Override
	public String exportTrackList(String tokenId, String ticketId, int firstNum, int pageSize) throws ServiceException {
			
		try{
			ServiceTokenCheck.active(tokenId);
		}catch(OAInterfaceException localOAInterfaceException){
			throw new ServiceException((int)ErrorServiceMessage.authorityservice.getErroCode(),ErrorServiceMessage.authorityservice.getValue());
		}
		
		TrackListExporter exporter = new TrackListExporter();
		
		try{
			return exporter.getTrackList(ticketId, firstNum, pageSize);
		}catch(ServiceException e){
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(), e.getMessage());
		}
	}

	@Override
	public String exportDoneList(String tokenId, String ticketId, int firstNum, int pageSize,String subject,String sender,String sendtime,String dotime) throws ServiceException {
			
		try{
			ServiceTokenCheck.active(tokenId);
		}catch(OAInterfaceException localOAInterfaceException){
			throw new ServiceException((int)ErrorServiceMessage.authorityservice.getErroCode(),ErrorServiceMessage.authorityservice.getValue());
		}
		
		DoneSentListExporter exporter = new DoneSentListExporter();
		
		try{
			return exporter.getDoneList(ticketId, firstNum, pageSize,subject,sender,sendtime,dotime);
		}catch(ServiceException e){
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(), e.getMessage());
		}
	}

	@Override
	public String exportSentList(String tokenId, String ticketId, int firstNum, int pageSize,String subject,String revier,String sendtime) throws ServiceException {
			
		try{
			ServiceTokenCheck.active(tokenId);
		}catch(OAInterfaceException localOAInterfaceException){
			throw new ServiceException((int)ErrorServiceMessage.authorityservice.getErroCode(),ErrorServiceMessage.authorityservice.getValue());
		}
		
		DoneSentListExporter exporter = new DoneSentListExporter();
		
		try{
			return exporter.getSentList(ticketId, firstNum, pageSize,subject,revier,sendtime);
		}catch(ServiceException e){
			log.error(e.getMessage(), e);
			throw new ServiceException(e.getErrorNumber(), e.getMessage());
		}
	}

}
