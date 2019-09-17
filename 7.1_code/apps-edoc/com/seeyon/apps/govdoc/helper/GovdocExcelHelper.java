package com.seeyon.apps.govdoc.helper;

import com.seeyon.apps.govdoc.vo.GovdocListVO;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.DataRow;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.LocaleContext;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocElement;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 公文导出Excel辅助类
 */
public class GovdocExcelHelper extends GovdocHelper {
	
	private static final Log LOGGER = CtpLogFactory.getLog(GovdocExcelHelper.class);	
	
	/**
	 * 收发文登记簿导出Excel
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public static DataRecord getRegisterListExcel(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		List<Map<String, String>> columns = ParamUtil.getJsonDomainGroup("columnDomain");
		
		DataRecord dataRecord = new DataRecord();
	    DataRow[] datarow = new DataRow[flipInfo.getData().size()];
	    
		//数据
		if(Strings.isNotEmpty(flipInfo.getData())) {
			for(int i=0; i<flipInfo.getData().size(); i++) {
				GovdocListVO vo = (GovdocListVO)flipInfo.getData().get(i);
				DataRow row = new DataRow();
				for(int j=0; j<columns.size(); j++) {
					Map<String, String> map = columns.get(j);
					String name = map.get("name");
					String value = GovdocListHelper.getListColumnValue(name, vo);
					row.addDataCell(value, j + 1);
				}
			    datarow[i] = row;
			}
		}
		dataRecord.addDataRow(datarow);
		
		//列名
		if(Strings.isNotEmpty(columns)) {
			String[] columnName = new String[columns.size()];;
			for(int j=0; j<columns.size(); j++) {
				Map<String, String> map = columns.get(j);
				columnName[j] = map.get("display");
			}
			dataRecord.setColumnName(columnName);
		}
		
		if(Strings.isNotBlank(params.get("statTitle"))) {
            dataRecord.setTitle(params.get("statTitle"));
            dataRecord.setSheetName(params.get("statTitle"));
        }
		
		return dataRecord;
	}
	
	/**
	 * 公文查询结果导出Excel
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public static DataRecord getQueryListExcel(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		List<Map<String, String>> columns = GovdocListHelper.getQueryListColumn(params);
		
		DataRecord dataRecord = new DataRecord();
	    DataRow[] datarow = new DataRow[flipInfo.getData().size()];
	    
		//数据
		if(Strings.isNotEmpty(flipInfo.getData())) {
			for(int i=0; i<flipInfo.getData().size(); i++) {
				GovdocListVO vo = (GovdocListVO)flipInfo.getData().get(i);
				
				DataRow row = new DataRow();
				for(int j=0; j<columns.size(); j++) {
					Map<String, String> map = columns.get(j);
					String name = map.get("name");
					String value = GovdocListHelper.getListColumnValue(name, vo);
					row.addDataCell(value, j + 1);
				}
			    datarow[i] = row;
			}
		}
		dataRecord.addDataRow(datarow);
		
		//列名
		if(Strings.isNotEmpty(columns)) {
			String[] columnName = new String[columns.size()];;
			for(int j=0; j<columns.size(); j++) {
				Map<String, String> map = columns.get(j);
				columnName[j] = map.get("display");
			}
			dataRecord.setColumnName(columnName);
		}
		
		if(Strings.isNotBlank(params.get("statTitle"))) {
            dataRecord.setTitle(params.get("statTitle"));
            dataRecord.setSheetName(params.get("statTitle"));
        }
		
		return dataRecord;
	}
		
    /**
     * 公文元素导出Excel
     * @param request
     * @param elementList
     * @param element_title
     * @return
     */
	public static DataRecord exportEdocElement(HttpServletRequest request,List<EdocElement> elementList,String element_title){
		DataRecord dataRecord = new DataRecord();

		Locale local = LocaleContext.getLocale(request);
		String resource = "com.seeyon.v3x.edoc.resources.i18n.EdocResource";

		String elementName = ResourceBundleUtil.getString(resource, local, "edoc.element.elementName");//元素名称
		String elementCode = ResourceBundleUtil.getString(resource, local, "edoc.element.elementfieldName");//元素代码
		String dataType = ResourceBundleUtil.getString(resource, local, "edoc.element.elementType");//数据类型
		String elementType = ResourceBundleUtil.getString(resource, local, "edoc.element.elementIsSystem"); //元素类型
		
		if (null != elementList && elementList.size() > 0) {
			DataRow[] datarow = new DataRow[elementList.size()];
			for (int i = 0; i < elementList.size(); i++) {
				EdocElement element = elementList.get(i);
				DataRow row = new DataRow();				
				String name = element.getName();
				String dType = "";
				String dTypeLable = "";
				String eType = ResourceBundleUtil.getString(resource, local, "edoc.element.userType");;
				
				boolean isSystem = element.getIsSystem();
				if(isSystem){
					if(null!=name && !"".equals(name)) {
						name = ResourceBundleUtil.getString(resource, local, name);
						eType = ResourceBundleUtil.getString(resource, local, "edoc.element.systemType");
					}
				}
				
				switch(element.getType()) {
					case EdocElement.C_iElementType_Comment : dTypeLable = "edoc.element.comment";break;
					case EdocElement.C_iElementType_Date : dTypeLable = "edoc.element.date";break;
					case EdocElement.C_iElementType_Decimal : dTypeLable = "edoc.element.decimal";break;
					case EdocElement.C_iElementType_Integer : dTypeLable = "edoc.element.integer";break;
					case EdocElement.C_iElementType_List : dTypeLable = "edoc.element.list";break;
					case EdocElement.C_iElementType_LogoImg : dTypeLable = "edoc.element.img";break;
					case EdocElement.C_iElementType_String : dTypeLable = "edoc.element.string";break;
					case EdocElement.C_iElementType_Text : dTypeLable = "edoc.element.text";break;
				}
		
				dType = ResourceBundleUtil.getString(resource, local, dTypeLable);
				
				row.addDataCell(null!= name ? String.valueOf(name) : "", 1);
				row.addDataCell(null!= element.getFieldName() ? String.valueOf(element.getFieldName()) : "", 1);
				row.addDataCell(null!= dType ? String.valueOf(dType) : "", 1);
				row.addDataCell(null!= eType ? String.valueOf(eType) : "", 1);
				
				datarow[i] = row;
			}
			try {
				dataRecord.addDataRow(datarow);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		
		String[] columnName = { elementName , elementCode , dataType , elementType};
		dataRecord.setColumnName(columnName);
		dataRecord.setTitle(element_title);
		dataRecord.setSheetName(element_title);
	
		return dataRecord;
	}
    
}
