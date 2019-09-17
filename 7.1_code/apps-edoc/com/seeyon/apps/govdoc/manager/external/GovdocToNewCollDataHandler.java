package com.seeyon.apps.govdoc.manager.external;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.api.NewCollDataHandler;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Attachment;

/**
 * 公文转协同的数据获取实现类
 * @author 唐桂林
 *
 */
public class GovdocToNewCollDataHandler extends NewCollDataHandler {
	
	private static final Log LOGGER = LogFactory.getLog(GovdocToNewCollDataHandler.class);
	private AffairManager affairManager;
	
	@Override
	public String getHandlerName() {
		return "govdoc";
	}

	@Override
	public String getSubject(Map<String, Object> params) {
		CtpAffair bean = (CtpAffair) params.get("edocAffairObj");
        return bean.getSubject();
	}
	
	@Override
	public Map<String, Object> getParams(String sourceId, String extendInfo) {
		Map<String, Object> params = new HashMap<String, Object>();
        try {
			Long affairId = Long.parseLong(sourceId);
			CtpAffair bean = affairManager.get(affairId);
			params.put("edocAffairObj", bean);
		} catch (BusinessException e) {
			LOGGER.error("", e);
		}
        return params;
	}

	@Override
	public List<Attachment> getAttachments(Map<String, Object> params) {
		CtpAffair affair = (CtpAffair) params.get("edocAffairObj");
		List<Attachment> attList = new ArrayList<Attachment>();
		String subject = affair.getSubject();
		//对标题中的换行符进行处理
		subject = subject.replaceAll("\n", " ");
		//将原文作为关联文档添加进协同中
		Attachment attachment = new Attachment();
        attachment.setGenesisId(affair.getId());
        attachment.setCategory(1);
        attachment.setFilename(subject);
        attachment.setDescription(affair.getId()+"");
        attachment.setCreatedate(new Date());
        attachment.setFileUrl(affair.getId());
        attachment.setMimeType("edoc");
        attachment.setType(2);
        attachment.setSize(0l);
        attList.add(attachment);
		
		return attList;
	}
	
	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}
	
}
