package com.seeyon.apps.govdoc.listener;

import java.util.ArrayList;
import java.util.List;

import com.seeyon.apps.doc.api.DocApi;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.news.api.NewsApi;
import com.seeyon.apps.news.bo.NewsTypeBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ProductEditionEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.portal.api.PortalApi;
import com.seeyon.ctp.portal.po.PortalPagePortlet;
import com.seeyon.ctp.portal.po.PortalSpaceFix;
import com.seeyon.ctp.portal.po.PortalSpacePage;
import com.seeyon.ctp.portal.space.event.AddSpaceEvent;
import com.seeyon.ctp.portal.util.Constants.SpaceType;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.ListenEvent;

public class GovdocPortalEventListener {
	
	private PortalApi portalApi;
	private NewsApi newsApi;
	private DocApi docApi;
	private OrgManager orgManager;
	
	@SuppressWarnings("static-access")
	@ListenEvent(event = AddSpaceEvent.class, async = true)
	public void updateSpaceData(AddSpaceEvent evt) throws BusinessException {
		PortalSpaceFix spaceFix = evt.getSpaceFix();
		int spaceType = spaceFix.getType();
		// 只有G6才有这个功能
		ProductEditionEnum currentProductEditionEnum = ProductEditionEnum.getCurrentProductEditionEnum();
		if (currentProductEditionEnum.getKey() == currentProductEditionEnum.governmentgroup.getKey()) {
			if (SpaceType.corporation.ordinal() == spaceType) {
				Long spaceId = spaceFix.getId();
				Long accountId = spaceFix.getAccountId();
				PortalSpacePage spacePage = portalApi.getSpacePage(spaceFix.getPath());
				PortalPagePortlet root = (PortalPagePortlet) spacePage.getExtraAttr("RootPagePortlet");
				@SuppressWarnings("unchecked")
				List<PortalPagePortlet> portletList = (List<PortalPagePortlet>) root.getExtraAttr("ChildrenPortlets");
				List<String[]> infos = getPortletInfo();
				// 需要查询出来
				List<NewsTypeBO> newsTypeList = newsApi.findNewsTypesByAccountId(new FlipInfo(), accountId, "", "", "");
				V3xOrgMember member = orgManager.getMemberById(OrgConstants.GROUP_ADMIN_ID);
				User currentUser = new User();
				currentUser.setId(member.getId());
				currentUser.setLoginAccount(member.getOrgAccountId());
				currentUser.setLocale(AppContext.getLocale());
				AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, currentUser);
				for (PortalPagePortlet portalPagePortlet : portletList) {
					for (String[] info : infos) {
						if(portalPagePortlet.getLayoutRow().toString().equals(info[1]) && portalPagePortlet.getLayoutColumn().toString().equals(info[2])){
							resetSingleBoardId(currentUser, info, spaceId, portalPagePortlet.getId(), newsTypeList, accountId);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 通过插件配置获取单位空间栏目信息
	 * 
	 * @return
	 */
	private List<String[]> getPortletInfo() {
		PluginDefinition definition = SystemEnvironment.getPluginDefinition("govdoc");
		String accountPortlet = definition.getPluginProperty("govdoc.accountPortlet");
		List<String[]> infos = new ArrayList<String[]>();
		if (!Strings.isBlank(accountPortlet)) {
			String[] portlets = accountPortlet.split(",");
			for (String portlet : portlets) {
				infos.add(portlet.split("\\|"));
			}
		}
		return infos;
	}

	private void resetSingleBoardId(User user, String[] info, Long spaceId, Long portletId, List<NewsTypeBO> newsTypeList, Long accountId) throws BusinessException {
		// String name = info[0];
		String x = info[1];
		String y = info[2];
		String propertyName = info[3];
		String tabIndex = info[4];
		String type = info[5];
		// news
		if ("news".equals(type)) {
			NewsTypeBO newsBO = getNewsType(newsTypeList, info[0]);
			if (newsBO != null) {
				portalApi.updatePortletProperty(user, spaceId.toString(), portletId, tabIndex, x, y, propertyName, newsBO.getId().toString());
			}
		} else if ("doc".equals(type)) {// doc
			// 文档资料不能用menu的id 需要根据单位在docresource中去获取
			DocResourceBO doc = docApi.getAccountFolder(accountId);
			if (doc != null) {
				portalApi.updatePortletProperty(user, spaceId.toString(), portletId, tabIndex, x, y, propertyName, doc.getId().toString());
			}
		}
	}

	private NewsTypeBO getNewsType(List<NewsTypeBO> newsTypeList, String name) {
		for (NewsTypeBO newsTypeBO : newsTypeList) {
			if (name.equals(newsTypeBO.getTypeName())) {
				return newsTypeBO;
			}
		}
		return null;
	}

	public void setPortalApi(PortalApi portalApi) {
		this.portalApi = portalApi;
	}
	public void setNewsApi(NewsApi newsApi) {
		this.newsApi = newsApi;
	}
	public void setDocApi(DocApi docApi) {
		this.docApi = docApi;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
}
