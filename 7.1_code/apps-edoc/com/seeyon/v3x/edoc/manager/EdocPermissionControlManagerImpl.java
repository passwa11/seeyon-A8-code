package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.List;

import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.Constants;

/**
 * 本文件实现了操作访问。<br>
 * 如果需要增加或减少操作，都不需要更改此文件。相应的需要修改的地方是： <br>
 *  - 表v3x_metadata_item中定义的操作策略的种类。<br>
 *  - 表v3x_config中定义的策略与具体操作的映射。<br>
 *  - 表v3x_config中定义的操作与协同记录属性的映射。<br>
 * 
 * @author maokai
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 */
public class EdocPermissionControlManagerImpl implements EdocPermissionControlManager {

//	private MetadataManager metaDataManager;
	private final static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
			.getLog(EdocPermissionControlManagerImpl.class);
	private ConfigManager configManager;

	public void setConfigManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

//	public void setMetaDataManager(MetadataManager metaDataManager) {
//		this.metaDataManager = metaDataManager;
//	}

	@SuppressWarnings("unchecked")
	public List<String> getAllowedActions(EdocSummary summary, MetadataNameEnum configCategory,String nodePolicy) {
		List<String> actions = new ArrayList<String>();

		// 获得节点上支持的Action
		if (nodePolicy != null) {
			ConfigItem item = configManager.getConfigItem(
					configCategory.name(),
					nodePolicy);
			if (item != null) {
				String value = item.getConfigValue();
				String values[] = value.split(com.seeyon.ctp.common.constants.Constants.STRING_TOKEN_DELIMITER);

				for (int i = 0; i < values.length; i++) {
					actions.add(values[i]);
				}
			}
		}

		// 获得所有协同定义上支持的Action
		if (summary != null) {
			List<ConfigItem> items = configManager
					.listAllConfigByCategory(Constants.ConfigCategory.action_to_col_definition
							.name());

			if (items != null && items.size() > 0) {
				while (items.size() > 0) {
					ConfigItem item = items.remove(0);
					try {
						/*Method mds = EdocSummary.class.getMethod("get"
								+ item.getConfigValue());
						if ((Boolean) (mds.invoke(summary)) == false){
							actions.remove(item.getConfigItem());
						}
						*/
					}
					catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
			
			/**
			 * 不允许改变流程
			 */
			/*if(!summary.getCanModify()){
				actions.remove("Infom");
				actions.remove("AddNode");
				actions.remove("JointSign");
				actions.remove("RemoveNode");
			}*/
		}

		return actions;
	}

	public boolean isActionAllowed(EdocSummary summary,MetadataNameEnum configCategory, String nodePolicy,
			String action) {
		return getAllowedActions(summary, configCategory,nodePolicy).contains(action);
	}

}
