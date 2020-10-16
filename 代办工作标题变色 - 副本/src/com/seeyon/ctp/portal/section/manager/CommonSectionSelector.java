/**
 * $Author:  $
 * $Rev:  $
 * $Date:: #$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.section.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.portal.section.bo.SectionTreeNode;
import com.seeyon.ctp.util.UUIDLong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title: 常用备选栏目选择器
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 *
 * @since CTP2.0
 */
public class CommonSectionSelector extends BaseAbstractSectionSelector {
	@Override
	public List<SectionTreeNode> selectSectionTreeData(String spaceType,
			String spaceId) throws BusinessException {
		List<String[]> sections = super.selectAllowedSections(spaceType);
		List<SectionTreeNode> l = new ArrayList<SectionTreeNode>();
		if (sections != null) {
			for (String[] str : sections) {
				Long uuid = UUIDLong.longUUID();
				SectionTreeNode node = new SectionTreeNode();
				String sectionBeanId = str[0];
				String sectionName = str[1];
				if ("pendingSection".equals(sectionBeanId)) {
					Map<String, String> properties = new HashMap<String, String>();
					properties.put("graphical_value","importantLevel,overdue,handlingState,handleType,exigency");
					node.setProperties(properties);
				}

                if ("customMembersSection".equals(sectionBeanId) || "departmentMembersSection".equals(sectionBeanId) || "guestbookSection".equals(sectionBeanId)) {
                    node.setUnique(true);
                }

				node.setId(String.valueOf(uuid));
				node.setSectionBeanId(sectionBeanId);
				node.setSectionName(sectionName);
				l.add(node);
			}
		}
		return l;
	}

}
