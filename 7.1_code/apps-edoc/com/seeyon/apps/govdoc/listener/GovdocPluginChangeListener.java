/**
 * 
 */
package com.seeyon.apps.govdoc.listener;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.seeyon.apps.govdoc.manager.GovdocGenerateManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.plugin.PluginAddEvent;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.edoc.manager.EdocElementManager;

/**
 * 新增edoc插件，需补全公文基础数据
 * @author : 唐桂林
 * @Date : 2019年3月16日
 */
public class GovdocPluginChangeListener {
	
	private static Log LOGGER = CtpLogFactory.getLog(GovdocPluginChangeListener.class);
	
	private GovdocGenerateManager govdocGenerateManager;
	private OrgManager orgManager;
	private EdocElementManager edocElementManager;

	@ListenEvent(event = PluginAddEvent.class)
	public void onPluginAdd(PluginAddEvent evt) {
		Set<String> plugins = evt.getPlugins();
		if (Strings.isNotEmpty(plugins) && plugins.contains("edoc")) {
			LOGGER.info("edoc插件补偿");
			try {
				// 遍历各单位，确定是否未初始化；如未初始化，则进行补偿。
				List<V3xOrgAccount> allAccounts = orgManager.getAllAccounts();
				if (Strings.isNotEmpty(allAccounts)) {

					boolean isCreate = isNeedCreate(allAccounts);

					if (isCreate) {
						for (V3xOrgAccount account : allAccounts) {
							if (!account.isGroup()) {
								LOGGER.info("复制公文数据开始...");
								govdocGenerateManager.generate(account);
								LOGGER.info("复制公文数据结束...");
							}
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("edoc插件补偿：", e);
			}
		}
	}

	private boolean isNeedCreate(List<V3xOrgAccount> allAccounts) {
		boolean isCreate = true;
		if (Strings.isNotEmpty(allAccounts)) {
			long accountId = 0;
			for (V3xOrgAccount account : allAccounts) {
				if (!account.isGroup()) {
					accountId = account.getId();
					break;
				}
			}
			/**
			 * 1、只判断会议分类就可以了，有会议分类肯定会议资源肯定也有了，这2个是绑定在一起的。
			 * 2、可预见的会议分类不会很多，就直接查list了，没有新增count计数的方法，会有
			 */
			int count = edocElementManager.getAllEdocElementCount(accountId);
			if (count > 0) {
				isCreate = false;
			}
		}
		return isCreate;
	}
	

	public OrgManager getOrgManager() {
		return orgManager;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public EdocElementManager getEdocElementManager() {
		return edocElementManager;
	}
	public void setEdocElementManager(EdocElementManager edocElementManager) {
		this.edocElementManager = edocElementManager;
	}
	public GovdocGenerateManager getGovdocGenerateManager() {
		return govdocGenerateManager;
	}
	public void setGovdocGenerateManager(GovdocGenerateManager govdocGenerateManager) {
		this.govdocGenerateManager = govdocGenerateManager;
	}
}
