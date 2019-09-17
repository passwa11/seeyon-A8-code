package com.seeyon.v3x.edoc.util;

import java.util.List;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocElementManager;
import com.seeyon.v3x.edoc.manager.EdocFormManager;
import com.seeyon.ctp.util.Strings;

public class MetaUtil {
	private final static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
			.getLog(MetaUtil.class);
	
	
	public static void refMeta(EdocSummary summary)
	{
		EdocFormManager edocFormManager= (EdocFormManager)AppContext.getBean("edocFormManager");
		EdocElementManager elementManager=(EdocElementManager)AppContext.getBean("edocElementManager");
		EnumManager metadataManager=(EnumManager)AppContext.getBean("enumManagerNew");


		List list = null;
//		try{
//		list=edocFormManager.getEdocFormElementByFormId(summary.getFormId());
//		}catch(Exception e)
//		{
//			log.error(e.getMessage(), e);
//		} 
		//因为前面已经从数据库查询过使用表单的所有公文元素了，避免再次查询，也不对程序结构做太大的调整，因此将公文元素存进ThreadLocal中
		list = SharedWithThreadLocal.getEdocFormElements();
		
		
		EdocElement element=null;
		CtpEnumBean metadata = null;;
		Long elementId = null;
		int i,len=list.size();
		for(i=0;i<len;i++)
		{
			elementId = ((EdocFormElement)list.get(i)).getElementId();
			if(null==elementId){continue;}	
			String elementIdStr = elementId.toString();
			if(elementIdStr.length()==1){
				elementIdStr = "00"+elementIdStr;
			}else if(elementIdStr.length()==2){
				elementIdStr = "0"+elementIdStr;				
			}
			element=elementManager.getEdocElement(elementIdStr);
			if(null!=element){
				Long metadataId = element.getMetadataId();
				if(null==metadataId){continue;}
					metadata = metadataManager.getEnum(metadataId);
					setEdocSummaryValue(summary,element.getFieldName(), metadata);
			}
		} 
				
	}
	/**
	 * 把html页面提交过来的input的值设置到edocsummary对象内
	 * @param summary
	 * @param inputName
	 * @param inputValue
	 * @return
	 */
	public static void setEdocSummaryValue(EdocSummary summary,String inputName, CtpEnumBean metadata)
	{		 
		if("doc_type".equals(inputName))
		{			
			refMetadataItem(summary.getDocType(), metadata);
		}
		else if("send_type".equals(inputName))
		{			
			refMetadataItem(summary.getSendType(), metadata);
		}
		else if("secret_level".equals(inputName))
		{			
			refMetadataItem(summary.getSecretLevel(), metadata);
		}
		else if("urgent_level".equals(inputName))
		{			
			refMetadataItem(summary.getUrgentLevel(), metadata);
		}
		else if("keep_period".equals(inputName))
		{
			refMetadataItem(String.valueOf(summary.getKeepPeriod()), metadata);
		}
		else if("list1".equals(inputName))
		{			
			refMetadataItem(summary.getList1(), metadata);
		}
		else if("list2".equals(inputName))
		{			
			refMetadataItem(summary.getList2(), metadata);
		}
		else if("list3".equals(inputName))
		{			
			refMetadataItem(summary.getList3(), metadata);
		}
		else if("list4".equals(inputName))
		{			
			refMetadataItem(summary.getList4(), metadata);
		}
		else if("list5".equals(inputName))
		{			
			refMetadataItem(summary.getList5(), metadata);
		}
		else if("list6".equals(inputName))
		{			
			refMetadataItem(summary.getList6(), metadata);
		}
		else if("list7".equals(inputName))
		{			
			refMetadataItem(summary.getList7(), metadata);
		}
		else if("list8".equals(inputName))
		{			
			refMetadataItem(summary.getList8(), metadata);
		}
		else if("list9".equals(inputName))
		{			
			refMetadataItem(summary.getList9(), metadata);
		}
		else if("list10".equals(inputName))
		{			
			refMetadataItem(summary.getList10(), metadata);
		}
		else if("list11".equals(inputName))
		{			
			refMetadataItem(summary.getList11(), metadata);
		}
		else if("list12".equals(inputName))
		{			
			refMetadataItem(summary.getList12(), metadata);
		}
		else if("list13".equals(inputName))
		{			
			refMetadataItem(summary.getList13(), metadata);
		}
		else if("list14".equals(inputName))
		{			
			refMetadataItem(summary.getList14(), metadata);
		}
		else if("list15".equals(inputName))
		{			
			refMetadataItem(summary.getList15(), metadata);
		}
		else if("list16".equals(inputName))
		{
			refMetadataItem(summary.getList16(), metadata);
		}
		else if("list17".equals(inputName))
		{			
			refMetadataItem(summary.getList17(), metadata);
		}
		else if("list18".equals(inputName))
		{			
			refMetadataItem(summary.getList18(), metadata);
		}
		else if("list19".equals(inputName))
		{			
			refMetadataItem(summary.getList19(), metadata);
		}
		else if("list20".equals(inputName))
		{			
			refMetadataItem(summary.getList20(), metadata);
		}
		return;
	}
	
	public static void refMetadataItem(String inputValue, CtpEnumBean metadata){ 
		if(!Strings.isBlank(inputValue) && null!=metadata){
			EnumManager metadataManager=(EnumManager)AppContext.getBean("enumManagerNew");

			//CtpEnumItem item = metadataManager.getEnumItem(metadata.getName(), inputValue);
		/*	if(null!=item){ 
				//----性能优化，不需要下面的枚举更新逻辑
//				metadataManager.refMetadata(metadata.getId(), metadata.getIsSystem());
//				metadataManager.refMetadataItem(metadata.getId(), item.getId(), metadata.getIsSystem());
			}*/
		}
	}

}
